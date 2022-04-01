package architect.walls;

import architect.math.segments.Orientation;
import architect.math.segments.LineSegment;
import architect.math.segments.Side;
import static architect.Constants.*;
import architect.floorplan.FloorPlanAttribs;
import static architect.logger.LogManager.*;
import architect.math.*;
import static architect.math.segments.Side.*;
import architect.room.Room;
import architect.room.RoomRect;
import architect.room.RoomType;
import architect.utils.ConstructionFlags;
import architect.utils.UniqueID;
import static java.lang.Math.*;
import java.util.Collection;

public class RoomRectWall extends LineSegment {

	public WallType type;
	public float optimalPos; // two rooms will add their optimal pos for this wall -> is averaged afterwards by optimalPosWeights
	public float optimalPosWeights; // the more a room "cares" about a wall, the more the weight to optimal position it adds
	public RoomRect rrA, rrB;
	public WallSide wallSideA, wallSideB;
	private float doorPos;
	private boolean hasDoor = false;

	public final ConstructionFlags flags = new ConstructionFlags();
	public final int id;

	public RoomRectWall(Orientation orientation, WallType type, RoomRect rrA, RoomRect rrB, float min, float max, float pos) {
		super(min, max, pos, orientation);
		this.type = type;
		this.rrA = rrA;
		this.rrB = rrB;
		this.optimalPos = pos;
		id = UniqueID.nextID(getClass());
	}

	public RoomRectWall(Orientation orientation, WallType type, RoomRect rrA, float min, float max, float pos) {
		this(orientation, type, rrA, null, min, max, pos);
	}

	public RoomRectWall(RoomRectWall wallToCopy, float min, float max) {
		this(wallToCopy.orientation, wallToCopy.type, wallToCopy.rrA, wallToCopy.rrB, min, max, wallToCopy.pos);
	}

	public RoomRectWall(WallType wallType, RoomRect rrA, LineSegment line) {
		this(line.orientation, wallType, rrA, line.min, line.max, line.pos);
	}

	public RoomRectWall(WallType wallType, RoomRect rrA, RoomRect rrB, LineSegment line) {
		this(line.orientation, wallType, rrA, rrB, line.min, line.max, line.pos);
	}

	public WallSide wallSideOf(Room room) {
		if (rrA != null && rrA.isAssignedTo(room))
			return wallSideA;
		else if (rrB != null && rrB.isAssignedTo(room))
			return wallSideB;
		else
			throw new IllegalArgumentException(this + " is not a wall of " + room);
	}

	public void setDoor(float pos) {
		doorPos = pos;
		hasDoor = true;
	}

	public boolean hasDoor() {
		return hasDoor;
	}

	public float doorPos() {
		return doorPos;
	}

	@Override
	public String toString() {
		return id + ": [" + decimalFormat.format(min) + ", " + decimalFormat.format(max) + "] at " + decimalFormat.format(pos) + " " + orientation;
	}

	public void set(RoomRect rrA, RoomRect rrB, float min, float max, float pos) {
		this.rrA = rrA;
		this.rrB = rrB;
		this.min = min;
		this.max = max;
		this.pos = pos;
		this.optimalPos = pos;
	}

	public boolean canFuseWith(RoomRectWall wall, float maxDistForFuse) {
		if (orientation != wall.orientation || abs(pos - wall.pos) > EPSILON) {
			return false;
		}

		float overlap = min(max, wall.max) - max(min, max);
		if (overlap < -EPSILON)
			return false; // not touching -> cant fuse

		if (abs(optimalPos - wall.optimalPos) > maxDistForFuse) { // if pressures too different, don't fuse if between different rooms
			return (rrA == null || wall.isAssignedTo(rrA.assignedTo().orElse(null)))
				   && (rrB == null || wall.isAssignedTo(rrB.assignedTo().orElse(null)));
		}
		else {
			return true;
		}
	}

	public boolean isAssignedTo(Room room) {
		return ((rrA != null && rrA.isAssignedTo(room)) || (rrB != null && rrB.isAssignedTo(room)));
	}

	public boolean belongsToType(RoomType type) {
		return ((rrA != null && rrA.isAssigned() && rrA.assignedTo().get().type() == type)
				|| (rrB != null && rrB.isAssigned() && rrB.assignedTo().get().type() == type));
	}

	public Side sideOf(Room room) {
		if (!isAssignedTo(room))
			throw new IllegalArgumentException(this + " does not blong to " + room);

		if (isHorizontal()) {
			if (aboveOf(room))
				return Side.Top;
			else
				return Side.Bottom;
		}
		else {
			if (rightOf(room))
				return Side.Right;
			else
				return Side.Left;
		}
	}

	public boolean aboveOf(Room room) {
		if (isVertical())
			throw new IllegalArgumentException(this + " is not HORIZONTAL");

		return ((rrA != null && rrA.isAssignedTo(room) && rrA.centerY() < pos)
				|| (rrB != null && rrB.isAssignedTo(room) && rrB.centerY() < pos));
	}

	public boolean belowOf(Room room) {
		if (isVertical())
			throw new IllegalArgumentException(this + " is not HORIZONTAL");

		return ((rrA != null && rrA.isAssignedTo(room) && rrA.centerY() > pos)
				|| (rrB != null && rrB.isAssignedTo(room) && rrB.centerY() > pos));
	}

	public boolean rightOf(Room room) {
		if (isHorizontal())
			throw new IllegalArgumentException(this + " is not VERTICAL");

		return (rrA != null && rrA.isAssignedTo(room) && rrA.centerX() < pos)
			   || (rrB != null && rrB.isAssignedTo(room) && rrB.centerX() < pos);
	}

	public boolean leftOf(Room room) {
		if (isHorizontal())
			throw new IllegalArgumentException(this + " is not VERTICAL");

		return (rrA != null && rrA.isAssignedTo(room) && rrA.centerX() > pos)
			   || (rrB != null && rrB.isAssignedTo(room) && rrB.centerX() > pos);
	}

	public Vector2D getNormal(Room room) {
		if (isVertical()) {
			if ((rrA != null && rrA.isAssignedTo(room) && rrA.centerX() > pos)
				|| (rrB != null && rrB.isAssignedTo(room) && rrB.centerX() > pos))
				return new Vector2D(1, 0);
			else
				return new Vector2D(-1, 0);
		}
		else {
			if ((rrA != null && rrA.isAssignedTo(room) && rrA.centerY() > pos)
				|| (rrB != null && rrB.isAssignedTo(room) && rrB.centerY() > pos))
				return new Vector2D(0, 1);
			else
				return new Vector2D(0, -1);
		}
	}

	public RoomRect leftRoomRect() {
		if (isHorizontal())
			throw new IllegalArgumentException(this + " is not VERTICAL");

		return rrA.centerX() < pos ? rrA : rrB;
	}

	public RoomRect rightRoomRect() {
		if (isHorizontal())
			throw new IllegalArgumentException(this + " is not VERTICAL");

		return rrA.centerX() > pos ? rrA : rrB;
	}

	public RoomRect upperRoomRect() {
		if (isVertical())
			throw new IllegalArgumentException(this + " is not HORIZONTAL");

		return rrA.centerY() > pos ? rrA : rrB;
	}

	public RoomRect lowerRoomRect() {
		if (isVertical())
			throw new IllegalArgumentException(this + " is not HORIZONTAL");

		return rrA.centerY() < pos ? rrA : rrB;
	}

	public void recheckRoomRects(Collection<RoomRect> roomRects) {
		float bestRRAOverlap = 0, bestRRBOverlap = 0;
		RoomRect bestRRA = null, bestRRB = null;
		for (RoomRect rr : roomRects) {
			if (orientation == Orientation.Horizontal) {
				float overlap = rr.xOverlap(min, max);
				if (overlap > 0) {
					if (overlap > bestRRAOverlap && rr.sidePosEquals(Bottom, pos)) {
						bestRRAOverlap = overlap;
						bestRRA = rr;
					}
					else if (overlap > bestRRBOverlap && rr.sidePosEquals(Top, pos)) {
						bestRRBOverlap = overlap;
						bestRRB = rr;
					}
				}
			}
			else { // vertical wall
				float overlap = rr.yOverlap(min, max);
				if (overlap > 0) {
					if (overlap > bestRRAOverlap && rr.sidePosEquals(Left, pos)) {
						bestRRAOverlap = overlap;
						bestRRA = rr;
					}
					else if (overlap > bestRRBOverlap && rr.sidePosEquals(Right, pos)) {
						bestRRBOverlap = overlap;
						bestRRB = rr;
					}
				}
			}
		}
		rrA = bestRRA;
		rrB = bestRRB;
	}

	/**
	 * Retracts optimalPos if it overlaps the given rectangle (e.g. entrance)
	 *
	 * @param rect Rectangle to avoid overlap with
	 */
	public void avoidOptimalPosOverlap(Rectangle rect) {
		if (isHorizontal() && overlaps(rect.minX, rect.maxX)) {
			if (pos < optimalPos && rect.yOverlap(pos, optimalPos) > -EPSILON) {
				optimalPos = max(pos, rect.minY);
			}
			else if (pos > optimalPos && rect.yOverlap(optimalPos, pos) > -EPSILON) {
				optimalPos = min(pos, rect.maxY);
			}
		}
		else if (isVertical() && overlaps(rect.minY, rect.maxY)) {
			if (pos < optimalPos && rect.xOverlap(pos, optimalPos) > -EPSILON) {
				optimalPos = max(pos, rect.minX);
			}
			else if (pos > optimalPos && rect.xOverlap(optimalPos, pos) > -EPSILON) {
				optimalPos = min(pos, rect.maxX);
			}
		}
	}

	public boolean canOptimalPosCut(FloorPlanAttribs attribs) {
		return abs(optimalPos - pos) > attribs.minSubrectCut;
	}

	public Rectangle optimalPosRect() {
		Rectangle rect;
		if (isHorizontal())
			if (optimalPos > pos)
				rect = new Rectangle(min, pos, length(), optimalPos - pos);
			else
				rect = new Rectangle(min, optimalPos, length(), pos - optimalPos);
		else { // orientation == Orientation.VERTICAL
			if (optimalPos > pos)
				rect = new Rectangle(pos, min, optimalPos - pos, length());
			else
				rect = new Rectangle(optimalPos, min, pos - optimalPos, length());
		}

		return rect;
	}
}
