package architect.processors.connector;

import architect.walls.WallNodeNeighbor;

public interface CostJudge {

	public float judge(WallNodeNeighbor neighbor);

}
