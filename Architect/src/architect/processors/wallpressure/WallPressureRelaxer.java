/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package architect.processors.wallpressure;

import architect.Constants;
import architect.math.segments.Interval;
import architect.math.Rectangle;
import architect.math.segments.Side;
import architect.processors.FloorPlanProcessor;
import architect.processors.helpers.Cutter;
import architect.processors.helpers.Deisolator;
import architect.processors.helpers.Simplifier;
import architect.room.Room;
import architect.room.RoomRect;
import static architect.utils.ConstructionFlags.Flag.DELETED;
import architect.utils.FloatComparable;
import architect.utils.FloatComparator;
import architect.walls.RoomRectWall;
import architect.walls.WallType;
import static java.lang.Math.max;
import static java.lang.Math.min;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.java.Log;


/**
 *
 * @author VTPlusAKnauer
 */
@Log
public class WallPressureRelaxer extends FloorPlanProcessor {
	
	private PriorityQueue<FloatComparable<Room>> roomQueue;

	private LinkedList<CutRect> appliedFixCutters;

	@Override
	public void process() {
		roomQueue = sortRoomsForRelaxing(fp.rooms);
		appliedFixCutters = new LinkedList<>();

		if (!Constants.DEV_MODE_WALLPRESSURERELAXER_STEPS) {
			while (!roomQueue.isEmpty()) {
				relaxRoomOutwardPressures();
			}

			Deisolator.resolveRoomRectIsolation(fp.rooms);
			Simplifier.simplifyAll(fp);

			for (Room room : fp.rooms) {
				if (room.subRects().isEmpty()) {
					throw new RuntimeException("Room " + room + " was destroyed by wall pressure cuts");
				}
			}
		}
	}

	private PriorityQueue<FloatComparable<Room>> sortRoomsForRelaxing(Collection<Room> rooms) {
		return FloatComparator.createPriorityQueue(
				rooms.stream().filter(room -> room.getOptimalRect().isValid()),
				room -> areaOverlapRatioWithPredictedPressureCutters(room));
	}

	private float areaOverlapRatioWithPredictedPressureCutters(Room room) {
		return (float) room.getWallLoop().stream()
				.filter(wall -> wall.type == WallType.INTERNAL && wall.canOptimalPosCut(fp.attribs))
				.mapToDouble(wall -> room.overlapArea(wall.optimalPosRect()))
				.sum() / room.area();
	}

	public void relaxRoomOutwardPressures() {
		if (roomQueue.isEmpty())
			return;
		Room room = roomQueue.poll().obj;

		LinkedList<LinkedList<RoomRectWall>> wallChains = WallChainBuilder.forRoom(room, fp, appliedFixCutters);
		List<CutRect> cutRects = CutRectBuilder.fromOptimalPosOnWallChains(room, wallChains, fp);

		if (!cutRects.isEmpty()) {
			cutRects = removeOverlappingCutters(cutRects);
			trimCuttersToNotOverExtendRoom(room, cutRects);
			alignCutRectsAtCorners(cutRects);
			trimCuttersToNotOverlap(fp.floorPlanPoly.entrance, cutRects);

			cutAreaForRoom(room, cutRects);
		}
	}

	private List<CutRect> removeOverlappingCutters(List<CutRect> cutRects) {
		for (ListIterator<CutRect> outer = cutRects.listIterator(); outer.hasNext();) {
			CutRect cutterA = outer.next();
			for (ListIterator<CutRect> inner = cutRects.listIterator(outer.nextIndex()); inner.hasNext();) {
				CutRect cutterB = inner.next();
				if (cutterA.getRect().overlaps(cutterB.getRect())) {
					if (cutterB.getRect().area() < cutterA.getRect().area())
						cutterB.flags.add(DELETED);
					else
						cutterA.flags.add(DELETED);
				}
			}
		}

		return cutRects.stream().filter(rect -> !rect.flags.contains(DELETED))
				.collect(Collectors.toList());
	}

	private void trimCuttersToNotOverExtendRoom(Room room, List<CutRect> cutRects) {
		for (CutRect cutter : cutRects) {
			if (cutter.isHorizontal()) {
				Interval range = room.getHorizontalInterval(
						cutter.ownerEdge == Side.Bottom ? cutter.getRect().minY : cutter.getRect().maxY, cutter.getRect().minX, cutter.getRect().maxX);
				if (!range.isValid())
					throw new RuntimeException("room.getHorizontalInterval invalid: " + room);
				float newMinX = max(cutter.getRect().minX, range.min);
				cutter.setRect(new Rectangle(newMinX, cutter.getRect().minY, min(cutter.getRect().maxX, range.max) - newMinX, cutter.getRect().height));
			}
			else {
				Interval range = room.getVerticalInterval(
						cutter.ownerEdge == Side.Left ? cutter.getRect().minX : cutter.getRect().maxX, cutter.getRect().minY, cutter.getRect().maxY);
				if (!range.isValid())
					throw new RuntimeException("room.getVerticalInterval invalid: " + room);

				float newMinY = max(cutter.getRect().minY, range.min);
				cutter.setRect(new Rectangle(cutter.getRect().minX, newMinY, cutter.getRect().width, min(cutter.getRect().maxY, range.max) - newMinY));
			}
		}
	}

	private void alignCutRectsAtCorners(List<CutRect> cutRects) {
		for (CutRect cutterA : cutRects) {
			for (CutRect cutterB : cutRects) {
				if (cutterA != cutterB && cutterA.getRect().isTouchingOnlyCorner(cutterB.getRect())) {
					if (cutterA.isHorizontal() && cutterB.isVertical())
						cutterA.setRect(cutterA.getRect().scaledToOverlapEntireWidth(cutterB.getRect()));
					else if (cutterA.isVertical() && cutterB.isHorizontal())
						cutterB.setRect(cutterB.getRect().scaledToOverlapEntireWidth(cutterA.getRect()));
				}
			}
		}
	}

	private void trimCuttersToNotOverlap(Rectangle rectToNotOverlap, List<CutRect> cutRects) {
		for (CutRect cutRect : cutRects) {
			cutRect.setRect(cutRect.getRect().trimmedLeastToNotOverlap(rectToNotOverlap));
		}
	}

	private void cutAreaForRoom(Room room, List<CutRect> cutRects) {
		List<Rectangle> rects = cutRects.stream().map(cutRect -> cutRect.getRect()).collect(Collectors.toList());

		ArrayList<RoomRect> insideCutters = Cutter.cut(fp, rects);
		room.addAllSubRects(insideCutters);

		appliedFixCutters.addAll(cutRects);
	}
}
