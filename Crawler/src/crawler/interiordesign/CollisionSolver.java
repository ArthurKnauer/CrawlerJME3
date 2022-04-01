package crawler.interiordesign;

import com.jme3.math.Vector3f;
import crawler.interiordesign.agents.Agent;
import crawler.interiordesign.agents.Placement;
import crawler.interiordesign.agents.Side;
import static crawler.interiordesign.agents.Side.*;
import crawler.main.Globals;
import java.util.List;

public class CollisionSolver {

	public static Vector3f findFreeSideSpot(Agent child, Agent parent, List<Agent> placed) {

		Side parentSide = FRONT;
		Side childSide = child.getAttachSides().get(0);

		final float sideExtent = parent.sideExtent(parentSide);
		final float sideLength = sideExtent * 2;
		final float childExtent = child.sideExtent(childSide);
		final float childLength = childExtent * 2;

		if (sideLength < childLength) // no free interval space
			return null;

		// compute correct y-Value for the child (vertical alignment)
		float heightFromCenter = 0;
		Placement placement = child.getPlacement();
		Vector3f extents = parent.getExtents(null);
		Vector3f childExtents = child.getExtents(null);
		switch (placement) {
			case Beside:
				heightFromCenter = -extents.y + childExtents.y;
				break;
			case OnShelf:
				heightFromCenter = +extents.y + childExtents.y;
				break;
			case HangingBelow:
				heightFromCenter = -extents.y - childExtents.y;
				break;
			case BesideTop:
				heightFromCenter = +extents.y - childExtents.y;
				break;
		}
		// place child on the correct side with correct offsets
		Vector3f sideNormal = parent.getSideNormal(parentSide);
		Vector3f normalOffset = sideNormal.mult(parent.normalExtent(parentSide));
		Vector3f normalOffsetChild = sideNormal.mult(child.normalExtent(childSide));
		Vector3f spot = parent.getPosition().add(normalOffset)
				.addLocal(normalOffsetChild)
				.addLocal(0, heightFromCenter, 0); // spot at the center

		// compute an AABBs for the aligned child
		Vector3f sideDirection;
		if (parentSide == FRONT || parentSide == BACK)
			sideDirection = parent.getSideNormal(RIGHT);
		else
			sideDirection = sideNormal.clone();
		Vector3f childExtentPlanar = sideNormal.mult(child.normalExtent(childSide))
				.addLocal(sideDirection.mult(child.sideExtent(childSide)));
		Vector3f childUnobstructExtentPlanar = sideNormal.mult(child.normalUnobstructExtent(childSide))
				.addLocal(sideDirection.mult(child.sideUnobstructExtent(childSide)));

		// move child to either edge, and then slide away from collisions
		float maxMoveLength = sideLength - childLength;
		boolean placingOnMaxEdge = Globals.getRandom().nextBoolean();
		if (placingOnMaxEdge) { // move to the "max" edge
			spot.addLocal(sideDirection.mult(sideExtent - childExtent));
			sideDirection.multLocal(-1);
		}
		else {
			spot.addLocal(sideDirection.mult(childExtent - sideExtent));
		}

		child.setPosition(spot);

//		
//		
//		BoundingBox childBBox = new BoundingBox(spot, Math.abs(childExtentPlanar.x),
//												childExtents.y, Math.abs(childExtentPlanar.z));
//		BoundingBox childUnobstructBBox = new BoundingBox(spot, Math.abs(childUnobstructExtentPlanar.x),
//														  child.unobstructExtents.y, Math.abs(childUnobstructExtentPlanar.z));
		boolean collisionAvoided = avoidCollisionLinear(child, sideDirection, sideNormal, maxMoveLength, placed);
		if (!collisionAvoided)
			return null; // moved too far, outside of parent range
		else
			return child.getPosition();
	}

	private static boolean avoidCollisionLinear(Agent child, Vector3f moveDir, Vector3f normal,
												 float maxMoveLength, List<Agent> toAvoid) {
		float moveToAvoidAll = 0;
		boolean collisionsFound = true;
		while (collisionsFound) {
			collisionsFound = false;
			for (Agent agent : toAvoid) {
				if (agent != child) {
					if (agent.isObstructor()) {
						if (agent.overlaps(child)) {
							float moveToAvoid = 0.1f + child.distToResolveOverlapOnAxis(agent, moveDir);
							child.move(moveDir.mult(moveToAvoid));
							moveToAvoidAll += moveToAvoid;
							if (moveToAvoidAll > maxMoveLength)
								return true; // moved outside of range
							collisionsFound = true;
						}
					}
//					else if (!agent.isObstructable() && agent.unobstructBbox.intersects(childUnobstructBBox)) {
//						float moveToAvoid = 0.1f + agent.unobstructBbox.distanceToCollisionResolve(childUnobstructBBox, moveDir);
//						childBBox.getCenter().addLocal(moveDir.mult(moveToAvoid));
//						childUnobstructBBox.setCenter(childBBox.getCenter());
//						moveToAvoidAll += moveToAvoid;
//						if (moveToAvoidAll > maxMoveLength)
//							return null; // moved outside of range
//						collisionsFound = true;
//					}
//					else if (agent.isObstructable && agent.bbox.intersects(childBBox)) {
//						// move to the side, if moving to the front is longer
//						float moveToFront = 0.01f + agent.bbox.distanceToCollisionResolve(childBBox, normal);
//						float moveToSide = 0.1f + agent.bbox.distanceToCollisionResolve(childUnobstructBBox, moveDir);
//
//						if (moveToSide < moveToFront) {
//							childBBox.getCenter().addLocal(moveDir.mult(moveToSide));
//							childUnobstructBBox.setCenter(childBBox.getCenter());
//							moveToAvoidAll += moveToSide;
//							if (moveToAvoidAll > maxMoveLength)
//								return null; // moved outside of range
//							collisionsFound = true;
//						}
//					}
				}
			}
		}

//		// move to the front away from obstructables (radiator, curtains, etc.)
//		for (Agent agent : toAvoid) {
//			if (agent != this && agent.isObstructable && agent.bbox.intersects(childBBox)) { // move to the "front"
//				float moveToAvoid = 0.01f + agent.bbox.distanceToCollisionResolve(childBBox, normal);
//				if (moveToAvoid > Math.max(agent.bbox.getXExtent(), agent.bbox.getZExtent()))
//					return null; // have to move away to faar, "length-wise" instead of "thickness-wise"
//				childBBox.getCenter().addLocal(normal.mult(moveToAvoid));
//			}
//		}

		return true;
	}

}
