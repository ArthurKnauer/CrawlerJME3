/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package architect.processors.wallpressure;

import static architect.Constants.EPSILON;
import architect.math.segments.Orientation;
import architect.math.Rectangle;
import architect.math.segments.Side;
import architect.math.Vector2D;
import architect.processors.FloorPlanProcessor;
import architect.processors.helpers.WallCreator;
import architect.room.Room;
import architect.room.RoomType;
import architect.walls.RoomRectWall;
import architect.walls.WallType;
import static java.lang.Math.max;
import java.util.EnumMap;

/**
 *
 * @author VTPlusAKnauer
 */
public class WallPressureBuilder extends FloorPlanProcessor {

	public final boolean canPressureHallway;

	public WallPressureBuilder(boolean canPressureHallway) {
		this.canPressureHallway = canPressureHallway;
	}

	@Override
	public void process() {
		WallCreator.updateRoomsAndWalls(fp);

		resetWallOptimalPositions();
		sumUpWallOptimalPositions();
		averageOverWallOptimalPositions();
	}

	private void resetWallOptimalPositions() {
		for (RoomRectWall wall : fp.walls) {
			wall.optimalPos = 0;
			wall.optimalPosWeights = 0;
		}
	}

	private void sumUpWallOptimalPositions() {
		for (Room room : fp.rooms) {
			if (room.getOptimalRect().isValid()) {
				EnumMap<Orientation, Float> pressureWeights = pressureWeights(room);
				addOptimalPosToWallsWithWeights(room, pressureWeights);
			}
		}
	}

	private void averageOverWallOptimalPositions() {
		for (RoomRectWall wall : fp.walls) {
			if (wall.optimalPosWeights < EPSILON) {
				wall.optimalPos = wall.pos;
			}
			else
				wall.optimalPos /= wall.optimalPosWeights;

			// dont press into entrance
			if (wall.belongsToType(RoomType.Hallway)) {
				wall.avoidOptimalPosOverlap(fp.floorPlanPoly.entrance);
			}
		}
	}

	private EnumMap<Orientation, Float> pressureWeights(Room room) {
		Vector2D totalPerimeter = room.perimeter();
		Vector2D diffRoomPerimeter = room.perimeterWithNeighbors();
		Rectangle optimalRect = room.getOptimalRect().rect;
		Rectangle boundingRect = room.boundingRect();

		EnumMap<Orientation, Float> pressureWeights = new EnumMap<>(Orientation.class);
		pressureWeights.put(Orientation.Horizontal,
							(diffRoomPerimeter.x / totalPerimeter.x) * (optimalRect.height / boundingRect.height));
		pressureWeights.put(Orientation.Vertical,
							(diffRoomPerimeter.y / totalPerimeter.y) * (optimalRect.width / boundingRect.width));
		return pressureWeights;
	}

	private void addOptimalPosToWallsWithWeights(Room room, EnumMap<Orientation, Float> weights) {
		Rectangle optimalRect = room.getOptimalRect().rect;
		for (RoomRectWall wall : room.getWallLoop()) {
			if (wall.type == WallType.INTERNAL && (canPressureHallway || !wall.belongsToType(RoomType.Hallway))) {
				float weight = weights.get(wall.orientation)
							   * max(0, optimalRect.overlap(wall)) / optimalRect.sideLength(wall.orientation);

				Side roomSide = wall.sideOf(room);
				float desiredWallPos = optimalRect.sidePos(roomSide);

				wall.optimalPos += desiredWallPos * weight;
				wall.optimalPosWeights += weight;
			}
		}
	}
}
