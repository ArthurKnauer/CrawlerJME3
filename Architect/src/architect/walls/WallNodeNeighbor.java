package architect.walls;

public class WallNodeNeighbor {

	private final WallNode node;	// the neighbor
	private final RoomRectWall wall;	// the wall to this neighbor	

	public WallNodeNeighbor(WallNode wallNode, RoomRectWall wall) {
		this.node = wallNode;
		this.wall = wall;
	}

	public WallNode getNode() {
		return node;
	}

	public RoomRectWall getWall() {
		return wall;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 43 * hash + node.hashCode();
		hash = 43 * hash + wall.hashCode();
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || obj.getClass() != this.getClass())
			return false;
		return node.equals(((WallNodeNeighbor) obj).node);
	}
}
