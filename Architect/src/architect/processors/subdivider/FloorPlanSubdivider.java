/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package architect.processors.subdivider;

import architect.math.segments.Orientation;
import architect.math.Rectangle;
import architect.math.segments.Side;
import architect.processors.FloorPlanProcessor;
import architect.room.Room;
import architect.room.RoomRect;
import architect.room.RoomType;
import architect.snapgrid.SnapRule;
import architect.utils.FloatComparable;
import architect.utils.FloatComparator;
import static java.lang.Math.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

/**
 *
 * @author VTPlusAKnauer
 */
public class FloorPlanSubdivider extends FloorPlanProcessor {

	@Override
	protected void process() {
		Collection<Rectangle> subdivided = subdvideFloorPlanRects();
		subdivided = ensureNoWallThroughEntrance(subdivided);
		subdivided = ensureEntranceRectMaxSize(subdivided);
		Collection<RoomRect> roomRects = createRoomRects(subdivided);

		fp.roomRects.addAll(roomRects);
		fp.rooms.add(createHallwayRoom());
	}

	private List<Rectangle> subdvideFloorPlanRects() {
		int randInt = rand.nextInt(fp.roomsToBuild.list().size());
		int roomRectsToBuild = fp.roomsToBuild.list().size() * 2 // we need twice as many roomRects as rooms
							   + randInt;

		PriorityQueue<FloatComparable<Rectangle>> subdivideQueue = new PriorityQueue<>(roomRectsToBuild + 2,
																					   new FloatComparator<Rectangle>());
		for (Rectangle rect : fp.floorPlanPoly.subRects()) {
			subdivideQueue.offer(new FloatComparable(rect, rect.area()));
		}

		while (subdivideQueue.size() < roomRectsToBuild) {
			Rectangle biggestRect = subdivideQueue.poll().obj;
			SplitChildren children = splitRect(biggestRect, null);

			subdivideQueue.offer(new FloatComparable(children.childA, children.childA.area()));
			subdivideQueue.offer(new FloatComparable(children.childB, children.childB.area()));
		}

		return subdivideQueue.stream().map(crr -> crr.obj).collect(Collectors.toList());
	}

	private Collection<Rectangle> ensureNoWallThroughEntrance(Collection<Rectangle> rects) {
		Rectangle entrance = fp.floorPlanPoly.entrance;
		boolean rectContainingEntranceFound = false;
		ArrayList<Rectangle> result = new ArrayList<>(rects.size());
		for (Rectangle rect : rects) {
			if (!rectContainingEntranceFound && rect.contains(entrance)) {
				rectContainingEntranceFound = true;
				result.add(rect);
			}
			else if (!rectContainingEntranceFound && rect.overlaps(entrance)) {
				rect.cut(entrance).stream().filter(cutlet -> !cutlet.overlaps(entrance)).forEach(cutlet -> {
					result.add(cutlet);
				});
			}
			else
				result.add(rect);
		}

		if (!rectContainingEntranceFound)
			result.add(entrance);

		return result;
	}

	private Collection<Rectangle> ensureEntranceRectMaxSize(Collection<Rectangle> rects) {
		ArrayList<Rectangle> result = new ArrayList<>(rects.size() + 4);

		for (Rectangle rect : rects) {
			if (rect.overlaps(fp.floorPlanPoly.entrance))
				result.addAll(splitEntranceRect(rect));
			else
				result.add(rect);
		}

		return result;
	}

	private Collection<Rectangle> splitEntranceRect(Rectangle entranceOverlappingRect) {
		ArrayList<Rectangle> result = new ArrayList<>(4);
		while (entranceOverlappingRect.area() > fp.attribs.minHallwayArea * 1.5f) {
			SplitChildren split = splitRect(entranceOverlappingRect, fp.floorPlanPoly.entrance);
			Rectangle newOverlapper = split.rectOverlapping(fp.floorPlanPoly.entrance);
			if (newOverlapper.area() < fp.attribs.minHallwayArea)
				break;
			result.add(split.otherThan(newOverlapper));
			entranceOverlappingRect = newOverlapper;
		}

		result.add(entranceOverlappingRect);

		return result;
	}

	private Collection<RoomRect> createRoomRects(Collection<Rectangle> rects) {
		ArrayList<RoomRect> roomRects = new ArrayList<>(rects.size());
		for (Rectangle rect : rects) {
			roomRects.add(new RoomRect(rect, fp.floorPlanPoly));
			fp.snapGrid.snap(rect, SnapRule.CLOSEST);
		}

		updateRRNeighborShips(roomRects);

		return roomRects;
	}

	private void updateRRNeighborShips(Collection<RoomRect> roomRects) {
		for (RoomRect rrA : roomRects) {
			for (RoomRect rrB : roomRects) {
				if (rrA != rrB && rrA.canBeNeighbors(rrB, fp.attribs.minOverLapForNeighbor)) {
					rrA.addNeighbor(rrB);
					rrB.addNeighbor(rrA);
				}
			}
		}
	}

	private SplitChildren splitRect(Rectangle parent, Rectangle notToOverlap) {
		Rectangle childA, childB;
		if (parent.width > parent.height) {
			float separatePos = randSplitPos(parent, Orientation.Vertical, notToOverlap);
			separatePos = fp.snapGrid.snapX(separatePos);
			childA = Rectangle.fromMinMax(parent.minX, parent.minY, separatePos, parent.maxY);
			childB = Rectangle.fromMinMax(separatePos, parent.minY, parent.maxX, parent.maxY);
		}
		else { // parent.height >= parent.width
			float separatePos = randSplitPos(parent, Orientation.Horizontal, notToOverlap);
			separatePos = fp.snapGrid.snapY(separatePos);
			childA = Rectangle.fromMinMax(parent.minX, parent.minY, parent.maxX, separatePos);
			childB = Rectangle.fromMinMax(parent.minX, separatePos, parent.maxX, parent.maxY);
		}

		return new SplitChildren(childA, childB);
	}

	private float randSplitPos(Rectangle parent, Orientation cutOrientation, Rectangle notToOverlap) {
		Side lowSide = Side.lowSide(cutOrientation.opposite());
		float minSplitLength = fp.attribs.minSubrectSplitLength;
		float sideLength = parent.sideLength(cutOrientation.opposite());
		float cutPosLeeway = max(0, sideLength - minSplitLength * 2);
		float randFloat = rand.nextFloat();
		float separateOffset = min(sideLength * 0.5f, minSplitLength) + cutPosLeeway * randFloat;
		float separatePos = parent.sidePos(lowSide) + separateOffset;

		if (notToOverlap != null && notToOverlap.intersects(separatePos, cutOrientation))
			separatePos = notToOverlap.nearestSidePos(separatePos, cutOrientation);

		return separatePos;
	}

	private Room createHallwayRoom() {
		Rectangle entrance = fp.floorPlanPoly.entrance;
		Room hallway = new Room(RoomType.Hallway, fp.roomStats.get(RoomType.Hallway), "hw", fp.attribs);
		RoomRect entranceRR = fp.roomRects.stream().filter(rr -> rr.overlaps(entrance)).findFirst().orElse(null);
		if (entranceRR == null)
			throw new RuntimeException("Couldn't find RR overlapping entrance");

		hallway.addSubRect(entranceRR);
		hallway.addImportantRect(entrance);
		return hallway;
	}
}
