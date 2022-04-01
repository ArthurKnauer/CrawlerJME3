package crawler.map;

import architect.floorplan.FloorPlan;
import architect.math.Rectangle;
import architect.room.Room;
import architect.room.RoomType;
import architect.walls.RoomRectWall;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.collision.shapes.MeshCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import common.material.MaterialLoader;
import crawler.main.Globals;
import crawler.map.rooms.RoomMesh;
import crawler.map.rooms.WallSegment;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author VTPlusAKnauer
 */
class FloorPlanToNode {

	static Node createApartment(FloorPlan floorPlan, HashMap<Room, ArrayList<WallSegment>> wallSegmentMap,
								Vector3f apartmentOffset, BoundingBox bbox) {
		Node apartmentNode = new Node();

		createWallsAndWindows(wallSegmentMap, floorPlan, apartmentOffset, apartmentNode);

		addDoorFrames(floorPlan, apartmentOffset, apartmentNode);

		setBounds(floorPlan, apartmentNode, bbox);
		return apartmentNode;
	}

	private static void createWallsAndWindows(HashMap<Room, ArrayList<WallSegment>> wallSegmentMap,
											FloorPlan floorPlan, Vector3f apartmentOffset, Node apartmentNode) {
		
		Material wallMat = MaterialLoader.load("Textures/concrete.dds", "Textures/concrete_normal.dds");
		Material floorMat = MaterialLoader.load("Textures/wood.dds", "Textures/wood_normal.dds");
		Material bedFloorMat = MaterialLoader.load("Textures/carpet.dds", "Textures/carpet_normal.dds");
		Material kitFloorMat = MaterialLoader.load("Textures/linoleum.dds", "Textures/linoleum_normal.dds");
		Material brFloorMat = MaterialLoader.load("Textures/bathroom_tiles.dds", "Textures/bathroom_tiles_normal.dds");

		for (Room room : floorPlan.rooms) {
			ArrayList<WallSegment> wallSegmentList = new ArrayList<>();
			wallSegmentMap.put(room, wallSegmentList);
			ArrayList<Mesh> meshes = FloorPlanMesher.createRoomMeshes(room, wallSegmentList, floorPlan.attribs);
			for (WallSegment segment : wallSegmentList) {
				segment.getCenter().addLocal(apartmentOffset);
			}

			Mesh floorMesh = meshes.get(RoomMesh.FLOOR_MESH_IDX);
			Geometry floor = new Geometry("floor " + room.name(), floorMesh);
			if (room.type() == RoomType.Bathroom)
				floor.setMaterial(brFloorMat);
			else if (room.type() == RoomType.Bedroom)
				floor.setMaterial(bedFloorMat);
			else if (room.type() == RoomType.Kitchen)
				floor.setMaterial(kitFloorMat);
			else
				floor.setMaterial(floorMat);

			Mesh wallMesh = meshes.get(RoomMesh.WALL_MESH_IDX);
			Geometry wall = new Geometry("wall " + room.name(), wallMesh);
			wall.setMaterial(wallMat);
			wall.updateGeometricState();

			Globals.getLpvProcessor().addOccluder(floor);
			Globals.getLpvProcessor().addOccluder(wall);
			Globals.getLpvProcessor().addLightPoints(meshes.get(RoomMesh.WINDOW_LIGHT_POINTS));

			Node roomNode = new Node(room.name());
			roomNode.attachChild(floor);
			roomNode.attachChild(wall);
			roomNode.setLocalTranslation(apartmentOffset);

			CompoundCollisionShape roomShape = new CompoundCollisionShape();
			roomShape.addChildShape(new MeshCollisionShape(wallMesh), Vector3f.ZERO);
			roomShape.addChildShape(new MeshCollisionShape(floorMesh), Vector3f.ZERO);
			RigidBodyControl rigidBodyControl = new RigidBodyControl(roomShape, 0);
			rigidBodyControl.setCollideWithGroups(0xffffff);
			roomNode.addControl(rigidBodyControl);
			Globals.getPhysicsSpace().add(roomNode);

			roomNode.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);

			Rectangle br = room.boundingRect();
			roomNode.setModelBound(new BoundingBox(new Vector3f(br.minX, 0, br.minY), new Vector3f(br.maxX, 3, br.maxY)));
			apartmentNode.attachChild(roomNode);
		}
	}

	private static void addDoorFrames(FloorPlan floorPlan, Vector3f apartmentOffset, Node apartmentNode) {
		Material woodWhiteMat = MaterialLoader.load("Textures/wood_white.dds", "Textures/wood_normal.dds");

		for (RoomRectWall wall : floorPlan.walls) {
			if (wall.hasDoor()) {
				Node doorNode = new Node();
				Spatial doorFrame = Globals.getAssetManager().loadModel("Models/door_frame.mesh.j3o");
				doorFrame.setMaterial(woodWhiteMat);
				doorFrame.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
				doorNode.attachChild(doorFrame);

				Node door = new Node();
				door.setLocalTranslation(0, 1.11f, 0);
				doorNode.attachChild(door);

//				Spatial doorSpatial = assetManager.loadModel("Models/door.mesh.j3o");
//				doorSpatial.setMaterial(woodWhiteMat);
//				doorSpatial.setShadowMode(ShadowMode.Receive);	
//				doorSpatial.setLocalTranslation(0, -1.11f, 0);
//				doorSpatial.rotate(0, FastMath.PI * 0.5f, 0);
//				door.attachChild(doorSpatial);
				if (wall.isVertical()) {
					doorNode.setLocalTranslation(wall.pos, 0, wall.doorPos());
				}
				else {
					doorNode.setLocalTranslation(wall.doorPos(), 0, wall.pos);
					Matrix3f rot = new Matrix3f();
					rot.fromAngleAxis(FastMath.HALF_PI, new Vector3f(0, 1, 0));
					doorNode.setLocalRotation(rot);
				}
				doorNode.getLocalTranslation().addLocal(apartmentOffset);
				apartmentNode.attachChild(doorNode);

//				door.addControl(new RigidBodyControl(new BoxCollisionShape(new Vector3f(0.02f, 1.0f, 0.5f)), 500));
//				door.getControl(RigidBodyControl.class).setAngularDamping(0.999f);
//				bulletAppState.getPhysicsSpace().add(door);
//				
//				doorFrame.addControl(new RigidBodyControl(new BoxCollisionShape(new Vector3f(0, 0, 0)), 0));
//				bulletAppState.getPhysicsSpace().add(doorFrame);
//			
//				HingeJoint joint = new HingeJoint(doorFrame.getControl(RigidBodyControl.class), // A
//                     door.getControl(RigidBodyControl.class), // B
//                     new Vector3f(0, 0f, 0.5f),  // pivot point local to A
//                     new Vector3f(0f, -1.11f, 0.5f),  // pivot point local to B 
//                     Vector3f.UNIT_Y,           // DoF Axis of A (Z axis)
//                     Vector3f.UNIT_Y);        // DoF Axis of B (Z axis)
//				joint.setLimit(-1.6f, 1.6f);
//				bulletAppState.getPhysicsSpace().add(joint);
			}
		}
	}

	private static void setBounds(FloorPlan floorPlan, Node apartmentNode, BoundingBox bbox) {
		float margin = 0.2f;
		Vector3f minPos = new Vector3f(floorPlan.floorPlanPoly.boundingRect().minX - margin, -margin,
									   floorPlan.floorPlanPoly.boundingRect().minY - margin);//.addLocal(apartmentOffset);
		Vector3f maxPos = new Vector3f(floorPlan.floorPlanPoly.boundingRect().maxX + margin, 3 + margin,
									   floorPlan.floorPlanPoly.boundingRect().maxY + margin);//.addLocal(apartmentOffset);
		BoundingBox apartMentBBox = new BoundingBox(minPos, maxPos);
		apartmentNode.setModelBound(apartMentBBox);
		bbox.setCenter(apartMentBBox.getCenter());
		bbox.setXExtent(apartMentBBox.getXExtent());
		bbox.setYExtent(apartMentBBox.getYExtent());
		bbox.setZExtent(apartMentBBox.getZExtent());
	}
}
