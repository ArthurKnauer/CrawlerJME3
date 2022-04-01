/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architecttest.analyze;

import architect.floorplan.FloorPlan;
import architect.math.segments.Side;
import architect.room.Room;
import architect.room.RoomRect;
import java.util.ArrayList;

/**
 *
 * @author VTPlusAKnauer
 */
public class Validator {

	public static class BadRelation<A, B> {

		public final A a;
		public final B b;

		public BadRelation(A a, B b) {
			this.a = a;
			this.b = b;
		}
	}

	public static ArrayList<BadRelation<RoomRect, RoomRect>> rrMissingRRNeighbor = new ArrayList<>();
	public static ArrayList<BadRelation<RoomRect, RoomRect>> rrsShouldNotBeNeighbors = new ArrayList<>();
	public static ArrayList<BadRelation<RoomRect, RoomRect>> rrsShouldBeNeighbors = new ArrayList<>();
	public static ArrayList<BadRelation<Room, RoomRect>> roomMissingRR = new ArrayList<>();
	public static ArrayList<BadRelation<Room, RoomRect>> rrAssignedToWrongRoom = new ArrayList<>();
	public static ArrayList<BadRelation<Room, Room>> roomsShouldBeNeighbors = new ArrayList<>();
	public static ArrayList<BadRelation<Room, Room>> roomsShouldNotBeNeighbors = new ArrayList<>();
	public static ArrayList<BadRelation<RoomRect, Side>> unsnappedSides = new ArrayList<>();

	/**
	 * Checks for bad variables (relations) between RoomRects and Rooms
	 *
	 * @param fp holds RoomRects and Rooms to check
	 * @return true if errors found
	 */
	public static boolean findErrors(FloorPlan fp) {
		rrMissingRRNeighbor.clear();
		rrsShouldNotBeNeighbors.clear();
		rrsShouldBeNeighbors.clear();
		roomMissingRR.clear();
		rrAssignedToWrongRoom.clear();
		roomsShouldBeNeighbors.clear();
		roomsShouldNotBeNeighbors.clear();
		unsnappedSides.clear();

		for (RoomRect rr : fp.roomRects) {
			if (!fp.snapGrid.isXSnapped(rr.minX))
				unsnappedSides.add(new BadRelation<>(rr, Side.Left));
			if (!fp.snapGrid.isXSnapped(rr.maxX))
				unsnappedSides.add(new BadRelation<>(rr, Side.Right));
			if (!fp.snapGrid.isYSnapped(rr.minY))
				unsnappedSides.add(new BadRelation<>(rr, Side.Bottom));
			if (!fp.snapGrid.isYSnapped(rr.maxY))
				unsnappedSides.add(new BadRelation<>(rr, Side.Top));

			for (RoomRect neighbor : rr.neighbors()) {
				if (!fp.roomRects.contains(neighbor)) {
					rrMissingRRNeighbor.add(new BadRelation<>(rr, neighbor));
				}
				else if (!rr.canBeNeighbors(neighbor, fp.attribs.minOverLapForNeighbor)) {
					rrsShouldNotBeNeighbors.add(new BadRelation<>(rr, neighbor));
				}
			}

			for (RoomRect rr2 : fp.roomRects) { // check if should be neighbors
				if (rr2 != rr && !rr.neighbors().contains(rr2) && rr.canBeNeighbors(rr2, fp.attribs.minOverLapForNeighbor)) {
					rrsShouldBeNeighbors.add(new BadRelation<>(rr, rr2));
				}
			}

			if (rr.isAssigned() && !rr.assignedTo().get().subRects().contains(rr)) {
				rrAssignedToWrongRoom.add(new BadRelation<>(rr.assignedTo().get(), rr));
			}
		}

		for (Room room : fp.rooms) {
			for (RoomRect rr : room.subRects()) {
				if (!fp.roomRects.contains(rr)) {
					roomMissingRR.add(new BadRelation<>(room, rr));
				}

				if (!rr.isAssignedTo(room)) {
					rrAssignedToWrongRoom.add(new BadRelation<>(room, rr));
				}
			}
		}

		for (Room room1 : fp.rooms) {
			for (Room room2 : fp.rooms) {
				if (room1 != room2) {
					boolean neighborsByRoomRects = false;
					for (RoomRect rr : room1.subRects()) {
						if (rr.neighborAssignedTo(room2)) {
							neighborsByRoomRects = true;
							break;
						}
					}

					if (room1.neighbors().contains(room2) && !neighborsByRoomRects)
						roomsShouldNotBeNeighbors.add(new BadRelation<>(room1, room2));
					else if (!room1.neighbors().contains(room2) && neighborsByRoomRects)
						roomsShouldBeNeighbors.add(new BadRelation<>(room1, room2));
				}
			}
		}

		return !unsnappedSides.isEmpty() || !roomsShouldBeNeighbors.isEmpty() || !roomsShouldNotBeNeighbors.isEmpty()
			   || !rrMissingRRNeighbor.isEmpty() || !rrsShouldNotBeNeighbors.isEmpty()
			   || !rrsShouldBeNeighbors.isEmpty() || !roomMissingRR.isEmpty() || !rrAssignedToWrongRoom.isEmpty();
	}
}
