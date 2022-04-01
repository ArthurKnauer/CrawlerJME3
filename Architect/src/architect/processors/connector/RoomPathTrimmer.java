package architect.processors.connector;

import architect.room.Room;
import architect.walls.RoomRectWall;

/**
 *
 * @author VTPlusAKnauer
 */
public class RoomPathTrimmer implements PathTrimmer {

	private final Room room;

	public RoomPathTrimmer(Room room) {
		this.room = room;
	}

	@Override
	public Room canTrim(RoomRectWall wall) {
		if (wall.rrA != null && room == wall.rrA.assignedTo().get())
			return room;
		if (wall.rrB != null && room == wall.rrB.assignedTo().get())
			return room;
		return null;
	}

}
