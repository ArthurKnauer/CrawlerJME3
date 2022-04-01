/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architect.processors.helpers;

import architect.Constants;
import static architect.Constants.EPSILON;
import architect.floorplan.FloorPlan;
import architect.math.segments.LineSegment;
import architect.math.Rectangle;
import architect.math.Vector2D;
import architect.room.Room;
import architect.room.RoomRect;
import architect.snapgrid.SnapRule;
import static architect.utils.ConstructionFlags.Flag.*;
import static java.lang.Math.max;
import static java.lang.Math.min;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author VTPlusAKnauer
 */
public final class Cutter {

	private Cutter() { // static methods only		
	}

	/**
	 * Cuts RRs of a FloorPlanPoly with a rectangle (cutter)
	 *
	 * @param floorPlan holds all the RoomRects
	 * @param cutter rectangle to cut with
	 * @return RRs inside the cutter
	 */
	public static ArrayList<RoomRect> cut(FloorPlan floorPlan, Rectangle cutter) {
		return Cutter.cut(floorPlan, cutter, null);
	}

	/**
	 * Cuts RRs of a FloorPlanPoly or only a list of RRs with a rectangle (cutter)
	 *
	 * @param fp holds all the RoomRects
	 * @param cutter rectangle to cut with
	 * @param toCut list of RRs to cut, if null -> all RoomRects in the FloorPlan will be checked for overlap and cut
	 * @return RRs inside the cutter
	 */
	public static ArrayList<RoomRect> cut(FloorPlan fp, Rectangle cutter, Collection<RoomRect> toCut) {
		ArrayList<RoomRect> insideCutter = new ArrayList<>();
		ArrayList<RoomRect> toRemove = new ArrayList<>();
		ArrayList<RoomRect> toAdd = new ArrayList<>();
		ArrayList<Room> affectedRooms = new ArrayList<>();

		if (toCut == null)
			toCut = fp.roomRects;

		for (RoomRect rr : toCut) {
			if (rr.flags.contains(DONT_CUT))
				continue;

			Room room = rr.assignedTo().orElse(null); // TODO: refactor
			if (room == null || !room.flags.contains(DONT_CUT)) {
				if (cutter.minOverlap(rr) > fp.attribs.minSubrectCut) {

					List<RoomRect> cutlets = rr.cut(cutter, fp.attribs.minSubrectCut);
					if (cutlets != null) {
						if (room != null) {
							room.removeSubRect(rr);
							room.addAllSubRects(cutlets);

						}
						toRemove.add(rr);
						toAdd.addAll(cutlets);

						for (RoomRect cutlet : cutlets) {
							if (cutlet.overlaps(cutter))
								insideCutter.add(cutlet);
						}
					}
					else { // rr was completely inside cutter
						insideCutter.add(rr);
					}

					if (room != null)
						affectedRooms.add(room);
				}
			}
		}

		fp.roomRects.removeAll(toRemove);
		fp.roomRects.addAll(toAdd);

		return insideCutter;
	}

	/**
	 * Cuts RRs of a FloorPlanPoly with multiple rectangles (cutters)
	 *
	 * @param fp FloorPlan holds all the RoomRects
	 * @param cutters rectangles to cut with
	 * @return RRs inside the cutters
	 */
	public static ArrayList<RoomRect> cut(FloorPlan fp, Collection<Rectangle> cutters) {
		HashSet<RoomRect> insideCutters = new HashSet<>();
		ArrayList<RoomRect> insideCuttersEndResult = new ArrayList<>();

		if (Constants.DEV_MODE) { // check for cutter overlap
			for (Rectangle rectA : cutters) {
				for (Rectangle rectB : cutters) {
					if (rectA != rectB && rectA.overlaps(rectB))
						throw new RuntimeException("Cutting Rects may not overlap: " + rectA + " ovelaps with " + rectB);
				}
			}
		}

		for (Rectangle cutter : cutters) {
			insideCutters.addAll(Cutter.cut(fp, cutter));
		}

		// some insiders maybe have been cut -> filter out that still exist in floorplan
		for (RoomRect insider : insideCutters) {
			if (fp.roomRects.contains(insider))
				insideCuttersEndResult.add(insider);
		}

		return insideCuttersEndResult;
	}

	public static boolean cutConnector(FloorPlan fp, Room roomA, Room roomB) {
		if (roomA.canHaveDoorWith(roomB))
			return false;

		float connectorSize = fp.attribs.hallwayWidth;
		ArrayList<Rectangle> choices = new ArrayList<>(4);

		if (roomA.commonPerimeter(roomB) < EPSILON) { // corner touch
			Vector2D commonCorner = roomA.commonCorner(roomB);
			if (commonCorner == null)
				return false;

			choices.add(new Rectangle(commonCorner.x, commonCorner.y, connectorSize, connectorSize));
			choices.add(new Rectangle(commonCorner.x - connectorSize, commonCorner.y, connectorSize, connectorSize));
			choices.add(new Rectangle(commonCorner.x - connectorSize, commonCorner.y - connectorSize, connectorSize, connectorSize));
			choices.add(new Rectangle(commonCorner.x, commonCorner.y - connectorSize, connectorSize, connectorSize));
		}
		else {
			LineSegment commonWall = roomA.commonLongestWall(roomB);

			if (commonWall == null) {
				throw new RuntimeException("Common perimeter but no common wall between " + roomA + " and " + roomB);
			}
			else if (commonWall.isHorizontal()) {
				choices.add(new Rectangle(commonWall.min, commonWall.pos, connectorSize, connectorSize));
				choices.add(new Rectangle(commonWall.min, commonWall.pos - connectorSize, connectorSize, connectorSize));
				choices.add(new Rectangle(commonWall.max - connectorSize, commonWall.pos, connectorSize, connectorSize));
				choices.add(new Rectangle(commonWall.max - connectorSize, commonWall.pos - connectorSize, connectorSize, connectorSize));
			}
			else { // bestWall is vertical	
				choices.add(new Rectangle(commonWall.pos, commonWall.min, connectorSize, connectorSize));
				choices.add(new Rectangle(commonWall.pos, commonWall.max - connectorSize, connectorSize, connectorSize));
				choices.add(new Rectangle(commonWall.pos - connectorSize, commonWall.min, connectorSize, connectorSize));
				choices.add(new Rectangle(commonWall.pos - connectorSize, commonWall.max - connectorSize, connectorSize, connectorSize));
			}
		}

		Rectangle connector = null;
		float bestScore = 0.0f;
		connectorRectLoop:
		for (Rectangle rect : choices) {
			float overlapA = max(roomA.commonXPerimeter(rect), roomA.commonYPerimeter(rect));
			float overlapB = max(roomB.commonXPerimeter(rect), roomB.commonYPerimeter(rect));
			if (min(overlapA, overlapB) > fp.attribs.minOverlapForDoor - EPSILON) {
				// the less the connector disturbs room optimal rect, the better
				float score = Float.MAX_VALUE;
				for (Room room : fp.rooms) {
					if (room.getOptimalRect().isValid()) {
						score = min(score, room.getOptimalRect().rect.manhattanDistBetweenCenters(rect));
						if (score < bestScore - EPSILON)
							break; // already not the best choice
					}
					for (Rectangle importantRect : room.importantRects()) {
						if (rect.overlaps(importantRect))
							score /= 1.0f + rect.overlapArea(importantRect);
					}
				}
				if (score > bestScore - EPSILON) {
					connector = rect;
					bestScore = score;
				}
			}
		}

		if (connector != null) {
			connector = fp.snapGrid.snap(connector, SnapRule.DONT_SHRINK_AT_ALL);
			Room toGetConnector = roomA;
			if (roomB.overlapArea(connector) > roomA.overlapArea(connector))
				toGetConnector = roomB;

			ArrayList<RoomRect> insideConnector = Cutter.cut(fp, connector);
			toGetConnector.addAllSubRects(insideConnector);
			// important rects make sure their area wont be cut away as a protrusion
			toGetConnector.addImportantRect(connector);
		}
		else {
			throw new RuntimeException("Couldn't connect " + roomA + " with " + roomB);
		}

		return true;
	}
}
