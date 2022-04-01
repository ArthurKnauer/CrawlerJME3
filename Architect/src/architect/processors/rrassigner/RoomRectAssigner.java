/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package architect.processors.rrassigner;

import architect.Constants;
import architect.processors.FloorPlanProcessor;
import architect.room.Room;
import architect.room.RoomRect;
import architect.room.RoomType;
import architect.utils.FloatComparable;
import architect.utils.FloatComparator;
import java.util.Collection;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author VTPlusAKnauer
 */
public class RoomRectAssigner extends FloorPlanProcessor {

	private PriorityQueue<FloatComparable<RoomWithClaim>> roomQueue;
	private Set<RoomRectClaim> claimableRoomRects;

	@Override
	protected void process() {
		assignRoomRectsOverlappingOptimalRectsToRooms(fp.roomRects, fp.rooms);

		claimableRoomRects = collectClaimableRoomRects(fp.roomRects);
		if (!claimableRoomRects.isEmpty()) {
			Set<RoomWithClaim> roomsWithClaim = collectRoomsWithClaim(claimableRoomRects);
			prepareRoomsToClaimArea(roomsWithClaim, claimableRoomRects);

			for (RoomWithClaim roomWithClaim : roomsWithClaim) {
				roomWithClaim.updateAllClaims();
			}
			roomQueue = FloatComparator.createPriorityQueue(roomsWithClaim, rwc -> 1.0f / rwc.room.areaToNeedRatio());

			if (!Constants.DEV_MODE_ROOMRECTASSIGNER_STEPS) {
				while (!roomQueue.isEmpty()) {
					addBestRR();
				}
			}
		}
	}

	private void assignRoomRectsOverlappingOptimalRectsToRooms(Collection<RoomRect> roomRects, Collection<Room> rooms) {
		roomRects.stream().filter(rr -> !rr.isAssigned()).forEach((rr) -> {
			Room bestRoomToJoin = findRoomWithMaxOptimalRectOverlapFor(rooms, rr);
			if (bestRoomToJoin != null)
				bestRoomToJoin.addSubRect(rr);
		});
	}

	private Room findRoomWithMaxOptimalRectOverlapFor(Collection<Room> rooms, RoomRect rr) {
		Room bestRoomToJoin = null;
		float minAreaOverlap = rr.area() * 0.25f;
		for (Room room : rooms) {
			if (room.getOptimalRect().isValid() && rr.neighborAssignedTo(room)) {
				float areaOverlap =room.getOptimalRect().rect.overlapArea(rr);
				if (areaOverlap > minAreaOverlap) {
					minAreaOverlap = areaOverlap;
					bestRoomToJoin = room;
				}
			}
		}
		return bestRoomToJoin;
	}

	private Set<RoomRectClaim> collectClaimableRoomRects(Collection<RoomRect> roomRects) {
		return roomRects.stream().filter(rr -> !rr.isAssigned())
				.map(rr -> new RoomRectClaim(rr))
				.collect(Collectors.toSet());
	}

	private Set<RoomWithClaim> collectRoomsWithClaim(Set<RoomRectClaim> claimableRoomRects) {
		return claimableRoomRects.stream()
				.flatMap(crr -> crr.rr.neighborsMatching(RoomRect::isAssigned))
				.map(rr -> rr.assignedTo().get()).distinct()
				.filter(room -> room.type() != RoomType.Hallway)
				.map(RoomWithClaim::new).collect(Collectors.toSet());
	}

	private void prepareRoomsToClaimArea(Set<RoomWithClaim> roomsWithClaim,
										 Set<RoomRectClaim> claimableRoomRects) {
		roomsWithClaim.stream().forEach(rwc -> claimableRoomRects.stream()
				.filter(crr -> crr.rr.neighborAssignedTo(rwc.room))
				.forEach(crr -> rwc.prepareToClaim(crr)));
	}

	public void addBestRR() {
		if (roomQueue.isEmpty())
			return;

		FloatComparable<RoomWithClaim> comparableRoom = roomQueue.poll();
		RoomWithClaim roomWithClaim = comparableRoom.obj;

		if (roomWithClaim.claimedRoomRects().isEmpty())
			return; // all claimed RRs were taken before my turn

		RoomRectClaim bestClaimedRR = findBestClaimedRRFor(roomWithClaim);

		if (bestClaimedRR != null) {
			if (!claimableRoomRects.remove(bestClaimedRR))
				throw new RuntimeException("RoomRectClaim " + bestClaimedRR + " doesn't exist in claimableRoomRects");

			roomWithClaim.takeClaimedRoomRect(bestClaimedRR, claimableRoomRects);
			// can takeClaimedRoomRect more roomrects -> put back into queue with new key
			if (!roomWithClaim.claimedRoomRects().isEmpty()) {
				comparableRoom.setValue(1.0f / roomWithClaim.room.areaToNeedRatio());
				roomQueue.add(comparableRoom);
			}
		}
		else { // no max claim -> try this room again soon
			comparableRoom.setValue(roomQueue.peek().value() * 0.9f);
			roomQueue.add(comparableRoom);
		}
	}

	private RoomRectClaim findBestClaimedRRFor(RoomWithClaim roomWithClaim) {
		float bestScore = 0;
		RoomRectClaim bestClaimedRR = null;
		for (RoomRectClaim claimedRR : roomWithClaim.claimedRoomRects()) {
			TwoHighestClaimers twoHighestClaimers = claimedRR.twoHighestClaimers();
			// if my claim is highest
			if (twoHighestClaimers.highestClaimer.obj == roomWithClaim) {
				float claimValue = twoHighestClaimers.highestClaimer.value();
				float secondBestClaim = twoHighestClaimers.secondHighestClaimer != null
										? twoHighestClaimers.secondHighestClaimer.value() : 0;
				float score = claimValue - secondBestClaim; // the bigger the "domination" the better the rr
				if (score > bestScore) {
					bestClaimedRR = claimedRR;
					bestScore = score;
				}
			}
		}

		return bestClaimedRR;
	}
}
