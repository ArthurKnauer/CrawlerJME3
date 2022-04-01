/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architect.processors.protrusion;

import architect.floorplan.FloorPlan;
import architect.math.segments.LineSegment;
import architect.math.Rectangle;
import architect.math.segments.Side;
import architect.room.Room;
import architect.room.RoomRect;
import architect.utils.FloatComparable;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

class ProtrusionEnlarger {

	final RoomRect rr;
	final Room takerRoom;
	final Rectangle enlargerRect;

	ProtrusionEnlarger(RoomRect rr, Room takerRoom) {
		this.rr = rr;
		this.takerRoom = takerRoom;
		this.enlargerRect = enlargerRect(rr, takerRoom);
	}

	private static Rectangle enlargerRect(RoomRect rr, Room takerRoom) {
		LineSegment roomEdge = roomEdgeIntervalTouching(takerRoom, rr);
		if (roomEdge.isVertical())
			return new Rectangle(rr.minX, roomEdge.min, rr.width, roomEdge.length());
		else
			return new Rectangle(roomEdge.min, rr.minY, roomEdge.length(), rr.height);
	}

	// TODO: move to room
	private static LineSegment roomEdgeIntervalTouching(Room room, RoomRect rr) {
		EnumSet<Side> attachedSides = rr.sidesAttachedTo(room);
		if (attachedSides.size() != 1)
			throw new RuntimeException("Only one side must be attached to room " + room
									   + ", if protrusion " + rr + " is to be enlarged, instead attached sides: "
									   + attachedSides.size());

		Side sideAttachedToTaker = attachedSides.iterator().next();
		LineSegment lineTouchingTaker = rr.sideLineSegment(sideAttachedToTaker);
		return room.getRoomInterval(sideAttachedToTaker.opposite(), lineTouchingTaker);
	}

	static Optional<ProtrusionEnlarger> findBestEnlarger(RoomRect rr, List<Room> bestRooms, FloorPlan fp) {
		return bestRooms.stream()
				.map(room -> comparableEnlarger(rr, room, fp))
				.sorted().map(comparable -> comparable.obj)
				.findFirst();
	}

	static FloatComparable<ProtrusionEnlarger> comparableEnlarger(RoomRect rr, Room takerRoom, FloorPlan fp) {
		ProtrusionEnlarger enlarger = new ProtrusionEnlarger(rr, takerRoom);
		return new FloatComparable<>(enlarger, enlargerScore(enlarger.enlargerRect, fp));
	}

	private static float enlargerScore(Rectangle enlarger, FloorPlan fp) {
		for (Room room : fp.rooms) {
			if (room.importantRectsOverlap(enlarger))
				return 0;
		}
		return 1 / enlarger.area();
	}
}
