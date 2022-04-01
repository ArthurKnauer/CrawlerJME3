package architect.processors.openings;

import static architect.Constants.EPSILON;
import architect.math.segments.LineSegment;
import architect.math.Rectangle;
import architect.processors.FloorPlanProcessor;
import architect.processors.helpers.Simplifier;
import architect.processors.helpers.WallCreator;
import architect.room.Room;
import static architect.utils.ConstructionFlags.Flag.PROCESSED;
import architect.walls.RoomRectWall;
import architect.walls.WallSide;
import architect.walls.WallType;
import java.util.*;

/**
 *
 * @author VTPlusAKnauer
 */
public class DoorCreator extends FloorPlanProcessor {

	HashSet<Room> processedRooms;

	@Override
	protected void process() {
		Simplifier.simplifyAll(fp);
		WallCreator.updateRoomsAndWalls(fp);

		processedRooms = new HashSet<>();
		Queue<Room> publicRooms = new LinkedList<>();
		for (Room room : fp.rooms) {
			if (room.isOverlapping(fp.floorPlanPoly.entrance)) {
				publicRooms.add(room);
				processedRooms.add(room);
				addEntranceDoor(room, fp.attribs.doorWidth, fp.attribs.doorHeight, fp.floorPlanPoly.entrance);
			}
		}

		while (!publicRooms.isEmpty() && processedRooms.size() < fp.rooms.size()) {
			publicRooms.addAll(addDoors(publicRooms.poll()));
		}
	}

	private ArrayList<Room> addDoors(Room room) {
		ArrayList<Room> publicRooms = new ArrayList<>();

		// prepare wall lists for each unprocessed neighbor
		HashMap<Room, ArrayList<RoomRectWall>> roomToWallMap = new HashMap<>();
		for (Room neighbor : room.neighbors()) {
			if (!processedRooms.contains(neighbor)) {
				roomToWallMap.put(neighbor, new ArrayList<>(4));
			}
		}

		// collect doorable walls for each neighbor room
		for (RoomRectWall wall : room.getWallLoop()) {
			if (wall.type == WallType.INTERNAL) {
				Room neighbor = wall.rrA.isAssignedTo(room) ? wall.rrB.assignedTo().get() : wall.rrA.assignedTo().get();
				if (!processedRooms.contains(neighbor)) {
					LineSegment overlap = wall.wallSideA.overlap(wall.wallSideB);
					if (overlap.length() > fp.attribs.minOverlapForDoor - EPSILON) {
						ArrayList<RoomRectWall> walls = roomToWallMap.get(neighbor);
						walls.add(wall);
					}
				}
			}
		}

		// select a random wall for each neighbor and door
		for (Room neighbor : room.neighbors()) {
			if (!processedRooms.contains(neighbor)) {
				ArrayList<RoomRectWall> walls = roomToWallMap.get(neighbor);
				if (!walls.isEmpty()) { // at least one doorable wall was found
					RoomRectWall wall = walls.get(rand.nextInt(walls.size()));
					addDoorToWall(wall, fp.attribs.doorWidth, fp.attribs.doorHeight);
					processedRooms.add(neighbor);
					if (neighbor.stats().isPublic)
						publicRooms.add(neighbor);
				}
			}
		}

		return publicRooms;
	}

	private void addDoorToWall(RoomRectWall wall, float width, float height) {
		LineSegment overlap = wall.wallSideA.overlap(wall.wallSideB);
		float leeway = overlap.length() - width - fp.attribs.wallThickness;
		if (leeway < 0)
			throw new RuntimeException("cant build a door where not enough overlap at wall " + wall);
		float doorPos = overlap.min + fp.attribs.wallThickness * 0.5f + rand.nextFloat() * leeway;
		wall.wallSideA.addDoor(doorPos, doorPos + width, height, wall.rrB.assignedTo().get());
		wall.wallSideB.addDoor(doorPos, doorPos + width, height, wall.rrA.assignedTo().get());
		wall.setDoor(doorPos + width * 0.5f);
	}

	private void addEntranceDoor(Room room, float width, float height, Rectangle entrance) {
		for (RoomRectWall wall : room.getWallLoop()) {
			if (wall.type != WallType.INTERNAL && wall.isTouching(entrance)) {
				WallSide wallSide = wall.wallSideA == null ? wall.wallSideB : wall.wallSideA;
				LineSegment overlap = wallSide.overlap(entrance);
				float doorPos = overlap.mid() - width * 0.5f;
				wallSide.addDoor(doorPos, doorPos + width, height, null);
				wall.setDoor(doorPos + width * 0.5f);
				wallSide.flags.add(PROCESSED);
			}
		}
	}

}
