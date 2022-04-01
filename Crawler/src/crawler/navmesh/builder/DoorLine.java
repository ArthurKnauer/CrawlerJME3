package crawler.navmesh.builder;

import architect.room.Room;
import com.jme3.math.Vector3f;
import convdecomp.geometry.Point;
import lombok.*;

class DoorLine {

	@NonNull public final Room roomA;
	public final Room roomB;
	public final Point start;
	public final Point end;
	@Getter @Setter private Vector3f startVertex;
	@Getter @Setter private Vector3f endVertex;
	@Getter @Setter private int vertexIdx;
	@Getter @Setter private boolean navPolyCreated = false;

	public DoorLine(Room roomA, Room roomB, Point start, Point end) {
		if (roomA == null)
			throw new IllegalArgumentException("roomA cannot be null");
		this.roomA = roomA;
		this.roomB = roomB;
		this.start = start;
		this.end = end;
	}

	public boolean isOppositeOf(DoorLine other) {
		return other.roomB == roomA && other.roomA == roomB;
	}

	boolean hasNullRoom() {
		return roomB == null;
	}
}
