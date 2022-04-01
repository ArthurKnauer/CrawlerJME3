/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package crawler.interiordesign;

import architect.room.Room;
import architect.room.RoomType;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import crawler.debug.GeometryBuilder;
import crawler.interiordesign.agents.Agent;
import crawler.interiordesign.agents.AgentTypes;
import crawler.main.Globals;
import crawler.map.rooms.WallSegment;
import java.util.*;
import java.util.logging.Level;
import lombok.extern.java.Log;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;

/**
 *
 * @author VTPlusAKnauer
 */
@Log
public class InteriorDesigner {

	private Room currentRoom;
	private Node currentRoomNode;
	private boolean showDebugDisplay;

	private final HashMap<Room, HashMap<Integer, RandomIterableList<Agent>>> roomAgentsMap = new HashMap<>();
	private final ArrayList<Agent> allAgents = new ArrayList<>();
	private final ArrayList<Agent> obstructors = new ArrayList<>();

	private Node debugDisplay = new Node();

	private final HashMap<Integer, ColorRGBA> typeToDebugColor = new HashMap<>();
	private final ColorRGBA defaultDebugColor = ColorRGBA.White;

	public InteriorDesigner() {
		typeToDebugColor.put(AgentTypes.INVISIBLE_OBSTRUCTOR, ColorRGBA.Red);
		typeToDebugColor.put(AgentTypes.WINDOW_WALL, ColorRGBA.Cyan);
		typeToDebugColor.put(AgentTypes.WINDOWLESS_WALL, ColorRGBA.Blue);
	}

	public void furnishAll(Collection<Room> rooms, Node furnitureNod, HashMap<Room, ArrayList<WallSegment>> wallSegmentMap) {
		clearAllAgents();
		clearDebugDisplay(furnitureNod);
		

		// furnish rooms
		int counter = 1;
		for (Room room : rooms) {
			if (room.type() == RoomType.Bedroom && counter == 2) {
				furnish(room, furnitureNod, wallSegmentMap.get(room));
				break;
			}
			if (room.type() == RoomType.Bedroom)
				counter++;
		}
	}

	private void clearDebugDisplay(Node rootNode) {
		debugDisplay.removeFromParent();
		debugDisplay = new Node();
		debugDisplay.setQueueBucket(RenderQueue.Bucket.Overlay);
		debugDisplay.setCullHint(showDebugDisplay ? Spatial.CullHint.Inherit : Spatial.CullHint.Always);
		rootNode.attachChild(debugDisplay);
	}

	public void toggleFurnitureDebugDisplay() {
		showDebugDisplay = !showDebugDisplay;
		debugDisplay.setCullHint(showDebugDisplay ? Spatial.CullHint.Inherit : Spatial.CullHint.Always);
	}

	private void clearAllAgents() {
		allAgents.clear();
		obstructors.clear();

		for (HashMap<Integer, RandomIterableList<Agent>> map : roomAgentsMap.values()) {
			for (RandomIterableList<Agent> list : map.values()) {
				for (Agent agent : list) {
					agent.removeSpatialFromParent();
					agent.removeFromPhysicsSpace();
				}
				list.clear();
			}
		}
	}

	private void furnish(Room room, Node roomNode, ArrayList<WallSegment> wallSegments) {
		currentRoom = room;
		currentRoomNode = roomNode;

		HashMap<Integer, RandomIterableList<Agent>> map = roomAgentsMap.get(room);
		if (map == null) {
			map = new HashMap<>();
			roomAgentsMap.put(room, map);
		}

		List<Agent> wallAgents = WallSideToAgent.wallAgentsForRoom(room.getWallSides());
		addAllAgents(wallAgents);
		shuffleAgents(map);

		try {
			LuaFunction furnish = compileLuaFurnishFunction();
			furnish.call(CoerceJavaToLua.coerce(this), CoerceJavaToLua.coerce(Globals.getRandom()), CoerceJavaToLua.coerce(room.type()));
		} catch (Exception ex) {
			log.log(Level.SEVERE, "furnish error", ex);
		}

		// add window models
		Node windows = WindowInstaller.buildNodeWithWindows(room);
		roomNode.attachChild(windows);
	}

	private static LuaFunction compileLuaFurnishFunction() {
		LuaValue globals = JsePlatform.standardGlobals();
		globals.get("dofile").call(LuaValue.valueOf("assets/lua/furnish.lua"));
		return (LuaFunction) globals.get("furnish");
	}

	private void shuffleAgents(HashMap<Integer, RandomIterableList<Agent>> map) {
		for (ArrayList<Agent> list : map.values()) {
			Collections.shuffle(list, Globals.getRandom());
		}
	}

	public void addAllAgents(List<Agent> agents) {
		agents.stream().forEach(agent -> addAgent(agent));
	}

	public void addAgent(Agent agent) {
		if (agent.needsParents()) {
			if (findAndAttachToParent(agent)) {
				currentRoomNode.attachChild(agent.getSpatial());
				agent.addToPhysicsSpace(0);
				Globals.getLpvProcessor().addOccluder(agent.getGeometry());
				setAgentFixed(agent);
			}
		}
		else {
			setAgentFixed(agent);
		}

		allAgents.add(agent);
		addDebugShape(agent);
	}

	private void setAgentFixed(Agent agent) {
		HashMap<Integer, RandomIterableList<Agent>> map = roomAgentsMap.get(currentRoom);
		RandomIterableList<Agent> list = map.get(agent.getType());
		if (list == null) {
			list = new RandomIterableList<>();
			map.put(agent.getType(), list);
		}
		list.add(agent);

		if (agent.isObstructor())
			obstructors.add(agent);
	}

	private boolean findAndAttachToParent(Agent agent) {
		HashMap<Integer, RandomIterableList<Agent>> map = roomAgentsMap.get(currentRoom);

		for (Integer parentType : agent.getParentTypes()) {
			ArrayList<Agent> parentList = map.get(parentType);
			if (parentList != null) {
				for (Agent parent : parentList) {
					if (parent.attachToSide(agent, obstructors))
						return true;
				}
			}
		}

		return false;
	}

	private void addDebugShape(Agent agent) {
		ColorRGBA color = typeToDebugColor.get(agent.getType());
		if (color == null)
			color = defaultDebugColor;

		Spatial cube = GeometryBuilder.buildWireCube(agent, color);
		debugDisplay.attachChild(cube);
	}

//	public boolean attachToWindowlessWall(Agent agent, Agent.Side agentSide) {
//		return attachToWall(agent, agentSide, roomAgents.windowlessWalls, CHILD_BOTTOM_TO_PARENT_BOTTOM);
//	}
//
//	public boolean attachToAnyWall(Agent agent, Agent.Side agentSide) {
//		return attachToWall(agent, agentSide, roomAgents.allWalls, CHILD_BOTTOM_TO_PARENT_BOTTOM);
//	}
//
//	public boolean attachToWindowWall(Agent agent, Agent.Side agentSide) {
//		return attachToWall(agent, agentSide, roomAgents.windowWalls, CHILD_BOTTOM_TO_PARENT_BOTTOM);
//	}
//
//	public boolean attachToWindowWall(Agent agent, Agent.Side agentSide, Agent.Placement alignment) {
//		return attachToWall(agent, agentSide, roomAgents.windowWalls, alignment);
//	}
//
//	public boolean attachToWall(Agent agent, Agent.Side agentSide, ArrayList<Agent> walls, Agent.Placement alignment) {
//		for (Agent wall : walls) {
//			if (attachToSide(agent, wall, FRONT, agentSide, 0, alignment)) {
//				walls.remove(wall); // move to the end, prefer other walls next time
//				walls.add(wall);
//				return true;
//			}
//		}
//		return false;
//	}
//
//
//	public boolean attachToSide(Agent agent, Agent parent, Agent.Side parentSide, Agent.Side childSide, float mass) {
//		return attachToSide(agent, parent, parentSide, childSide, mass, CHILD_BOTTOM_TO_PARENT_BOTTOM);
//	}
//
//	public boolean attachToShelf(Agent agent, Agent parent, Agent.Side parentSide, Agent.Side childSide, float mass) {
//		if (agent.attachToShelf(parent, parentSide, childSide, rand, roomAgents.allAgents)) {
//			roomAgents.furniture.add(agent);
//			roomAgents.allAgents.add(agent);
//			roomNode.attachChild(agent.spatial);
//			agent.addToPhysicsSpace(physicsSpace, mass);
//			return true;
//		}
//		return false;
//	}
//
//
//	public Agent createAgent(String modelFileLeft, String modelFileMiddle, String modelFileRight,
//							 String textureFile, boolean transparent, boolean isObstructable) {
//
//		Material mat = new Material(assetManager, "MatDefs/LPVShaded.j3md");
//		Texture tex = assetManager.loadTexture(new TextureKey(textureFile, false));
//		tex.setWrap(Texture.WrapMode.Repeat);
//		mat.setTexture("DiffuseMap", tex);
//
//		if (transparent) {
//
//		}
//
//		Geometry geometryLeft = (Geometry) ((Node) assetManager.loadModel(modelFileLeft)).getChild(0);
//		Geometry geometryMiddle = (Geometry) ((Node) assetManager.loadModel(modelFileMiddle)).getChild(0);
//		Geometry geometryRight = (Geometry) ((Node) assetManager.loadModel(modelFileRight)).getChild(0);
//
//		if (transparent) {
//			mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
//			mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
//			mat.setTexture("ColorMap", tex);
//
//			geometryLeft.setQueueBucket(RenderQueue.Bucket.Transparent);
//			geometryLeft.setShadowMode(RenderQueue.ShadowMode.Off);
//			geometryMiddle.setQueueBucket(RenderQueue.Bucket.Transparent);
//			geometryMiddle.setShadowMode(RenderQueue.ShadowMode.Off);
//			geometryRight.setQueueBucket(RenderQueue.Bucket.Transparent);
//			geometryRight.setShadowMode(RenderQueue.ShadowMode.Off);
//		}
//		else {
//			geometryLeft.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
//			geometryMiddle.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
//			geometryRight.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
//		}
//
//		geometryLeft.setMaterial(mat);
//		geometryMiddle.setMaterial(mat);
//		geometryRight.setMaterial(mat);
//
//		geometryLeft.setModelBound(new BoundingBox());
//		geometryLeft.updateModelBound();
//		geometryMiddle.setModelBound(new BoundingBox());
//		geometryMiddle.updateModelBound();
//		geometryRight.setModelBound(new BoundingBox());
//		geometryRight.updateModelBound();
//
//		Vector3f extentLeft = ((BoundingBox) geometryLeft.getModelBound()).getExtent(null);
//		Vector3f extentMiddle = ((BoundingBox) geometryMiddle.getModelBound()).getExtent(null);
//		Vector3f extentRight = ((BoundingBox) geometryRight.getModelBound()).getExtent(null);
//
//		return null;//new ArrayAgent(spatial, new Vector3f(), extent, isObstructable, true);
//	}
}
