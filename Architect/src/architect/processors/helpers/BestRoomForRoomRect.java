/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architect.processors.helpers;

import static architect.Constants.EPSILON;
import architect.math.segments.Interval;
import architect.math.Rectangle;
import architect.math.Vector2D;
import architect.room.Room;
import architect.room.RoomPerimeter;
import architect.room.RoomRect;
import architect.room.SideOverlap;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 *
 * @author VTPlusAKnauer
 */
public final class BestRoomForRoomRect {

	private BestRoomForRoomRect() {
	}

	public static List<Room> findBestRoomFor(RoomRect rr) {
		Optional<Room> bestRoom = rr.assignedTo().filter(room -> room.importantRectsOverlap(rr));

		if (!bestRoom.isPresent()) {
			List<RoomPerimeter> roomPerimeters = rr.roomPerimeters();
			bestRoom = findRoomWithMaxMinOverlapGreaterZero(roomPerimeters);

			if (!bestRoom.isPresent())
				bestRoom = findRoomWithMaxSufficientOverlapScore(rr, roomPerimeters);

			if (!bestRoom.isPresent()) {
				ArrayList<Room> bestRooms = new ArrayList<>();
				if (rr.isAssigned()) {
					bestRooms.addAll(filterRoomPerimeters(roomPerimeters, rectIsNoProtrusionForRoom(rr)));
					if (bestRooms.isEmpty())
						bestRooms.add(rr.assignedTo().get());
				}
				else
					bestRooms.addAll(filterRoomPerimeters(roomPerimeters, roomOverlapsEntireWidthOrHeight(rr)));

				return bestRooms;
			}
		}

		return bestRoom.map(Collections::singletonList).orElseThrow(RuntimeException::new);
	}

	private static Optional<Room> findRoomWithMaxMinOverlapGreaterZero(List<RoomPerimeter> roomPerimeters) {
		return roomPerimeters.stream()
				.filter(rp -> rp.perimeter.minElement() > EPSILON)
				.max(Comparator.comparing(rp -> rp.perimeter.minElement()))
				.map(rp -> rp.room);
	}

	private static Optional<Room> findRoomWithMaxSufficientOverlapScore(RoomRect rr, List<RoomPerimeter> roomPerimeters) {
		float minFitScore = 0.1f;
		return roomPerimeters.stream().map(rp -> rp.room)
				.filter(room -> roomWallOverlapScore(room, rr) > minFitScore)
				.max(Comparator.comparing(room -> roomWallOverlapScore(room, rr)));
	}

	private static float roomWallOverlapScore(Room room, Rectangle rect) {
		SideOverlap sideOverlap = room.getLongestTouchingSide(rect);
		Interval roomWall = room.getRoomInterval(sideOverlap.getSide(), rect.sideLineSegment(sideOverlap.getSide().opposite()));
		return sideOverlap.getOverlap() + (sideOverlap.getOverlap() / roomWall.length());
	}

	private static List<Room> filterRoomPerimeters(List<RoomPerimeter> roomPerimeters,
												   Predicate<RoomPerimeter> predicate) {
		return roomPerimeters.stream()
				.filter(predicate).map(p -> p.room)
				.collect(Collectors.toList());
	}

	private static Predicate<RoomPerimeter> rectIsNoProtrusionForRoom(Rectangle rect) {
		return rp -> {
			Vector2D overlap = rp.perimeter;
			Room room = rp.room;
			boolean widthOverlapIsNoProtrusion = room.stats().maxProtrusionSize < rect.width + EPSILON
												 && overlap.x > rect.width - EPSILON;
			boolean heightOverlapIsNoProtrusion = room.stats().maxProtrusionSize < rect.height + EPSILON
												  && overlap.y > rect.height - EPSILON;
			return widthOverlapIsNoProtrusion || heightOverlapIsNoProtrusion;
		};
	}

	private static Predicate<RoomPerimeter> roomOverlapsEntireWidthOrHeight(Rectangle rect) {
		return rp -> {
			Vector2D overlap = rp.perimeter;
			return overlap.x > rect.width - EPSILON || overlap.y > rect.height - EPSILON;
		};
	}
}
