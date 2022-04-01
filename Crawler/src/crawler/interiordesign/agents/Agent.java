/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package crawler.interiordesign.agents;

import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import crawler.interiordesign.CollisionSolver;
import crawler.interiordesign.RandomIterableList;
import static crawler.interiordesign.agents.Side.*;
import static crawler.interiordesign.agents.Side.FRONT;
import crawler.main.Globals;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 *
 * @author VTPlusAKnauer
 */
public class Agent extends OrientedAgentBox {

	private final int type;
	private final Spatial spatial;

	private final RandomIterableList<Integer> parentTypes;
	private final RandomIterableList<Side> attachSides;
	private final Placement placement;

	private boolean addedToPhysicsSpace = false;

	PriorityQueue<Shelf> shelves
						 = new PriorityQueue<>(1, (Shelf a, Shelf b) -> a.children.size() < b.children.size() ? -1 : 1);

	Agent(AgentBuilder builder) {
		super(builder);
		this.type = builder.type;
		this.spatial = builder.spatial;
		this.parentTypes = builder.parentTypes;
		this.attachSides = builder.attachSides;
		this.placement = builder.placement;
	}

	public int getType() {
		return type;
	}

	public Spatial getSpatial() {
		return spatial;
	}

	public Geometry getGeometry() {
		return (Geometry) ((Node) spatial).getChild(0);
	}

	public boolean needsParents() {
		return parentTypes != null && !parentTypes.isEmpty();
	}

	public ArrayList<Integer> getParentTypes() {
		return parentTypes;
	}

	public Placement getPlacement() {
		return placement;
	}

	public List<Side> getAttachSides() {
		return attachSides;
	}

	public boolean isObstructor() {
		return isObstructor;
	}

	public void addToPhysicsSpace() {
		addToPhysicsSpace(0);
	}

	public void addToPhysicsSpace(float mass) {
		if (!addedToPhysicsSpace) {
			BoxCollisionShape boxCollisionShape = new BoxCollisionShape(getExtents(null));
			RigidBodyControl rbc = new RigidBodyControl(boxCollisionShape, mass);
			spatial.addControl(rbc);
			Globals.getPhysicsSpace().add(spatial);
			rbc.setSleepingThresholds(20, 20);
			addedToPhysicsSpace = true;
		}
	}

	public void removeSpatialFromParent() {
		if (spatial != null)
			spatial.removeFromParent();
	}

	public void removeFromPhysicsSpace() {
		if (addedToPhysicsSpace) {
			Globals.getPhysicsSpace().remove(spatial);
			addedToPhysicsSpace = false;
		}
	}

	public boolean attachToSide(Agent child, List<Agent> obstructors) {
		Side mySide = FRONT;
		Side childSide = child.attachSides.random();
		Vector3f spot = CollisionSolver.findFreeSideSpot(child, this, obstructors);
		if (spot != null) {
			child.place(spot, this, mySide, childSide);
			return true;
		}
		return false;
	}

//	public void addShelf(float bottom, float height) {
//		shelves.add(new Shelf(bottom, height, this));
//	}
//
//	public boolean attachToShelf(Agent parent, Side parentSide, Side childSide, Random rand, List<Agent> placed) {
//		Shelf shelf = parent.shelves.peek();
//
//		// orientate agent towards the parent (depends which sides should touch)
//		orientateToParent(parent, parentSide, childSide);
//
//		// set initial random location inside parent top side
//		Vector3f randomLoc = parent.getSideNormal(RIGHT).multLocal((rand.nextFloat() * 2 - 1.0f) * (parent.extents.x - extents.x))
//				.addLocal(parent.frontNormal.mult((rand.nextFloat() * 2 - 1.0f) * (parent.extents.z - extents.z)))
//				.addLocal(parent.location);
//		location.set(randomLoc.x, shelf.shelfBBox.getCenter().y - shelf.shelfBBox.getYExtent() + bbox.getYExtent(), randomLoc.z);
//
//		if (!shelf.children.isEmpty()) { // avoid collision with siblings
//			Vector3f newRandomLoc = parent.getSideNormal(RIGHT).multLocal((rand.nextFloat() * 2 - 1.0f) * (parent.extents.x - extents.x))
//					.addLocal(parent.frontNormal.mult((rand.nextFloat() * 2 - 1.0f) * (parent.extents.z - extents.z)))
//					.addLocal(parent.location);
//
//			Vector3f moveDir = new Vector3f(newRandomLoc.x - randomLoc.x, 0, newRandomLoc.z - randomLoc.z);
//			moveDir.normalizeLocal();
//
//			Vector3f movedLocation = avoidCollisionPlanar(shelf.children, parent, moveDir);
//			location.set(movedLocation);
//			updateBoundingBox();
//
//			// check if inside parent top side after moving
//			BoundingBox shelfBB = new BoundingBox(shelf.shelfBBox);
//			shelfBB.setXExtent(shelfBB.getXExtent() - bbox.getXExtent());
//			shelfBB.setYExtent(shelfBB.getYExtent() - bbox.getYExtent() + 0.01f);
//			shelfBB.setZExtent(shelfBB.getZExtent() - bbox.getZExtent());
//			if (!shelfBB.contains(bbox.getCenter())) {
//				return false;
//			}
//		}
//
//		spatial.setLocalTranslation(location);
//		spatial.lookAt(location.add(frontNormal), Vector3f.UNIT_Y);
//
//		updateBoundingBox();
//
//		// children number increased, reinsert shelf into sorted priority queue
//		shelf.children.add(this);
//		parent.shelves.remove();
//		parent.shelves.add(shelf);
//
//		return true;
//	}
	public void place(Vector3f location, Agent parent, Side parentSide, Side childSide) {
		setPosition(location);
		orientateToParent(parent, parentSide, childSide);

		spatial.setLocalTranslation(location);
		spatial.lookAt(location.add(getZAxis()), Vector3f.UNIT_Y);
		//updateBoundingBox();
	}

//	private Vector3f avoidCollisionPlanar(List<Agent> toAvoid, Agent ignore, Vector3f moveDir) {
//		// moveDir is always on the horizontal X/Z plane		
//		boolean collisionsFound = true;
//		Vector3f movedLocation = location.clone();
//		while (collisionsFound) {
//			collisionsFound = false;
//			for (Agent agent : toAvoid) {
//				if (agent != ignore) {
//					float dist = agent.location.distance(movedLocation);
//					float minDist = cylinderRadius + agent.cylinderRadius;
//					if (dist < minDist - Constants.EPSILON) { // collision
//						collisionsFound = true;
//
//						// find lambda where "location + lambda * moveDir" avoids collision with agent
//						Vector3f ap = movedLocation.subtract(agent.location);
//						float a = moveDir.lengthSquared();
//						float beta = moveDir.dot(ap);
//						float c = ap.lengthSquared() - minDist * minDist;
//						float root = FastMath.sqrt(beta * beta - a * c);
//						float lambda1 = (-beta + root) / a;
//						float lambda2 = (-beta - root) / a;
//
//						float lambda = Math.max(lambda1, lambda2) + Constants.EPSILON;
//						movedLocation.addLocal(moveDir.mult(lambda));
//					}
//				}
//			}
//		}
//
//		return movedLocation;
//	}
	@Override
	public String toString() {
		return "Agent{" + "type=" + type + ", spatial=" + spatial + ", parents=" + parentTypes + ", attachSides=" + attachSides + ", placement=" + placement + ", addedToPhysicsSpace=" + addedToPhysicsSpace + ", shelves=" + shelves + '}';
	}
}
