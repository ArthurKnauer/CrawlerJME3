/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package architect.processors.protrusion;

import architect.Constants;
import static architect.Constants.EPSILON;
import architect.processors.FloorPlanProcessor;
import architect.processors.helpers.BestRoomForRoomRect;
import architect.processors.helpers.Cutter;
import architect.processors.helpers.Deisolator;
import architect.room.Room;
import architect.room.RoomRect;
import static architect.room.RoomStatus.Flags.ProtrusionsRemoved;
import architect.utils.FloatComparable;
import architect.utils.FloatComparator;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author VTPlusAKnauer
 */
public class ProtrusionRemover extends FloorPlanProcessor {

	private PriorityQueue<FloatComparable<Room>> roomQueue;
	private HashSet<ProtrusionHistoryHash> protrusionHistory;
	private HashSet<Room> affectedRooms;

	@Override
	protected void process() {
		if (fp.rooms.stream().anyMatch(room -> !room.status.contains(ProtrusionsRemoved))) {
			roomQueue = FloatComparator.createPriorityQueue(fp.rooms, Room::area);
			protrusionHistory = new HashSet<>();
			affectedRooms = new HashSet<>();

			if (!Constants.DEV_MODE_PROTRUSIONREMOVER_STEPS) {
				fixAllRooms();
			}

			for (Room room : fp.rooms) {
				room.status.add(ProtrusionsRemoved);
			}
		}
	}

	private void fixAllRooms() {
		int roomsFixed = 0;
		while (!roomQueue.isEmpty()) {
			removeProtrusionsStep();

			if (roomsFixed++ > 100)
				throw new RuntimeException("ProtrusionRemover possible infinite loop (fixed rooms > 100): " + roomQueue);
		}
	}

	public void removeProtrusionsStep() {
		if (!roomQueue.isEmpty()) {
			Room roomToFix = roomQueue.poll().obj;

			affectedRooms.clear();
			fixSmallestProtrusion(roomToFix);
			affectedRooms.addAll(Deisolator.resolveRoomRectIsolation(roomToFix));

			// TODO: dont add room, multiple times -> check flag?
			for (Room room : affectedRooms) {
				roomQueue.add(new FloatComparable<>(room, room.area()));
			}
		}
	}

	private void fixSmallestProtrusion(Room room) {
		if (room.subRects().size() > 1) {
			room.updateMaxProtrusionSize();

			Protrusion protrusion = ProtrusionFinder.smallestProtrusion(room, protrusionHistory, fp);
			if (protrusion != null)
				fixProtrusion(protrusion);
		}
	}

	private void fixProtrusion(Protrusion protrusion) {
		Room room = protrusion.rr.assignedTo().get();
		float overlapWithImportantRects = room.areaOverlapWithImportantRects(protrusion.isolatingRect);
		if (overlapWithImportantRects < 0.5f) {
			RoomRect rrToGive = protrusion.isolateRoomRect(fp);

			if (protrusion instanceof ZigZagProtrusion)
				giveToRoom(rrToGive, ((ZigZagProtrusion) protrusion).enveloperRoom);
			else
				findBestRoomAndGive(rrToGive);
			affectedRooms.add(room);
		}
	}

	private void findBestRoomAndGive(RoomRect rrToGive) {
		List<Room> bestRooms = BestRoomForRoomRect.findBestRoomFor(rrToGive);
		if (bestRooms.size() > 1) {
			findBestEnlargerAndGive(rrToGive, bestRooms);
		}
		else {
			giveToRoom(rrToGive, bestRooms.get(0));
		}
	}

	//TODO: why do giveToRoom and enlargeAndGive exist separately?
	private void giveToRoom(RoomRect rrToGive, Room taker) {
		if (!rrToGive.isAssignedTo(taker)) {
			taker.addSubRect(rrToGive);
			affectedRooms.add(taker);
		}
		else {
			FloatComparable<ProtrusionEnlarger> comparableEnlarger
												= ProtrusionEnlarger.comparableEnlarger(rrToGive, taker, fp);
			if (comparableEnlarger.value() > EPSILON)
				enlargeAndGive(comparableEnlarger.obj);
		}
	}

	private void findBestEnlargerAndGive(RoomRect rrToGive, List<Room> bestRooms) {
		ProtrusionEnlarger.findBestEnlarger(rrToGive, bestRooms, fp)
				.ifPresent(this::enlargeAndGive);
	}

	// TODO: instead of collecting affected rooms, flag each room with add / remove rr
	private void enlargeAndGive(ProtrusionEnlarger enlarger) {
		ArrayList<RoomRect> insideCutter = Cutter.cut(fp, enlarger.enlargerRect);
		if (!insideCutter.isEmpty()) {
			Set<Room> affected = insideCutter.stream().filter(RoomRect::isAssigned)
					.map(rr -> rr.assignedTo().get())
					.collect(Collectors.toSet());
			affectedRooms.addAll(affected);

			enlarger.takerRoom.addAllSubRects(insideCutter);
		}

		enlarger.takerRoom.addSubRect(enlarger.rr);
	}
}
