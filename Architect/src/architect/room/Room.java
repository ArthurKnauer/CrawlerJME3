package architect.room;

import architect.math.segments.Orientation;
import architect.math.segments.LineSegment;
import architect.math.segments.Side;
import architect.math.segments.Interval;
import static architect.Constants.*;
import architect.floorplan.FloorPlan;
import architect.floorplan.FloorPlanAttribs;
import architect.floorplan.FloorPlanPoly;
import architect.math.*;
import architect.rectpoly.RectilinearPolyWithNeighbors;
import static architect.room.RoomStatus.Flags.ProtrusionsRemoved;
import static architect.room.RoomStatus.Flags.WallsCreated;
import architect.room.optimalrect.OptimalRect;
import architect.utils.ConstructionFlags;
import static architect.utils.ConstructionFlags.Flag.*;
import architect.utils.UniqueID;
import architect.walls.*;
import static java.lang.Math.*;
import java.util.*;
import java.util.function.Predicate;
import lombok.Getter;

public final class Room extends RectilinearPolyWithNeighbors<Room, RoomRect, RoomRectWall> {

	public final ConstructionFlags flags = new ConstructionFlags();
	public final RoomStatus status = new RoomStatus();

	private final ArrayList<Rectangle> importantRects = new ArrayList<>(); // important rects make sure their area wont be cut away as a protrusion

	public final int id;

	private final FloorPlanAttribs attribs;

	private RoomType type;
	private RoomStats stats;
	private String name;

	@Getter private float windowablePerimeter = 0;
	private final EnumMap<Side, Float> windowablePerimeters;

	@Getter private WallNode rootWallNode;
	@Getter private final LinkedList<WallNode> wallNodeLoop = new LinkedList<>();
	@Getter private final LinkedList<RoomRectWall> wallLoop = new LinkedList<>(); // must start at a corner
	@Getter private final TreeMap<Integer, LinkedList<RoomRectWall>> horizontalWalls = new TreeMap<>();
	@Getter private final TreeMap<Integer, LinkedList<RoomRectWall>> verticalWalls = new TreeMap<>();
	@Getter private final LinkedList<WallSide> wallSides = new LinkedList<>();

	@Getter private final OptimalRect optimalRect = new OptimalRect();

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || obj.getClass() != this.getClass())
			return false;
		else
			return ((Room) obj).id == id;
	}

	public Room(RoomType type, RoomStats stats, String name, FloorPlanAttribs attribs) {
		super(new HashSet<>(), new LinkedList<>());
		this.type = type;
		this.stats = stats;
		this.name = name;
		this.id = UniqueID.nextID(getClass());
		this.attribs = attribs;

		windowablePerimeters = new EnumMap<>(Side.class);
		for (Side side : Side.values()) {
			windowablePerimeters.put(side, 0.0f);
		}
	}

	public List<Rectangle> importantRects() {
		return Collections.unmodifiableList(importantRects);
	}

	public boolean importantRectsOverlap(Rectangle rect) {
		return importantRects.stream()
				.anyMatch(importantRect -> rect.overlaps(importantRect));
	}

	public float windowablePerimeter(Side side) {
		return windowablePerimeters.get(side);
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(32);
		buf.append(name).append(" rr: ");
		for (RoomRect rr : subRects) {
			buf.append(rr.getID()).append(" ");
		}

		buf.append("| wn: ");

		for (WallNode node : getWallNodeLoop()) {
			buf.append(node.id).append(" ");
		}

		return buf.toString();
	}

	public RoomStats stats() {
		return stats;
	}

	public void setStats(RoomStats stats) {
		this.stats = stats;
	}

	public RoomType type() {
		return type;
	}

	public void setType(RoomType type) {
		this.type = type;
	}

	public String name() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public float areaToNeedRatio() {
		return (area() / stats.needsArea);
	}

	public void updateMaxProtrusionSize() {
		stats().maxProtrusionSize = min(boundingRect().shortestSideLength() * 0.5f,
										optimalRect.rect.shortestSideLength() * 0.5f);
	}

	public float windowablePerimeterSum() {
		return windowablePerimeter;
	}

	public void setRootWallNode(WallNode node) {
		rootWallNode = node;
	}

	public void updateWalls() {
		getHorizontalWalls().clear();
		getVerticalWalls().clear();
		getWallNodeLoop().clear();
		getWallLoop().clear();

		if (rootWallNode != null) {
			WallNode current = rootWallNode, next = null, previous = null;

			int wallNodesWalked = 0;
			while (next != rootWallNode && wallNodesWalked < 200) {
				wallNodesWalked++;
				getWallNodeLoop().add(current);
				if (current == null)
					throw new NullPointerException("current is null");
				for (WallNodeNeighbor neighbor : current.neighbors.values()) {
					WallNode node = neighbor.getNode();
					if (previous != node && neighbor.getWall().isAssignedTo(this)) {
						next = node;

						// we have the next node -> add the wall to this node to our treemap list
						RoomRectWall wall = neighbor.getWall();
						getWallLoop().add(wall);

						int index = (int) (wall.pos * 100);
						if (wall.isHorizontal()) {
							LinkedList<RoomRectWall> wallList = getHorizontalWalls().get(index);
							if (wallList == null) {
								wallList = new LinkedList<>();
								getHorizontalWalls().put(index, wallList);
							}
							wallList.add(wall);
						}
						else { // vertical
							LinkedList<RoomRectWall> wallList = getVerticalWalls().get(index);
							if (wallList == null) {
								wallList = new LinkedList<>();
								getVerticalWalls().put(index, wallList);
							}
							wallList.add(wall);
						}

						break;
					}
				}
				previous = current;
				current = next;
			}

			if (wallNodesWalked >= 200) {
				throw new RuntimeException("Room could not compute updateWalls successfully (inf. loop): " + name);
			}

			// wallLoop must start at a corner, other algorithms depend on this (WallSide creation)
			while (getWallLoop().getLast().orientation == getWallLoop().getFirst().orientation) {
				getWallLoop().addFirst(getWallLoop().removeLast()); // shift to the right
			}
		}
	}

	@Override
	public void addSubRect(RoomRect rr) {
		super.addSubRect(rr);

		status.clear();

		windowablePerimeter += rr.windowablePerimeterSum;
		for (Side side : Side.values()) {
			windowablePerimeters.put(side, windowablePerimeters.get(side) + rr.windowablePerimeter(side));
		}
	}

	@Override
	public void removeSubRect(RoomRect rr) {
		super.removeSubRect(rr);

		status.remove(ProtrusionsRemoved);
		status.remove(WallsCreated);

		windowablePerimeter -= rr.windowablePerimeterSum;
		for (Side side : Side.values()) {
			windowablePerimeters.put(side, windowablePerimeters.get(side) - rr.windowablePerimeter(side));
		}
	}

	public float maxWindowableOverlapRatio(Rectangle rect) {
		float vertOverlap = 0;
		float horiOverlap = 0;
		for (RoomRect rr : subRects) {
			horiOverlap += max(0, rect.xOverlap(rr))
						   * ((rr.windowablePerimeter(Side.Top) + rr.windowablePerimeter(Side.Bottom)) / rr.width);
			vertOverlap += max(0, rect.yOverlap(rr))
						   * ((rr.windowablePerimeter(Side.Left) + rr.windowablePerimeter(Side.Right)) / rr.height);
		}

		return max(vertOverlap / rect.height, horiOverlap / rect.width);
	}

	public Interval getHorizontalInterval(float y, float min, float max) {
		Interval range = new Interval(Float.MAX_VALUE, -Float.MAX_VALUE);
		for (RoomRect rr : subRects) {
			if (rr.intersectsY(y) && rr.xOverlap(min, max) > -EPSILON) {
				range.min = min(range.min, rr.minX);
				range.max = max(range.max, rr.maxX);
			}
		}
		return range;
	}

	public Interval getVerticalInterval(float x, float min, float max) {
		Interval range = new Interval(Float.MAX_VALUE, -Float.MAX_VALUE);
		for (RoomRect rr : subRects) {
			if (rr.intersectsX(x) && rr.yOverlap(min, max) > -EPSILON) {
				range.min = min(range.min, rr.minY);
				range.max = max(range.max, rr.maxY);
			}
		}
		return range;
	}

	public boolean touchingEdge(FloorPlanPoly floorPlanPoly, Side side) {
		return subRects.stream()
				.anyMatch(rr -> floorPlanPoly.edgesTouching(rr.sideLineSegment(side)).size() > 0);
	}

	public boolean touchingWindowableEdge(FloorPlanPoly floorPlanPoly, Side side) {
		return subRects.stream()
				.flatMap(rr -> floorPlanPoly.edgesTouching(rr.sideLineSegment(side)).stream())
				.anyMatch(wall -> wall.type == WallType.OUTER_WINDOWABLE);
	}

	public float areaOverlapWithImportantRects(Rectangle rect) {
		return (float) importantRects.stream()
				.mapToDouble(importantRect -> rect.overlapArea(importantRect))
				.sum();
	}

	public SideOverlap getLongestTouchingSide(Rectangle rect) {
		float overlap[] = new float[4];
		for (RoomRect rr : subRects) { // sum touch overlap for all sides of the rrs
			if (rr != rect) {
				if (rr.isTouchingSide(rect)) {
					Side side = rr.sideTouchingNeighbor(rect);
					overlap[side.ordinal()] += side.isLeftOrRight ? rr.yOverlap(rect) : rr.xOverlap(rect);
				}
			}
		}

		// find side with max overlap
		float maxOverlap = 0;
		Side longestTouchingSide = null;
		for (Side side : Side.values()) {
			if (overlap[side.ordinal()] > maxOverlap) {
				maxOverlap = overlap[side.ordinal()];
				longestTouchingSide = side;
			}
		}

		return new SideOverlap(longestTouchingSide, maxOverlap);
	}

	public LineSegment getRoomInterval(Side edge, LineSegment line) {
		Interval range = new Interval(Float.MAX_VALUE, -Float.MAX_VALUE);
		LinkedList<RoomRect> toCheck = new LinkedList<>();

		if (edge.isLeftOrRight) {
			boolean foundRROnEdge = false;
			for (RoomRect rr : subRects) { // look for RRs that are overlaping the edge now or add if maybe later when the interval is wider
				if (rr.intersectsX(line.pos)) {
					if (rr.overlap(line) > -EPSILON) {
						range.min = min(range.min, rr.minY);
						range.max = max(range.max, rr.maxY);
						foundRROnEdge = true;
					}
					else {
						toCheck.add(rr); // once the interval widens, more RRs can "connect" and increase the interval
						rr.flags.remove(PROCESSED);
					}
				}
			}
			if (!foundRROnEdge) {
				return new LineSegment(range, line.pos, line.orientation);
			}

			boolean foundRRInInterval = true; // loop throgh all possible RRs, increasing the interval, starting over once all are ruled out
			while (foundRRInInterval) {
				foundRRInInterval = false;
				for (RoomRect rr : toCheck) {
					if (!rr.flags.contains(PROCESSED) && rr.yOverlap(range.min, range.max) > -EPSILON) {
						range.min = min(range.min, rr.minY);
						range.max = max(range.max, rr.maxY);
						rr.flags.add(PROCESSED);
						foundRRInInterval = true;
					}
				}
			}
		}
		else { // horizontal edge
			boolean foundRROnEdge = false;
			for (RoomRect rr : subRects) { // look for RRs that are overlaping the edge now or add if maybe later when the interval is wider
				if (rr.intersectsY(line.pos)) {
					if (rr.overlap(line) > -EPSILON) {
						range.min = min(range.min, rr.minX);
						range.max = max(range.max, rr.maxX);
						foundRROnEdge = true;
					}
					else {
						toCheck.add(rr); // once the interval widens, more RRs can "connect" and increase the interval
						rr.flags.remove(PROCESSED);
					}
				}
			}
			if (!foundRROnEdge) {
				return new LineSegment(range, line.pos, line.orientation);
			}

			boolean foundRRInInterval = true; // loop throgh all possible RRs, increasing the interval, starting over once all are ruled out
			while (foundRRInInterval) {
				foundRRInInterval = false;
				for (RoomRect rr : toCheck) {
					if (!rr.flags.contains(PROCESSED) && rr.xOverlap(range.min, range.max) > -EPSILON) {
						range.min = min(range.min, rr.minX);
						range.max = max(range.max, rr.maxX);
						rr.flags.add(PROCESSED);
						foundRRInInterval = true;
					}
				}
			}
		}

		return new LineSegment(range, line.pos, line.orientation);
	}

	public boolean isTouchingSides(Room room) {
		return subRects.stream()
				.flatMap(rr -> room.subRects.stream().filter(nr -> rr.isTouchingSide(nr)))
				.findAny().isPresent();
	}

	public Vector2D commonCorner(Room room) {
		for (RoomRect rr : subRects) {
			for (RoomRect nr : room.subRects) {
				if (rr.isTouchingOnlyCorner(nr)) {
					return rr.cornerTouchingNeighbor(nr);
				}
			}
		}
		return null;
	}

	public LineSegment commonLongestWall(Room room) {
		if (!neighbors.contains(room))
			return null;

		LineSegment commonLongestWall = new LineSegment(0, 0, 0, Orientation.Horizontal);

		for (RoomRect rr : subRects) {
			for (Side side : Side.values()) {
				for (RoomRect neighbor : rr.sideNeighbors(side)) {
					if (neighbor.isAssignedTo(room)) {
						LineSegment touchLine = rr.commonPerimeterLine(neighbor);
						if (commonLongestWall.length() > 0 && commonLongestWall.canJoin(touchLine)) {
							commonLongestWall.scaleToHold(touchLine);
						}
						else if (touchLine.length() > commonLongestWall.length())
							commonLongestWall = touchLine;
					}
				}
			}
		}

		return commonLongestWall;
	}

	public void addImportantRect(Rectangle rect) {
		importantRects.add(rect);
	}

	public void addImportantRects(Collection<Rectangle> rects) {
		importantRects.addAll(rects);
	}

	// TODO: sum up consecutive perimeter instead of total
	public boolean canHaveDoorWith(Room neighbor) {
		return commonYPerimeter(neighbor) > attribs.minOverlapForDoor
			   || commonXPerimeter(neighbor) > attribs.minOverlapForDoor;
	}

	public boolean canHaveDoorWithNeighborMatching(Predicate<Room> predicate) {
		return neighbors.stream()
				.anyMatch(neighbor -> predicate.test(neighbor) && canHaveDoorWith(neighbor));
	}

	public void addWindows(FloorPlan fp) {
		for (RoomRectWall wall : getWallLoop()) {
			if (wall.type == WallType.OUTER_WINDOWABLE) {
				WallSide wallSide = wall.wallSideA == null ? wall.wallSideB : wall.wallSideA;
				if (wallSide.flags.contains(PROCESSED))
					continue;
				wallSide.addWindows(fp.attribs.windowWidth, fp.attribs.windowBottom, fp.attribs.windowTop);

				wallSide.flags.add(PROCESSED);
				//TODO: mark WallSide internal or external also windowable
			}
		}
	}

	public void updateWallSides() {
		wallSides.clear();

		RoomRectWall firstWall = getWallLoop().getFirst();
		RoomRectWall lastWall = getWallLoop().getLast();
		boolean counterClockWise;

		Vector2D wallDir = lastWall.intersection(firstWall).subtract(lastWall.center());
		Vector2D normal = lastWall.getNormal(this);
		counterClockWise = wallDir.x * normal.y - wallDir.y * normal.x > 0;

		// loop through all RoomRectWalls and join segments to WallSides
		// joining first and last wall not neccessary since wallLoop starts at a corner
		WallSide ws = null;
		for (RoomRectWall wall : getWallLoop()) {
			boolean aboveOrRightOfRoom;
			if (wall.isHorizontal())
				aboveOrRightOfRoom = wall.aboveOf(this);
			else
				aboveOrRightOfRoom = wall.rightOf(this);

			if (ws == null || ws.orientation != wall.orientation) {
				if (wall.isHorizontal())
					ws = new WallSide(wall, aboveOrRightOfRoom ? new Vector2D(0, -1) : new Vector2D(0, 1), wall.type);
				else // wall is vertical
					ws = new WallSide(wall, aboveOrRightOfRoom ? new Vector2D(-1, 0) : new Vector2D(1, 0), wall.type);
				if (counterClockWise)
					getWallSides().addLast(ws);
				else
					getWallSides().addFirst(ws);
			}
			else { // same orientation -> lengthen current wall, dont add new one               
				ws.max = max(ws.max, wall.max);
				ws.min = min(ws.min, wall.min);
			}

			if (wall.rrA.isAssignedTo(this))
				wall.wallSideA = ws;
			else
				wall.wallSideB = ws;

			if (wall.isHorizontal())
				if (aboveOrRightOfRoom)
					ws.setDirection(new Vector2D(counterClockWise ? 1 : -1, 0));
				else
					ws.setDirection(new Vector2D(counterClockWise ? -1 : 1, 0));
			else if (aboveOrRightOfRoom)
				ws.setDirection(new Vector2D(0, counterClockWise ? -1 : 1));
			else
				ws.setDirection(new Vector2D(0, counterClockWise ? 1 : -1));
		}
	}
}
