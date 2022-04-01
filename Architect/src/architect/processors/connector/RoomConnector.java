/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package architect.processors.connector;

import static architect.Constants.*;
import architect.math.segments.Orientation;
import architect.math.Rectangle;
import architect.math.Vector2D;
import architect.processors.FloorPlanProcessor;
import architect.processors.helpers.Cutter;
import architect.processors.helpers.Deisolator;
import architect.processors.helpers.Simplifier;
import architect.processors.helpers.WallCreator;
import architect.room.Room;
import architect.room.RoomRect;
import architect.room.RoomType;
import architect.snapgrid.SnapRule;
import static architect.utils.ConstructionFlags.Flag.*;
import architect.walls.RoomRectWall;
import architect.walls.WallNode;
import architect.walls.WallNodeNeighbor;
import architect.walls.WallType;
import static java.lang.Math.max;
import static java.lang.Math.min;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;

/**
 *
 * @author VTPlusAKnauer
 */
public class RoomConnector extends FloorPlanProcessor {

	private final static Logger LOGGER = getLogger(RoomConnector.class.getName());

	private ArrayList<HashSet<Room>> publicRoomIslands;

	private static int hallwaysCreated = 0;
	HashSet<Room> entranceIsland = null;

	@Override
	protected void process() {
		Simplifier.simplifyAll(fp);
		WallCreator.updateRoomsAndWalls(fp);
		publicRoomIslands = null;

		/**
		 * ***************************** collect public rooms to neighbor clusters *******************************
		 */
		publicRoomIslands = new ArrayList<>();
		for (Room room : fp.rooms) {
			room.flags.remove(PROCESSED);
		}

		entranceIsland = null;
		for (Room room : fp.rooms) {
			if (!room.flags.contains(PROCESSED) && room.stats().isPublic) { // isPublic -> try to connect to other publics
				HashSet<Room> island = new HashSet<>();
				collectPublicNeighbors(room, island);

				if (entranceIsland == null) {
					for (Room publicRoom : island) {
						if (publicRoom.type() == RoomType.Hallway) {
							entranceIsland = island;
							break;
						}
					}
				}
				if (entranceIsland != island)
					publicRoomIslands.add(island);
			}
		}

		if (entranceIsland == null)
			throw new RuntimeException("No hallway (entrance) public room found");

		if (!DEV_MODE_HALLWAYCREATOR_STEPS) {
			//	while (!publicRoomIslands.isEmpty()) {
			connectPublicRoomStep();
			//	}
		}

		Optional<Room> unconnectedPrivate = findUnconnectedPrivateRoom();
		while (unconnectedPrivate.isPresent()) {
			Simplifier.simplifyAll(fp);
			WallCreator.updateRoomsAndWalls(fp);
			connectPrivateRoom(unconnectedPrivate.get());
			unconnectedPrivate = findUnconnectedPrivateRoom();
		}
	}

	public Optional<Room> findUnconnectedPrivateRoom() {
		for (Room room : fp.rooms) {
			if (!room.stats().isPublic
				&& !room.canHaveDoorWithNeighborMatching(neighbor -> neighbor.stats().isPublic)) {
				return Optional.of(room);
			}
		}

		return Optional.empty();
	}

	public void connectPublicRoomStep() {
		if (publicRoomIslands == null || publicRoomIslands.isEmpty())
			return;

		Room startRoom = entranceIsland.iterator().next();

		float shortestPathCost = Float.MAX_VALUE;
		HashSet<Room> closestIsland = null;
		boolean alreadyConnected = false;
		Path path = null;
		for (HashSet<Room> island : publicRoomIslands) {
			Room endRoom = island.iterator().next();
			/**
			 * *********************** check corners first ************************
			 */
			if (startRoom.isTouchingSides(endRoom)) { // toching corners -> cut connector		
				Cutter.cutConnector(fp, startRoom, endRoom);
				closestIsland = island;
				alreadyConnected = true;
				break;
			}

			HashSet<Room> bothIslands = new HashSet<>(entranceIsland);
			bothIslands.addAll(island);
			path = PathFinder.find(fp, startRoom, endRoom, new RoomIslandCostJudge(bothIslands),
								   new RoomIslandPathTrimmer(entranceIsland), new RoomIslandPathTrimmer(island));

			//TODO: ?? corner was checked ??
			if (path.startNode == path.endNode) {
				Cutter.cutConnector(fp, path.startRoom, path.endRoom);
				closestIsland = island;
				alreadyConnected = true;
				break;
			}

			if (path.cost < shortestPathCost) {
				closestIsland = island;
			}
		}

		if (!alreadyConnected) {
			Room hallway = createHallway(path);
			if (hallway != null) {
				Deisolator.resolveRoomRectIsolation(fp.rooms);
				Simplifier.simplifyAll(fp);
			}
		}

		//	WallCreator.updateRoomsAndWalls(fp);
		publicRoomIslands.remove(closestIsland);
	}

	private static void collectPublicNeighbors(Room room, HashSet<Room> list) {
		list.add(room);
		room.flags.add(PROCESSED);

		for (Room neighbor : room.neighbors()) {
			if (room.canHaveDoorWith(neighbor) && neighbor.stats().isPublic && !neighbor.flags.contains(PROCESSED)) {
				collectPublicNeighbors(neighbor, list);
			}
		}
	}

	private Room createHallway(Path path) {
		/**
		 * **************************** create cutters for the corridor from the path *****************************
		 */
		ArrayList<Rectangle> corridorRects = new ArrayList<>();

		// create a cutter for each path segment
		WallNode node = path.startNode;
		Rectangle preRect = null;
		Orientation previousOrientation = Orientation.Horizontal;
		while (node.pathChild != null) {
			WallNode nextNode = node.pathChild;
			WallNodeNeighbor neighbor = node.neighbors.get(nextNode);
			RoomRectWall wall = neighbor.getWall();

			Vector2D posA = node.position;
			Vector2D posB = nextNode.position;

			// first, this rectangle is just a line
			float minX = min(posA.x, posB.x);
			float maxX = max(posA.x, posB.x);
			float minY = min(posA.y, posB.y);
			float maxY = max(posA.y, posB.y);

			// make the corridor segment from the line
			if (wall.isVertical()) {
				maxX += fp.attribs.hallwayHalfWidth;
				minX -= fp.attribs.hallwayHalfWidth;
			}
			else {
				maxY += fp.attribs.hallwayHalfWidth;
				minY -= fp.attribs.hallwayHalfWidth;
			}

			Rectangle corridor = fp.floorPlanPoly.moveInside(new Rectangle(minX, minY, maxX - minX, maxY - minY));

			// if this corridor segment is between two rooms, try to cut more from the bigger room while assuring minRoomSideSize
			if (wall.type == WallType.INTERNAL && wall.isVertical()) {
				Rectangle brLeft = wall.leftRoomRect().assignedTo().get().getOptimalRect().rect;
				Rectangle brRight = wall.rightRoomRect().assignedTo().get().getOptimalRect().rect;

				float leftMargin = min(wall.pos, min(brLeft.maxX, brLeft.minX + fp.attribs.minRoomSideSize));
				float rightMargin = max(wall.pos, max(brRight.minX, brRight.maxX - fp.attribs.minRoomSideSize));
				leftMargin = max(leftMargin, wall.pos - corridor.width);
				rightMargin = min(rightMargin, wall.pos + corridor.width);

				if (rightMargin - leftMargin < corridor.width) { // no leeway at all -> move to the centerPoint
					corridor = corridor.movedTo((leftMargin + (rightMargin - corridor.width)) * 0.5f, corridor.minY);
				}
				else {
					if (wall.pos - corridor.width > corridor.minX) {
						corridor = corridor.movedTo(wall.pos - corridor.width, corridor.minY); // move right
					}
					else if (wall.pos + corridor.width < corridor.maxX) {
						corridor = corridor.movedTo(wall.pos, corridor.minY); // move left
					}
					else {
						corridor = corridor.movedTo((leftMargin + (rightMargin - corridor.width)) * 0.5f, corridor.minY);
					}
				}
			}
			else if (wall.type == WallType.INTERNAL && wall.isHorizontal()) {
				Rectangle brBottom = wall.lowerRoomRect().assignedTo().get().getOptimalRect().rect;
				Rectangle brTop = wall.upperRoomRect().assignedTo().get().getOptimalRect().rect;

				float lowerMargin = min(wall.pos, min(brBottom.maxY, brBottom.minY + fp.attribs.minRoomSideSize));
				float upperMargin = max(wall.pos, max(brTop.minY, brTop.maxY - fp.attribs.minRoomSideSize));
				lowerMargin = max(lowerMargin, wall.pos - corridor.height);
				upperMargin = min(upperMargin, wall.pos + corridor.height);

				if (upperMargin - lowerMargin < corridor.height) { // no leeway at all -> move to the centerPoint
					corridor = corridor.movedTo(corridor.minX, (lowerMargin + (upperMargin - corridor.height)) * 0.5f);
				}
				else {
					if (wall.pos - corridor.height > corridor.minY) {
						corridor = corridor.movedTo(corridor.minX, wall.pos - corridor.height); // move up
					}
					else if (wall.pos + corridor.height < corridor.maxY) {
						corridor = corridor.movedTo(corridor.minX, wall.pos); // move down
					}
					else {
						corridor = corridor.movedTo(corridor.minX, (lowerMargin + (upperMargin - corridor.height)) * 0.5f);
					}
				}
			}

			// make sure two connecting corridor cutters connect seamlessly						
			if (preRect != null && node.pathParent != null) { // have a rectangle before me, scale me if it is a corner			
				float newMinX = corridor.minX, newMinY = corridor.minY,
						newMaxX = corridor.maxX, newMaxY = corridor.maxY;

				if (wall.isHorizontal() && wall.orientation != previousOrientation) {
					if (posA.x < posB.x - EPSILON)
						newMinX = min(newMinX, preRect.minX); // going right
					else
						newMaxX = max(newMaxX, preRect.maxX); // going left
				}
				else if (wall.isVertical() && wall.orientation != previousOrientation) {
					if (posA.y < posB.y - EPSILON)
						newMinY = min(newMinY, preRect.minY); // going up
					else
						newMaxY = max(newMaxY, preRect.maxY); // going down
				}
				else if (wall.isVertical() && wall.orientation == previousOrientation) {
					newMinX = preRect.minX;
					newMaxX = preRect.maxX;
				}
				else { // wall is HORIZONTAL && previousOrientation is HORIZONTAL
					newMinY = preRect.minY;
					newMaxY = preRect.maxY;
				}

				corridor = new Rectangle(newMinX, newMinY, max(0, newMaxX - newMinX), max(0, newMaxY - newMinY));
			}

			if (corridor.area() > EPSILON) {
				// scale to snap lines, while maintaning minimal width or height			
				if (wall.isHorizontal())
					corridor = fp.snapGrid.snap(corridor, SnapRule.DONT_SHRINK_HEIGHT);
				else
					corridor = fp.snapGrid.snap(corridor, SnapRule.DONT_SHRINK_WIDTH);

				// adjust prerect so that we connect
				if (preRect != null) {
					if (wall.isHorizontal() && wall.orientation != previousOrientation) {
						if (preRect.maxY < corridor.maxY - EPSILON)
							preRect = preRect.movedMaxYTo(corridor.maxY);
						else if (preRect.minY > corridor.minY + EPSILON)
							preRect = preRect.movedMinYTo(corridor.minY);
					}
					else if (wall.isVertical() && wall.orientation != previousOrientation) {
						if (preRect.maxX < corridor.maxX - EPSILON)
							preRect = preRect.movedMaxXTo(corridor.maxX);
						else if (preRect.minX > corridor.minX + EPSILON)
							preRect = preRect.movedMinXTo(corridor.minX);
					}
					corridorRects.set(corridorRects.size() - 1, preRect);
				}

				corridorRects.add(corridor);
				previousOrientation = wall.orientation;
				preRect = corridor;
			}

			node = nextNode;
		}

		unoverlapCorridorRectsWithStartAndDestRoom(path, corridorRects);

		Hallway hallway = createHallwayObj(path, corridorRects);
		return hallway.room;
	}

	private void unoverlapCorridorRectsWithStartAndDestRoom(Path path, ArrayList<Rectangle> corridorRects) {
		Rectangle firstRect = corridorRects.get(0);
		Rectangle lastRect = corridorRects.get(corridorRects.size() - 1);

		if (firstRect == lastRect) { // cut to not overlap with src and dest room
			if (path.startRoom.overlapArea(firstRect) > EPSILON) {
				for (RoomRect rr : path.startRoom.subRects()) {
					//TODO: change bounidng rect to edgeLoop
					firstRect = firstRect.movedOutside(rr, fp.floorPlanPoly.boundingRect());
				}
			}
			if (path.endRoom.overlapArea(lastRect) > EPSILON) {
				for (RoomRect rr : path.endRoom.subRects()) {
					//TODO: change bounidng rect to edgeLoop
					firstRect = firstRect.movedOutside(rr, fp.floorPlanPoly.boundingRect());
				}
			}

			firstRect = fp.snapGrid.snap(firstRect, SnapRule.DONT_SHRINK_AT_ALL);
			corridorRects.set(0, firstRect);
		}
	}

	private Hallway createHallwayObj(Path path, ArrayList<Rectangle> corridorRects) {
		corridorRects = Rectangle.nonOverlappingSet(corridorRects);
		Room room = new Room(RoomType.Hallway, fp.roomStats.get(RoomType.Hallway), "hw" + hallwaysCreated++, fp.attribs);
		fp.rooms.add(room);

		Hallway hallway = new Hallway(room, path, corridorRects);

		cutHallway(hallway);
		connectHallwayEnds(hallway);

		return hallway;
	}

	private void cutHallway(Hallway hallway) {
		if (hallway.corridorRects.size() > 0) {
			hallway.room.flags.add(DONT_CUT);
			ArrayList<RoomRect> insideCorridor = Cutter.cut(fp, hallway.corridorRects);
			hallway.room.flags.remove(DONT_CUT);
			hallway.room.addAllSubRects(insideCorridor);
		}

		hallway.room.addImportantRects(hallway.corridorRects);
	}

	private void connectHallwayEnds(Hallway hallway) {
		Cutter.cutConnector(fp, hallway.room, hallway.path.startRoom);
		Cutter.cutConnector(fp, hallway.room, hallway.path.endRoom);
	}

	private void connectPrivateRoom(Room room) {
		Optional<Room> publicNeighbor = bestPublicNeighbor(room);

		if (publicNeighbor.isPresent())
			Cutter.cutConnector(fp, room, publicNeighbor.get());
		else {
			connectRoomToClosestPublic(room);
		}
	}

	private Optional<Room> bestPublicNeighbor(Room room) {
		Room bestNeighborForDoor = null;
		float maxOverlap = 0;
		for (Room neighbor : room.neighbors()) {
			if (neighbor.stats().isPublic) {
				if (room.canHaveDoorWith(neighbor))
					return Optional.of(neighbor);
				else {
					float overlap = room.commonPerimeter(neighbor);
					if (overlap > maxOverlap) {
						maxOverlap = overlap;
						bestNeighborForDoor = neighbor;
					}
				}
			}
		}

		return Optional.ofNullable(bestNeighborForDoor);
	}

	private void connectRoomToClosestPublic(Room room) {
		closestPublic(room).ifPresent(publicRoom -> connectRooms(room, publicRoom));
	}

	private Optional<Room> closestPublic(Room room) {
		Room result = null;
		float minDist = Float.MAX_VALUE;
		for (Room other : fp.rooms) {
			if (other.stats().isPublic && other != room) {
				float dist = other.boundingRect().manhattanDistBetweenSides(room.boundingRect());
				if (dist < minDist) {
					minDist = dist;
					result = other;
				}
			}
		}

		return Optional.ofNullable(result);
	}

	private void connectRooms(Room startRoom, Room endRoom) {
		Path path = PathFinder.find(fp, startRoom, endRoom, DefaultCostJudge.instance(),
									new RoomPathTrimmer(startRoom), new RoomPathTrimmer(endRoom));
		Room hallway = createHallway(path);
		if (hallway != null) {
			Deisolator.resolveRoomRectIsolation(fp.rooms);
		}
	}
}
