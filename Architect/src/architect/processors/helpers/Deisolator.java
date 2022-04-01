/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architect.processors.helpers;

import architect.math.Rectangle;
import architect.room.Room;
import architect.room.RoomRect;
import static architect.utils.ConstructionFlags.Flag.*;
import java.util.*;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;

/**
 * Helper class for processors to look for and remove isolated RoomRects, which can appear after cutting
 *
 * @author VTPlusAKnauer
 */
public final class Deisolator {

	private final static Logger LOGGER = getLogger(Deisolator.class.getName());

	private Deisolator() { // static methods only		
	}

	/**
	 * Checks for isolated RRs in a given set of rooms
	 *
	 * @param rooms
	 * @return a set of rooms which adopted the isolated RRs
	 */
	public static Set<Room> resolveRoomRectIsolation(Set<Room> rooms) {
		HashSet<Room> grownRooms = new HashSet<>();

		for (Room room : rooms) {
			grownRooms.addAll(resolveRoomRectIsolation(room));
		}

		return grownRooms;
	}

	/**
	 * Checks for isolated RRs in a given room
	 *
	 * @param room
	 * @return a set of rooms which adopted the isolated RRs
	 */
	public static Set<Room> resolveRoomRectIsolation(Room room) {
		HashSet<Room> grownRooms = new HashSet<>();
		List<RoomRect> isolated = removeIsolatedRects(room);
		if (isolated != null && isolated.size() > 0) {
			boolean foundRoomsForAllRRs = false;
			for (int t = 0; t < 100 && !foundRoomsForAllRRs; t++) {
				foundRoomsForAllRRs = true;
				for (RoomRect rr : isolated) {
					if (rr.isAssigned())
						continue;
					List<Room> bestRooms = BestRoomForRoomRect.findBestRoomFor(rr);
					if (!bestRooms.isEmpty()) {
						Room bestRoom = bestRooms.get(0);
						bestRoom.addSubRect(rr);
						grownRooms.add(bestRoom);
					}
					else {
						foundRoomsForAllRRs = false;
					}
				}

				if (t >= 99)
					throw new RuntimeException("Can't find rooms for all isolated roomrects.");
			}
		}

		return grownRooms;
	}

	/**
	 * Finds and removes isolated RRs of a room, the bigger RR island stays as the RRs of the room
	 *
	 * @return list of RRs that are isolated and were removed
	 */
	private static List<RoomRect> removeIsolatedRects(Room room) {
		Set<RoomRect> roomRects = room.subRects();
		if (roomRects.isEmpty()) {
			return null;
		}

		ArrayList<ArrayList<RoomRect>> islands = new ArrayList<>();
		ArrayList<Float> islandSizes = new ArrayList<>();
		// reset all rooms as "uncharted"
		for (RoomRect rr : roomRects) {
			rr.flags.remove(PROCESSED);
		}

		// first starting rect will make first island of roomrects
		RoomRect start = roomRects.iterator().next();

		while (start != null) {
			Stack<RoomRect> stack = new Stack<>();
			start.flags.add(PROCESSED);
			stack.push(start);
			ArrayList<RoomRect> island = new ArrayList<>();
			float islandSize = 0;

			// findBestRoomFor all neighbors and neighbors' neighbors, etc.. and put them into one island
			while (!stack.isEmpty()) {
				RoomRect rr = stack.pop();
				island.add(rr);
				islandSize += rr.area();
				for (RoomRect neighbor : rr.neighbors()) {
					if (neighbor.isAssignedTo(room) && !neighbor.flags.contains(PROCESSED)) {
						neighbor.flags.add(PROCESSED);
						stack.push(neighbor);
					}
				}
			}

			islands.add(island); // no more neighbors
			islandSizes.add(islandSize);
			start = null;

			// findBestRoomFor an untouched rect -> new island
			for (RoomRect rr : roomRects) {
				if (!rr.flags.contains(PROCESSED)) {
					start = rr;
					break;
				}
			}
		}

		if (islands.size() < 2) { // no isolated roomrect islands or no roomrects at all
			return null;
		}
		else {
			if (room.importantRects().isEmpty()) {
				// findBestRoomFor biggest island which will be the room, or an island with important rects
				float biggestIslandSize = -1;
				ArrayList<RoomRect> biggestIsland = null;
				for (int i = 0; i < islandSizes.size(); i++) {
					if (islandSizes.get(i) > biggestIslandSize) {
						biggestIslandSize = islandSizes.get(i);
						biggestIsland = islands.get(i);
					}
				}

				// collect the smaller islands into one package
				ArrayList<RoomRect> isolated = new ArrayList<>();
				for (ArrayList<RoomRect> island : islands) {
					if (island != biggestIsland) {
						isolated.addAll(island);
					}
				}

				// remove all the isolated rrs from this room
				for (RoomRect rr : isolated) {
					room.removeSubRect(rr);
				}

				return isolated;
			}
			else { // have important rects, throw away rr islands which dont intersect them
				// collect the smaller islands into one package
				ArrayList<RoomRect> isolated = new ArrayList<>();
				for (ArrayList<RoomRect> island : islands) {
					if (!Rectangle.rectanglesOverlap(island, room.importantRects())) {
						isolated.addAll(island);
					}
				}

				// remove all the isolated rrs from this room
				for (RoomRect rr : isolated) {
					room.removeSubRect(rr);
				}

				return isolated;
			}
		}
	}
}
