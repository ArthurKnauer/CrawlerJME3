package architect.walls;

import architect.math.segments.LineSegment;
import architect.math.segments.Orientation;
import architect.math.Vector2D;
import architect.room.Room;
import architect.utils.ConstructionFlags;
import architect.utils.UniqueID;
import java.util.TreeMap;

public class WallSide extends LineSegment {

	public final Vector2D normal;
	private Vector2D direction;
	private final TreeMap<Float, Opening> openings = new TreeMap<>();
	private boolean hasWindows = false;
	private boolean hasDoors = false;

	public final WallType type;

	public final ConstructionFlags flags = new ConstructionFlags();

	public final int id;

	public WallSide(LineSegment line, Vector2D normal, WallType type) {
		this(line.min, line.max, line.pos, line.orientation, normal, type);
	}

	public WallSide(float min, float max, float pos, Orientation orientation, Vector2D normal, WallType type) {
		super(min, max, pos, orientation);
		this.normal = normal;
		this.type = type;
		this.id = UniqueID.nextID(getClass());
	}

	public static WallSide copyForSubFloorPlan(WallSide toCopy) {
		return new WallSide(toCopy.min, toCopy.max, toCopy.pos, toCopy.orientation, toCopy.normal,
							toCopy.type == WallType.INTERNAL ? WallType.OUTER_WINDOWLESS : toCopy.type);
	}

	public boolean hasWindows() {
		return hasWindows;
	}

	public boolean hasDoors() {
		return hasDoors;
	}

	public void setMinMax(float min, float max) {
		this.min = min;
		this.max = max;
	}

	public Vector2D getNormal() {
		return normal;
	}

	public void setDirection(Vector2D direction) {
		this.direction = direction;
	}

	public Vector2D getDirection() {
		if (isHorizontal())
			return new Vector2D(1, 0);
		else
			return new Vector2D(0, 1);
	}

	public TreeMap<Float, Opening> getOpenings() {
		return openings;
	}

	public Vector2D getOpeningStart(Opening opening) {
		if (isHorizontal())
			return new Vector2D(min + opening.start, pos);
		else
			return new Vector2D(pos, min + opening.start);
	}

	public Vector2D getOpeningCenter(Opening opening) {
		float extent = 0.5f * (opening.end - opening.start);
		if (isHorizontal())
			return new Vector2D(min + opening.start + extent, pos);
		else
			return new Vector2D(pos, min + opening.start + extent);
	}

	public Vector2D getOpeningEnd(Opening opening) {
		if (isHorizontal())
			return new Vector2D(min + opening.end, pos);
		else
			return new Vector2D(pos, min + opening.end);
	}

	public boolean convexCorner(WallSide wall) {
		Vector2D toCenter = wall.center().subtract(center());
		return toCenter.dot(normal) > 0;
	}

	public void addWindows(float windowWidth, float windowBottom, float windowTop) {
		float maxLength = length() - 0.2f;
		int windows = (int) (maxLength / windowWidth);
		if (windows > 0) {
			float windowLength = windows * windowWidth;
			float offset = (length() - windowLength) * 0.5f;

			openings.put(0.5f, new Opening(offset, windowLength + offset, windowBottom, windowTop, Opening.Type.WINDOW, null));
			hasWindows = true;
		}
	}

	public void addDoor(float start, float end, float height, Room neighborRoom) {
		openings.put(start - min, new Opening(start - min, end - min, 0, height, Opening.Type.DOOR, neighborRoom));
		hasDoors = true;
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final WallSide other = (WallSide) obj;
		if (this.type != other.type)
			return false;
		if (this.id != other.id)
			return false;
		return true;
	}
}
