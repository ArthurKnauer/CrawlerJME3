package architect.processors.connector;

import architect.walls.RoomRectWall;
import architect.walls.WallNodeNeighbor;
import architect.walls.WallType;

/**
 *
 * @author VTPlusAKnauer
 */
public class DefaultCostJudge implements CostJudge {

	private static final DefaultCostJudge instance = new DefaultCostJudge();

	private DefaultCostJudge() {
	}

	public static DefaultCostJudge instance() {
		return instance;
	}

	@Override
	public float judge(WallNodeNeighbor neighbor) {
		return defualtCost(neighbor);
	}

	public static float defualtCost(WallNodeNeighbor neighbor) {
		RoomRectWall wall = neighbor.getWall();

		float cost = wall.max - wall.min;
		if (wall.type == WallType.OUTER_WINDOWABLE)
			cost *= 40;
		else if (wall.type == WallType.OUTER_WINDOWLESS)
			cost *= 20.0f;
		return cost;
	}
}
