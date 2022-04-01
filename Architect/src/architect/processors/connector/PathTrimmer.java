package architect.processors.connector;

import architect.room.Room;
import architect.walls.RoomRectWall;

public interface PathTrimmer {

	public Room canTrim(RoomRectWall wall);

}
