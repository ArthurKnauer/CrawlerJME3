package architect.processors.connector;

import architect.room.Room;
import architect.walls.WallNode;

public class Path {

	public final WallNode startNode;
	public final WallNode endNode;
	public final Room startRoom;
	public final Room endRoom;
	public final float cost;

	public Path(WallNode startNode, WallNode endNode, Room startRoom, Room endRoom, float cost) {
		this.startNode = startNode;
		this.endNode = endNode;
		this.startRoom = startRoom;
		this.endRoom = endRoom;
		this.cost = cost;
	}
}
