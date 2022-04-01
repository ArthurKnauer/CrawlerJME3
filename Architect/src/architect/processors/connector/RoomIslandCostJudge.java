package architect.processors.connector;

import architect.room.Room;
import architect.walls.RoomRectWall;
import architect.walls.WallNodeNeighbor;
import java.util.HashSet;

public final class RoomIslandCostJudge implements CostJudge {

	final HashSet<Room> island;

	public RoomIslandCostJudge(HashSet<Room> island) {
		this.island = island;
	}

	@Override
	public float judge(WallNodeNeighbor neighbor) {
		RoomRectWall wall = neighbor.getWall();
		if (wall.rrA != null && island.contains(wall.rrA.assignedTo().get()))
			return 0;
		if (wall.rrB != null && island.contains(wall.rrB.assignedTo().get()))
			return 0;

		return DefaultCostJudge.defualtCost(neighbor);
	}
}
