/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architect.room.optimalrect;

import architect.Constants;
import architect.floorplan.FloorPlanPoly;
import architect.math.Rectangle;
import architect.math.segments.Side;
import architect.math.Vector2D;
import architect.room.Room;
import java.util.Collection;
import java.util.EnumSet;

/**
 *
 * @author AK47
 */
public class OptimalRect {

	public Rectangle rect = Rectangle.ZERO;
	public EnumSet<Side> canMove = EnumSet.of(Side.Left, Side.Right, Side.Top, Side.Bottom);


	public boolean isValid() {
		return rect.area() > Constants.EPSILON;
	}

	public void update(Room room, FloorPlanPoly floorPlanPoly) {
		if (room.biggestRect() == null) {
			throw new RuntimeException("Can't calculate optimal rect without biggestRect of " + room);
		}

		// make optimalRect square and contain optimal area
		float sideLength = (float) Math.sqrt(room.stats().needsArea);
		float halfSideLength = sideLength * 0.5f;
		Vector2D center = room.biggestRect().centerPoint();
		rect = new Rectangle(center.x - halfSideLength, center.y - halfSideLength, sideLength, sideLength);

		canMove = EnumSet.of(Side.Left, Side.Right, Side.Top, Side.Bottom);

		Rectangle boundingRect = room.boundingRect();

		// snap the rect to the outer edges if needed, check for parallel edgeLoop (windowed wall wins)
		if (room.stats().needsWindow) {
			float newMinX = rect.minX;
			float newMinY = rect.minY;

			boolean bottomHasWindowableWall = room.touchingWindowableEdge(floorPlanPoly, Side.Bottom);
			boolean topHasWindowableWall = room.touchingWindowableEdge(floorPlanPoly, Side.Top);
			boolean leftHasWindowableWall = room.touchingWindowableEdge(floorPlanPoly, Side.Left);
			boolean rightHasWindowableWall = room.touchingWindowableEdge(floorPlanPoly, Side.Right);

			if (bottomHasWindowableWall) {
				newMinY = boundingRect.minY;
				canMove.remove(Side.Top);
				canMove.remove(Side.Bottom);
			}
			if (topHasWindowableWall) {
				newMinY = boundingRect.maxY - rect.height;
				canMove.remove(Side.Bottom);
				canMove.remove(Side.Top);
			}
			if (leftHasWindowableWall) {
				newMinX = boundingRect.minX;
				canMove.remove(Side.Right);
				canMove.remove(Side.Left);
			}
			if (rightHasWindowableWall) {
				newMinX = boundingRect.maxX - rect.width;
				canMove.remove(Side.Left);
				canMove.remove(Side.Right);
			}

			// if windows left and right -> choose the longer window wall 
			if (rightHasWindowableWall && leftHasWindowableWall) {
				if (room.windowablePerimeter(Side.Left) > room.windowablePerimeter(Side.Right)) {
					newMinX = boundingRect.minX;
					canMove.add(Side.Left);
				}
				else {
					newMinX = boundingRect.maxX - rect.width;
					canMove.add(Side.Right);
				}
			}

			// if windows up and bellow -> choose the longer window wall 
			if (topHasWindowableWall && bottomHasWindowableWall) {
				if (room.windowablePerimeter(Side.Bottom) > room.windowablePerimeter(Side.Top)) {
					newMinY = boundingRect.minY;
					canMove.add(Side.Bottom);
				}
				else {
					newMinY = boundingRect.maxY - rect.height;
					canMove.add(Side.Top);
				}
			}
			rect = rect.movedTo(newMinX, newMinY);
		}
		else { // doesn't need window -> snap to outer wall anyway if touching
			float newMinX = rect.minX;
			float newMinY = rect.minY;
			if (room.touchingEdge(floorPlanPoly, Side.Bottom)) {
				newMinY = boundingRect.minY;
				canMove.remove(Side.Top);
			}
			else if (room.touchingEdge(floorPlanPoly, Side.Top)) {
				newMinY = boundingRect.maxY - rect.height;
				canMove.remove(Side.Bottom);
			}
			if (room.touchingEdge(floorPlanPoly, Side.Left)) {
				newMinX = boundingRect.minX;
				canMove.remove(Side.Right);
			}
			else if (room.touchingEdge(floorPlanPoly, Side.Right)) {
				newMinX = boundingRect.maxX - rect.width;
				canMove.remove(Side.Left);
			}
			rect = rect.movedTo(newMinX, newMinY);
		}

		rect = rect.movedOutside(floorPlanPoly.entrance, canMove, floorPlanPoly.boundingRect());
	}

	/**
	 * Move optimalrect away from neighboring rects if they overlap, this should equalize area distribution some more
	 *
	 * @param room
	 * @param floorPlanRect
	 * @param rooms
	 */
	public void relaxCollisions(Room room, FloorPlanPoly floorPlanRect, Collection<Room> rooms) {
		if (!room.getOptimalRect().isValid()) {
			return; // hallway does not use optimal rect
		}

		//Rectangle biggestRect = room.biggestRect();		
		CollisionResolve resolve = new CollisionResolve();

		// compute shortest distance to resolve collision with each neighboring room optimalrect (either vertical or horizontal)
		for (Room neighbor : rooms) {
			if (neighbor != room && neighbor.getOptimalRect().isValid()) {
				Vector2D move = rect.shortestCollisionResolve(neighbor.getOptimalRect().rect, canMove, null);
				resolve.addResolve(move);
			}
		}

		if (!resolve.isZero()) {
			// moving optimal rect can make it overlap with previously missed rects
			Rectangle movedRect = rect.moved(resolve.getAverage());
			for (Room neighbor : rooms) {
				if (neighbor != room && neighbor.getOptimalRect().isValid()) {
					Vector2D move = movedRect.shortestCollisionResolve(neighbor.getOptimalRect().rect, canMove, null);
					resolve.addResolve(move);
				}
			}

			// move, but dont collide with entrance rect and stay inside floorPlanPoly
			//TODO: change move inside boundingRect   
			if (!rect.overlaps(floorPlanRect.entrance))
				rect = rect.movedDistOrUntilTouch(resolve.getAverage(), floorPlanRect.entrance).movedInside(floorPlanRect.boundingRect());
		}
	}
}
