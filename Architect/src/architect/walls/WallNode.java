package architect.walls;

import architect.math.Vector2D;
import architect.room.Room;
import architect.room.RoomRect;
import architect.utils.UniqueID;
import java.util.HashMap;
import java.util.HashSet;

public final class WallNode {

	public final Vector2D position;
	private final int hashCode;

	public HashMap<WallNode, WallNodeNeighbor> neighbors;
	public HashSet<RoomRect> roomRects;
	public HashSet<Room> rooms;

	public enum SearchStatus {

		UNCHARTED,
		OPEN,
		CLOSED
	};

	public SearchStatus searchStatus = SearchStatus.UNCHARTED;
	public WallNode pathParent = null;
	public WallNode pathChild = null;
	public float distFromStart = 0;
	public float distToEndEstimate = 0;

	public final int id;

	public WallNode(Vector2D position, int hashcode) {
		this.position = position;
		this.hashCode = hashcode;

		neighbors = new HashMap<>();
		roomRects = new HashSet<>();
		rooms = new HashSet<>();
		id = UniqueID.nextID(getClass());
	}

	public void resetSearchStats() {
		searchStatus = SearchStatus.UNCHARTED;
		pathParent = null;
		pathChild = null;
		distFromStart = 0;
		distToEndEstimate = 0;
	}

	public float squaredDist(WallNode target) {
		return (target.position.x - position.x) * (target.position.x - position.x) + (target.position.y - position.y) * (target.position.y - position.y);
	}

	@Override
	public String toString() {
		StringBuilder res = new StringBuilder();
		res.append(id).append(":  ");
		for (WallNodeNeighbor neighbor : neighbors.values()) {
			res.append(neighbor.getNode().id).append(", ");
		}
		return res.substring(0, res.length() - 2);
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || obj.getClass() != this.getClass())
			return false;
		return ((WallNode) obj).hashCode == hashCode;
	}
}
