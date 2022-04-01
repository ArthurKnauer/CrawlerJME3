/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package architect.processors;

import architect.math.segments.Side;
import architect.processors.helpers.OptimalRectFinder;
import architect.room.Room;
import architect.room.RoomRect;
import architect.room.RoomType;
import architect.utils.FloatComparable;
import architect.utils.FloatComparator;
import architect.utils.UniqueID;
import architect.walls.WallSide;
import architect.walls.WallType;
import static java.lang.Math.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

/**
 *
 * @author VTPlusAKnauer
 */
public class ProtoRoomAssigner extends FloorPlanProcessor {

	private HashMap<WallSide, Integer> maxRoomsOnWall;
	private HashMap<WallSide, Integer> roomsOnWall;

	private final FloatComparator<RoomRect> roomRectComparator = new FloatComparator<>();

	@Override
	protected void process() {
		maxRoomsOnWall = new HashMap<>(fp.floorPlanPoly.edgeLoop().size());
		roomsOnWall = new HashMap<>(fp.floorPlanPoly.edgeLoop().size());
		calcMaxRoomPerWall();

		buildWindowedRooms();

		OptimalRectFinder.findAll(fp);

		buildWindowlessRooms();
	}

	private void calcMaxRoomPerWall() {
		double averageArea = fp.roomsToBuild().mapToDouble(stats -> stats.needsArea)
				.average().orElse(1);
		float averageRoomLength = (float) sqrt(averageArea);
		for (WallSide wall : fp.floorPlanPoly.edgeLoop()) {
			roomsOnWall.put(wall, 0);
			maxRoomsOnWall.put(wall, (int) (wall.length() / averageRoomLength + 0.5f));
		}
	}

	private void buildWindowedRooms() {
		PriorityQueue<FloatComparable<RoomRect>> windowedRRQueue = buildWindowedRRQueue();
		int windowedRooms = fp.roomsToBuildCount(stats -> stats.needsWindow);
		for (int r = 0; r < windowedRooms && windowedRRQueue.size() > 0; r++) {
			buildWindowedRoom(windowedRRQueue);
		}
	}

	private PriorityQueue<FloatComparable<RoomRect>> buildWindowedRRQueue() {
		return FloatComparator.createPriorityQueue(
				fp.roomRects.stream().filter(rr -> !rr.isAssigned() && rr.windowableOuterWallSides > 0),
				rr -> windowableRRScore(rr));
	}

	private void buildWindowlessRooms() {
		PriorityQueue<FloatComparable<RoomRect>> windowlessRRQueue = buildWindowlessRRQueue();

		while (fp.roomsToBuild.list().contains(RoomType.Bathroom) && windowlessRRQueue.size() > 0) {
			Room bath = new Room(RoomType.Bathroom, fp.roomStats.get(RoomType.Bathroom), "br", fp.attribs);
			RoomRect rr = windowlessRRQueue.poll().obj;
			bath.addSubRect(rr);
			fp.rooms.add(bath);
			fp.roomsToBuild.list().remove(RoomType.Bathroom);
		}
	}

	private PriorityQueue<FloatComparable<RoomRect>> buildWindowlessRRQueue() {
		return FloatComparator.createPriorityQueue(
				fp.roomRects.stream().filter(rr -> !rr.isAssigned()),
				rr -> windowlessRRScore(rr));
	}

	private void buildWindowedRoom(PriorityQueue<FloatComparable<RoomRect>> windowedRRQueue) {
		if (!windowedRRQueue.isEmpty()) {
			Room room = new Room(RoomType.None, fp.roomStats.get(RoomType.None), "" + UniqueID.nextChar(Room.class), fp.attribs);

			RoomRect rr = findBestFreeWindowableRR(windowedRRQueue);
			room.addSubRect(rr);
			fp.rooms.add(room);

			incrementRoomsOnWallsTouching(rr);
			rebuildWindowableRRQueue(windowedRRQueue);
		}
	}

	private RoomRect findBestFreeWindowableRR(PriorityQueue<FloatComparable<RoomRect>> windowedRRQueue) {
		FloatComparable<RoomRect> comparableRR = windowedRRQueue.poll();
		RoomRect rr = comparableRR.obj;

		PriorityQueue<FloatComparable<RoomRect>> windowableRRQueueSkipped = new PriorityQueue<>();

		while (isRROnOnlyOvercrowdedWalls(rr)) {
			windowableRRQueueSkipped.add(comparableRR);
			if (windowedRRQueue.isEmpty()) {
				windowedRRQueue.addAll(windowableRRQueueSkipped);
				windowableRRQueueSkipped.clear();

				incrementMaxRoomsOnWall();
			}
			rr = windowedRRQueue.poll().obj;
		}
		windowedRRQueue.addAll(windowableRRQueueSkipped);

		return rr;
	}

	private boolean isRROnOnlyOvercrowdedWalls(RoomRect rr) {
		for (Side side : Side.values()) {
			if (rr.windowablePerimeter(side) > 0) {
				for (WallSide wall : fp.floorPlanPoly.edgesTouching(rr.sideLineSegment(side))) {
					if (roomsOnWall.get(wall) < maxRoomsOnWall.get(wall))
						return false;
				}
			}
		}

		return true;
	}

	private void incrementMaxRoomsOnWall() {
		for (WallSide wall : maxRoomsOnWall.keySet()) {
			if (wall.type == WallType.OUTER_WINDOWABLE)
				maxRoomsOnWall.put(wall, maxRoomsOnWall.get(wall) + 1);
		}
	}

	private void incrementRoomsOnWallsTouching(RoomRect rr) {
		for (Side side : Side.values()) {
			if (rr.windowablePerimeter(side) > 0) {
				for (WallSide wall : fp.floorPlanPoly.edgesTouching(rr.sideLineSegment(side))) {
					roomsOnWall.put(wall, roomsOnWall.get(wall) + 1);
				}
			}
		}
	}

	private void rebuildWindowableRRQueue(PriorityQueue<FloatComparable<RoomRect>> windowedRRQueue) {
		if (!windowedRRQueue.isEmpty()) {
			ArrayList<FloatComparable<RoomRect>> rescoredRRs = new ArrayList<>(windowedRRQueue.size());

			for (FloatComparable<RoomRect> crr : windowedRRQueue) {
				if (!crr.obj.isAssigned()) {
					crr.setValue(windowableRRScore(crr.obj));
					rescoredRRs.add(crr);
				}
			}

			windowedRRQueue.clear();
			windowedRRQueue.addAll(rescoredRRs);
		}
	}

	private float windowableRRScore(RoomRect rr) {
		float roomOptimalArea = fp.roomStats.get(RoomType.Bedroom).needsArea;
		float ratio = rr.inverseAspectRatio();
		if (rr.area() < roomOptimalArea * 0.66f)
			ratio = rr.bestRatioWithFreeNeighbor();

		float freeNeighborScore = min(2, rr.getFreeNeighbors()) + 1;
		if (rr.area() < roomOptimalArea * 0.5f && freeNeighborScore == 1)
			freeNeighborScore = 0;

		//TODO: add ratio? windowableOuterWallSides is most important (corners)
		float score = rr.windowableOuterWallSides * rr.area() * freeNeighborScore;// * ratio * ratio;

		return score;
	}

	private float windowlessRRScore(RoomRect rr) {
		float freeArea = rr.area();
		for (Room room : fp.rooms) { // the more other rooms want a piece of this rr the less is its score
			if (room.getOptimalRect().isValid())
				freeArea -= room.getOptimalRect().rect.overlapArea(rr);
		}
		float score = freeArea * (float) pow(rr.windowlessOuterWallSides + 1, 0.2);
		return score;
	}
}
