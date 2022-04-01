package architect.processors.connector;

import architect.room.Room;
import architect.walls.RoomRectWall;
import java.util.HashSet;

public class RoomIslandPathTrimmer implements PathTrimmer {

	final HashSet<Room> island;

	public RoomIslandPathTrimmer(HashSet<Room> island) {
		this.island = island;
	}

	@Override
	public Room canTrim(RoomRectWall wall) {
		if (wall.rrA != null && island.contains(wall.rrA.assignedTo().get()))
			return wall.rrA.assignedTo().get();
		if (wall.rrB != null && island.contains(wall.rrB.assignedTo().get()))
			return wall.rrB.assignedTo().get();
		return null;
	}
}
