package architect.processors.helpers;

import architect.floorplan.FloorPlan;
import architect.math.segments.LineSegment;
import architect.math.segments.Side;
import architect.math.Vector2D;
import architect.room.Room;
import architect.room.RoomRect;
import static architect.room.RoomStatus.Flags.WallsCreated;
import architect.snapgrid.SnapGrid;
import static architect.utils.ConstructionFlags.Flag.PROCESSED;
import architect.walls.*;
import java.util.*;
import lombok.experimental.UtilityClass;

@UtilityClass
public class WallCreator {

	public static void updateRoomsAndWalls(FloorPlan fp) {
		if (fp.rooms.stream().anyMatch(room -> !room.status.contains(WallsCreated))) {
			createWallGraph(fp);

			for (Room room : fp.rooms) {
				room.updateWallSides();
				room.status.contains(WallsCreated);
			}

			OptimalRectFinder.findAll(fp);
		}
	}

	private static void addToWallListMap(TreeMap<Integer, LinkedList<RoomRectWall>> wallListMap, int index, ArrayList<RoomRectWall> walls, RoomRectWall wall) {
		LinkedList<RoomRectWall> wallList = wallListMap.get(index);
		if (wallList == null) {
			wallList = new LinkedList<>();
			wallListMap.put(index, wallList);
		}
		wallList.add(wall);
		walls.add(wall);
	}

	private static void createWallGraph(FloorPlan fp) {
		// sets of walls for each horizontal and vertical line between rooms
		ArrayList<RoomRectWall> horzWalls = new ArrayList<>();
		ArrayList<RoomRectWall> vertWalls = new ArrayList<>();
		TreeMap<Integer, LinkedList<RoomRectWall>> horizontalWalls = new TreeMap<>();
		TreeMap<Integer, LinkedList<RoomRectWall>> verticalWalls = new TreeMap<>();
		for (RoomRect rr : fp.roomRects) {
			rr.flags.remove(PROCESSED); // reset processed flags, a processed rr will be ignored as neighbor
		}
		for (RoomRect rr : fp.roomRects) {
			rr.flags.add(PROCESSED);

			EnumMap<Side, Integer> sideSnapIndex = new EnumMap<>(Side.class);
			sideSnapIndex.put(Side.Left, fp.snapGrid.getXSnapIndex(rr.sidePos(Side.Left)));
			sideSnapIndex.put(Side.Right, fp.snapGrid.getXSnapIndex(rr.sidePos(Side.Right)));
			sideSnapIndex.put(Side.Top, fp.snapGrid.getYSnapIndex(rr.sidePos(Side.Top)));
			sideSnapIndex.put(Side.Bottom, fp.snapGrid.getYSnapIndex(rr.sidePos(Side.Bottom)));

			// check if side belongs to outer wall (no neighbors) or add walls between each diff room neighbors otherwise
			for (Side side : Side.values()) {
				LineSegment rrSide = rr.sideLineSegment(side);
				TreeMap<Integer, LinkedList<RoomRectWall>> wallTreeMap = horizontalWalls;
				ArrayList<RoomRectWall> walls = horzWalls;
				if (side.isLeftOrRight) {
					wallTreeMap = verticalWalls;
					walls = vertWalls;
				}

				List<WallSide> outerWallsTouching = fp.floorPlanPoly.edgesTouching(rrSide);
				for (WallSide wall : outerWallsTouching) {
					LineSegment overlap = wall.overlap(rrSide);
					RoomRectWall rrw = new RoomRectWall(wall.type, rr, overlap);
					addToWallListMap(wallTreeMap, sideSnapIndex.get(side), walls, rrw);
				}

				for (RoomRect nb : rr.sideNeighbors(side)) {
					if (!nb.flags.contains(PROCESSED) && !nb.isAssignedLike(rr)) {
						LineSegment overlap = rrSide.overlap(nb.sideLineSegment(side.opposite()));

						RoomRectWall rrw = new RoomRectWall(WallType.INTERNAL, rr, nb, overlap);
						addToWallListMap(wallTreeMap, sideSnapIndex.get(side), walls, rrw);
					}
				}
			}
		}

		TreeMap<Integer, WallNode> wallNodeMap = new TreeMap<>();

		// go through all walls, find or create the according corners (WallNodes) and make a connection
		fp.walls.clear();
		for (RoomRectWall wall : horzWalls) {
			fp.walls.add(wall);
			createWallNodes(fp.snapGrid, wall, true, wallNodeMap);
		}

		for (RoomRectWall wall : vertWalls) {
			fp.walls.add(wall);
			createWallNodes(fp.snapGrid, wall, false, wallNodeMap);
		}

		fp.wallNodes.clear();
		fp.wallNodes.addAll(wallNodeMap.values());

		// all the rooms have their wall node root, now they have to make their own loop graph
		for (Room room : fp.rooms) {
			room.updateWalls();
		}
	}

	/**
	 * Adds two nodes from a wall segment, if they don't exist yet in the map
	 *
	 * @param wall horizontal or vertical Wall segment from the roomrects
	 * @param wallIsHorizontal orientation
	 * @param wallNodeMap map of wall nodes
	 */
	private static void createWallNodes(SnapGrid snapGrid, RoomRectWall wall, boolean wallIsHorizontal, TreeMap<Integer, WallNode> wallNodeMap) {
		Vector2D nodeAPos = wallIsHorizontal ? new Vector2D(wall.min, wall.pos) : new Vector2D(wall.pos, wall.min);
		Vector2D nodeBPos = wallIsHorizontal ? new Vector2D(wall.max, wall.pos) : new Vector2D(wall.pos, wall.max);

		int indexA = vectorToHash(nodeAPos.x, nodeAPos.y, snapGrid);
		int indexB = vectorToHash(nodeBPos.x, nodeBPos.y, snapGrid);

		final WallNode nodeA = getNodeOrPut(wallNodeMap, indexA, nodeAPos);
		final WallNode nodeB = getNodeOrPut(wallNodeMap, indexB, nodeBPos);

		// add roomrects and rooms for this corner node
		nodeA.roomRects.add(wall.rrA);
		nodeB.roomRects.add(wall.rrA);

		wall.rrA.assignedTo().ifPresent(room -> {
			nodeA.rooms.add(room);
			nodeB.rooms.add(room);
			room.setRootWallNode(nodeA);
		});

		if (wall.rrB != null) {
			nodeA.roomRects.add(wall.rrB);
			nodeB.roomRects.add(wall.rrB);

			wall.rrB.assignedTo().ifPresent(room -> {
				nodeA.rooms.add(room);
				nodeB.rooms.add(room);
				room.setRootWallNode(nodeA);
			});
		}

		// connect the two corners
		nodeA.neighbors.put(nodeB, new WallNodeNeighbor(nodeB, wall));
		nodeB.neighbors.put(nodeA, new WallNodeNeighbor(nodeA, wall));
	}

	private static WallNode getNodeOrPut(TreeMap<Integer, WallNode> wallNodeMap, int index, Vector2D nodePos) {
		WallNode node = wallNodeMap.get(index);
		if (node == null) {
			node = new WallNode(nodePos, index);
			wallNodeMap.put(index, node);
		}
		return node;
	}

	public static int vectorToHash(float x, float y, SnapGrid snapGrid) {
		// avoid marginal difference < 0.01
		int a = snapGrid.getXSnapIndex(x);
		int b = snapGrid.getYSnapIndex(y);

		// map to positive numbers
		if (a >= 0)
			a *= 2;
		else
			a = -a * 2 - 1;

		if (b >= 0)
			b *= 2;
		else
			b = -b * 2 - 1;

		// map to one number
		return (a + b) * (a + b + 1) / 2 + b;
	}
}
