/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package crawler.map;

import architect.Architect;
import architect.ArchitectBuilder;
import architect.BuildingArchitect;
import architect.floorplan.FloorPlan;
//import architect.floorplan.FloorPlanComplex;
import architect.floorplan.FloorPlanScriptSource;
import architect.room.Room;
import architect.utils.UniqueID;
import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import crawler.main.Globals;
import crawler.map.rooms.WallSegment;
import crawler.navmesh.NavMesh;
import crawler.navmesh.SuperNavMesh;
import crawler.navmesh.builder.NavMeshBuilder;
import crawler.navmesh.builder.NavMeshDisplay;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import lombok.extern.java.Log;

/**
 *
 * @author VTPlusAKnauer
 */
@Log
public class MapCreator {

	public final Node navDisplay = new Node();
	public final SuperNavMesh superNavMesh = new SuperNavMesh();
	public final ArrayList<Room> allRooms = new ArrayList<>();
	public HashMap<Room, ArrayList<WallSegment>> wallSegmentMap = new HashMap<>();

	public Node create(int seed) {
		Node mapNode = new Node();

		//Material mat = MaterialLoader.load("Textures/concrete.dds", "Textures/concrete_normal.dds");
		BoundingBox complexBBox = new BoundingBox();
		//	LinkedList<NavMesh> stairNavMeshes = new LinkedList<>();
		NavMesh stairsNavMeshLevel0 = null;
		//for (int s = 0; s < 1; s++) {
			// create staircase
//			Vector3f translation = new Vector3f(0, s * 3, 0);
//			Node staircase = (Node) assetManager.loadModel("Models/staircase.j3o");
//			staircase.setMaterial(mat);
//			staircase.setLocalTranslation(translation);
//
//			// add collision shape to staircase
//			MeshCollisionShape shape = new MeshCollisionShape(((Geometry) (staircase.getChild(0))).getMesh());
//			RigidBodyControl rigidBodyControl = new RigidBodyControl(shape, 0);
//			rigidBodyControl.setCollideWithGroups(0xffffff);
//			staircase.addControl(rigidBodyControl);
//			physicsSpace.add(staircase);
//			mapNode.attachChild(staircase);

			// create floorplan
//			BoundingBox stairsBBox = new BoundingBox(new Vector3f(0, -1.5f, -1.5f), new Vector3f(6.5f, 3, 1.5f));
//			stairsBBox.setCenter(stairsBBox.getCenter().add(translation));
//			NavMesh stairsNavMesh = NavMeshBuilder.buildForStairCase(translation, stairsBBox, "staircase" + s);
//			if (!stairNavMeshes.isEmpty()) { // connect to previous
//				stairsNavMesh.connectEntrancesWith("bottom", stairNavMeshes.getLast(), "top");
//			}
//
//			superNavMesh.add(stairsNavMesh);
//			stairNavMeshes.add(stairsNavMesh);
			Random rand = new Random();
			rand.setSeed(seed);
			UniqueID.resetAll();

		
			BuildingArchitect buildingArchitect = new BuildingArchitect(rand, "../ArchitectScripts");
			
			Vector3f apartmentOffset = new Vector3f(0, 0, 0);
			
			for (FloorPlan floorPlan : buildingArchitect.getSubFloorPlans()) {
				BoundingBox apartMentBBox = floorPlanToNodes(mapNode, floorPlan, apartmentOffset);
				NavMesh navMesh = createNavMesh(apartMentBBox, floorPlan, apartmentOffset);
				// TODO: connect entrances with any door-connected mesh
				// navMesh.connectEntrancesWith(hallwayNavMesh);
				superNavMesh.add(navMesh);
				complexBBox.mergeLocal(apartMentBBox);
				
				allRooms.addAll(floorPlan.rooms);
			}
	
//			Architect complexArchitect = ArchitectBuilder.fromScript("lua/architect/apartment_complex.lua");
//			FloorPlanScriptSource complexPlanSource = new FloorPlanScriptSource("lua/architect/apartment_complex_floorplan.lua");
//			Architect subArchitect = ArchitectBuilder.fromScript("lua/architect/apartment.lua");

			
			

			
//		
//			FloorPlanComplex floorPlanComplex = new FloorPlanComplex(complexArchitect, subArchitect, complexPlanSource);
//			floorPlanComplex.makeFloorPlans(rand);
//
//			Vector3f apartmentOffset = new Vector3f(0, 0, 0);
//
//			for (FloorPlan floorPlan : floorPlanComplex.getSubFloorPlans()) {
//				BoundingBox apartMentBBox = floorPlanToNodes(mapNode, floorPlan, apartmentOffset);
//				NavMesh navMesh = createNavMesh(apartMentBBox, floorPlan, apartmentOffset);
//				// TODO: connect entrances with any door-connected mesh
//				// navMesh.connectEntrancesWith(hallwayNavMesh);
//				superNavMesh.add(navMesh);
//				complexBBox.mergeLocal(apartMentBBox);
//				
//				allRooms.addAll(floorPlan.rooms);
//			}
	//	}

		setupNavDisplayNode(mapNode);

		mapNode.setModelBound(complexBBox);
		Vector3f min = complexBBox.getMin();
		Vector3f max = complexBBox.getMax();
		Globals.getLpvProcessor().setLPVShape(min, max.subtract(min));

		return mapNode;
	}

	private BoundingBox floorPlanToNodes(Node mapNode, FloorPlan floorPlan, Vector3f apartmentOffset) {
		BoundingBox apartMentBBox = new BoundingBox();
		
		Node apartmentNode = FloorPlanToNode.createApartment(floorPlan, wallSegmentMap, apartmentOffset, apartMentBBox);
		// add geometry
		mapNode.attachChild(apartmentNode);
		return apartMentBBox;
	}

	private NavMesh createNavMesh(BoundingBox apartMentBBox, FloorPlan floorPlan, Vector3f apartmentOffset) {
		NavMesh navMesh = NavMeshBuilder.build(floorPlan, apartmentOffset, apartMentBBox,
											   "apartment" + floorPlan.id, floorPlan.attribs);
		Node apartmentNavDisplay = NavMeshDisplay.createDisplay(navMesh);
		navDisplay.attachChild(apartmentNavDisplay);

		return navMesh;
	}

	private void setupNavDisplayNode(Node mapNode) {
		mapNode.attachChild(navDisplay);
		navDisplay.setQueueBucket(RenderQueue.Bucket.Overlay);
		navDisplay.setCullHint(Spatial.CullHint.Always); // hidden by default
	}

	public void toggleShowNavMesh() {
		if (navDisplay.getCullHint() != Spatial.CullHint.Always)
			navDisplay.setCullHint(Spatial.CullHint.Always);
		else
			navDisplay.setCullHint(Spatial.CullHint.Inherit);
	}
}
