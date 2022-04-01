package architect.room;

import architect.math.segments.Corner;
import architect.math.segments.LineSegment;
import architect.math.segments.Side;
import static architect.Constants.*;
import architect.floorplan.FloorPlanPoly;
import architect.math.*;
import architect.rectpoly.RectAssignedToPoly;
import architect.utils.ConstructionFlags;
import architect.utils.UniqueID;
import architect.walls.WallSide;
import architect.walls.WallType;
import static java.lang.Math.*;
import java.util.*;
import java.util.stream.Collectors;

public final class RoomRect extends RectAssignedToPoly<Room, RoomRect> {

	public final int id;

	public final float windowablePerimeterSum;
	public final int windowlessOuterWallSides; // amount of sides that coinside with a no-window-outer-wall
	public final int windowableOuterWallSides; // amount of sides that coinside with a potential-window-outer-wall	
	private final EnumMap<Side, Float> windowablePerimeter;

	private final FloorPlanPoly fpr;
	
	public final ConstructionFlags flags = new ConstructionFlags();

	public RoomRect(Rectangle rect, FloorPlanPoly fpr) {
		super(rect.minX, rect.minY, rect.width, rect.height);
		id = UniqueID.nextID(getClass());
		this.fpr = fpr;

		float windowablePerimeterSumCalc = 0;
		int windowableOuterWallSidesCalc = 0;
		int windowlessOuterWallSidesCalc = 0;

		windowablePerimeter = new EnumMap<>(Side.class);
		for (Side side : Side.values()) {
			windowablePerimeter.put(side, 0.0f);
		}

		for (Side side : Side.values()) {
			LineSegment line = sideLineSegment(side);
			List<WallSide> walls = fpr.edgesTouching(line);

			if (walls.isEmpty()) {
				windowlessOuterWallSidesCalc++;
			}
			else {
				boolean atLeastOneWindowableWall = false;
				for (WallSide wall : walls) {
					if (wall.type == WallType.OUTER_WINDOWABLE) {
						atLeastOneWindowableWall = true;
						float perimeterIncrease = wall.overlapLength(line);
						windowablePerimeter.put(side, windowablePerimeter.get(side) + perimeterIncrease);
						windowablePerimeterSumCalc += perimeterIncrease;
					}
				}

				if (atLeastOneWindowableWall)
					windowableOuterWallSidesCalc++;
				else
					windowlessOuterWallSidesCalc++;
			}
		}

		windowablePerimeterSum = windowablePerimeterSumCalc;
		windowableOuterWallSides = windowableOuterWallSidesCalc;
		windowlessOuterWallSides = windowlessOuterWallSidesCalc;
	}

	public float windowablePerimeter(Side side) {
		return windowablePerimeter.get(side);
	}

	public Room removeFromRoom() {
		Room wasAssgindedTo = assignedTo;
		if (assignedTo != null) {
			assignedTo.removeSubRect(this);
		}
		return wasAssgindedTo;
	}

	@Override
	public int hashCode() {
		return id;
	}

	public int getID() {
		return id;
	}

	@Override
	public String toString() {
		//returns "id: [neighbors] [intervals]"
		StringBuilder res = new StringBuilder(16);
		res.append(id).append(": [");
		for (Map.Entry<Side, TreeSet<RoomRect>> sneighbors : sideNeighbors.entrySet()) {
			if (!sneighbors.getValue().isEmpty()) {
				res.append(sneighbors.getKey().toString().charAt(0)).append(" ");
				for (RoomRect neighbor : sneighbors.getValue()) {
					res.append(neighbor.id).append(", ");
				}
			}
		}
		return res.substring(0, res.length() - 2) + "] " + super.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || obj.getClass() != this.getClass())
			return false;
		else
			return ((RoomRect) obj).id == id;
	}

	public List<RoomRect> cut(Rectangle cutter, float minRoomRectCut) {
		if (cutter.contains(this))
			return null;
		if (minOverlap(cutter) > minRoomRectCut) {

			ArrayList<Rectangle> rects = cut(cutter);
			List<RoomRect> roomRects = rects.stream().map(rect -> new RoomRect(rect, fpr))
					.collect(Collectors.toList());

			// add neighbor property between new cutlets and my old neighbors
			for (RoomRect neighbor : neighbors) {
				neighbor.removeNeighbor(this);
				for (RoomRect newRR : roomRects) {
					neighbor.addNeighbor(newRR);
					newRR.addNeighbor(neighbor);
				}
			}

			// add neighbor property inbetween cutlets
			for (RoomRect newRRA : roomRects) {
				for (RoomRect newRRB : roomRects) {
					if (newRRB != newRRA) {
						newRRB.addNeighbor(newRRA);
						newRRA.addNeighbor(newRRB);
					}
				}
			}

			return roomRects;
		}
		return null;
	}

	public EnumSet<Side> sidesAttachedTo(Room room) {
		EnumSet<Side> attachedSides = EnumSet.noneOf(Side.class);
		for (Side side : Side.values()) {
			for (RoomRect rr : sideNeighbors.get(side)) {
				if (rr.assignedTo == room) {
					attachedSides.add(side);
					break;
				}
			}
		}
		return attachedSides;
	}

	public boolean neighborAssignedTo(Room room) {
		return neighbors.stream()
				.anyMatch(neighbor -> neighbor.assignedTo == room);
	}

	public int getFreeNeighbors() {
		return (int) neighbors.stream()
				.filter(neighbor -> neighbor.assignedTo == null)
				.count();
	}

	public float roomOverlap(Room room, Side side) {
		return (float) sideNeighbors.get(side).stream()
				.filter(neighbor -> neighbor.assignedTo == room)
				.mapToDouble(neighbor -> side.isLeftOrRight ? yOverlap(neighbor) : xOverlap(neighbor))
				.sum();
	}

	public List<RoomPerimeter> roomPerimeters() {
		HashMap<Room, Vector2D> roomPerimeterMap = new HashMap<>();
		neighbors().stream().filter(neighbor -> neighbor.assignedTo != null).forEach(neighbor -> {
			Vector2D perimeter = roomPerimeterMap.get(neighbor.assignedTo);
			if (perimeter == null)
				perimeter = new Vector2D(0, 0);
			Vector2D perimeterSegment = new Vector2D(commonXPerimeter(neighbor),
													 commonYPerimeter(neighbor));
			perimeter = perimeter.add(perimeterSegment);
			roomPerimeterMap.put(neighbor.assignedTo, perimeter);
		});

		return roomPerimeterMap.entrySet().stream()
				.map(entry -> new RoomPerimeter(entry.getKey(), entry.getValue()))
				.collect(Collectors.toList());
	}

	public float bestRatioWithFreeNeighbor() {
		float bestRatio = inverseAspectRatio() * 0.5f; // if has no free neighbors, don't dominate those who do
		for (RoomRect rr : neighbors) {
			if (rr.assignedTo == null) {
				float horiOverlap = xOverlap(rr);
				float vertOverlap = yOverlap(rr);
				float newRatio = 0;

				// two neighboring rrs can only overlap horizontaly or vertically, not both ways
				// calculate the ratio of the inner rect of both rrs (can only be one)
				if (horiOverlap > EPSILON)
					newRatio = horiOverlap / (height + rr.height);
				else if (vertOverlap > EPSILON)
					newRatio = vertOverlap / (width + rr.width);

				if (newRatio > 1)
					newRatio = 1.0f / newRatio;
				if (newRatio > bestRatio) {  // if closer to 1
					bestRatio = newRatio;
				}
			}
		}
		return bestRatio;
	}

	public float cornerPos(Side side, Side edge) { // comments example: side = Side.Top and edge = Side.Left
		float closestToEdge = sidePos(edge.opposite()); // sidePos(Side.Right) = maxX of this RR
		for (RoomRect nb : sideNeighbors.get(side)) { // ALL top neighbors
			if (nb.assignedTo == assignedTo) // find leftmost same room neighbor
				closestToEdge = (edge.isLeftOrBottom)
								? min(nb.sidePos(edge), closestToEdge) : max(nb.sidePos(edge), closestToEdge); //nb.minX < closestToEdge -> closestToEdge = nb.minX
		}

		if (abs(closestToEdge - sidePos(edge.opposite())) < EPSILON) { // check further neighbors
			for (RoomRect nb : sideNeighbors.get(edge.opposite())) { // ALL right neighbors
				if (nb.assignedTo == assignedTo && sidePosEqual(side, nb))
					return nb.cornerPos(side, edge);
			}
		}
		return closestToEdge; // smallest minX of a same-room Side.Top neighbor
	}

	public boolean isCornerTouchingFloorPlanPoly(Corner corner) {
		return fpr.isEdgeTouching(cornerPoint(corner.verticalSide, corner.horizontalSide));
	}

	public Room roomEnvelopingCorner(Corner corner) {
		RoomRect leftOrRightNeighbor = sideNeighborAtEgde(corner.verticalSide, corner.horizontalSide);
		RoomRect topOrBottomNeighbor = sideNeighborAtEgde(corner.horizontalSide, corner.verticalSide);

		if (leftOrRightNeighbor == null || topOrBottomNeighbor == null
			|| leftOrRightNeighbor.assignedTo == assignedTo
			|| leftOrRightNeighbor.assignedTo != topOrBottomNeighbor.assignedTo)
			return null;
		else
			return leftOrRightNeighbor.assignedTo;
	}

}
