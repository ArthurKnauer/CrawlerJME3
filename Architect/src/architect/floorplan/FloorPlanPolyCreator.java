/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package architect.floorplan;

import static architect.math.segments.Orientation.*;
import architect.math.Rectangle;
import architect.math.Vector2D;
import architect.room.Room;
import architect.snapgrid.SnapGrid;
import architect.snapgrid.SnapRule;
import architect.walls.Opening;
import architect.walls.RoomRectWall;
import architect.walls.WallSide;
import architect.walls.WallType;
import java.util.ArrayList;
import java.util.HashSet;

/**
 *
 * @author VTPlusAKnauer
 */
public class FloorPlanPolyCreator {

	public static FloorPlanPoly createPolyForApartment(FloorPlanAttribs attribs, SnapGrid snapGrid) {

		HashSet<Rectangle> mainRects = new HashSet<>(3);
		mainRects.add(Rectangle.fromMinMax(-5, -5, 5, 5));
		mainRects.add(Rectangle.fromMinMax(5, -3, 8, 3));
		mainRects.add(Rectangle.fromMinMax(-8, -5, -5, 3));

		for (Rectangle rect : mainRects) {
			snapGrid.snap(rect, SnapRule.CLOSEST);
		}

		Rectangle entranceRect = new Rectangle(0, -5, attribs.hallwayWidth, attribs.hallwayWidth);
		entranceRect = snapGrid.snap(entranceRect, SnapRule.DONT_SHRINK_AT_ALL);
		ArrayList<WallSide> floorPlanWalls = new ArrayList<>(11);

		floorPlanWalls.add(new WallSide(-8, -5, -5, Horizontal, new Vector2D(0, 1), WallType.OUTER_WINDOWLESS));
		floorPlanWalls.add(new WallSide(-5, 5, -5, Horizontal, new Vector2D(0, 1), WallType.OUTER_WINDOWLESS));
		floorPlanWalls.add(new WallSide(-5, -3, 5, Vertical, new Vector2D(-1, 0), WallType.OUTER_WINDOWLESS));
		floorPlanWalls.add(new WallSide(5, 8, -3, Horizontal, new Vector2D(0, 1), WallType.OUTER_WINDOWLESS));
		floorPlanWalls.add(new WallSide(-3, 3, 8, Vertical, new Vector2D(-1, 0), WallType.OUTER_WINDOWLESS));
		floorPlanWalls.add(new WallSide(5, 8, 3, Horizontal, new Vector2D(0, -1), WallType.OUTER_WINDOWABLE));
		floorPlanWalls.add(new WallSide(3, 5, 5, Vertical, new Vector2D(-1, 0), WallType.OUTER_WINDOWABLE));
		floorPlanWalls.add(new WallSide(-5, 5, 5, Horizontal, new Vector2D(0, -1), WallType.OUTER_WINDOWABLE));
		floorPlanWalls.add(new WallSide(3, 5, -5, Vertical, new Vector2D(1, 0), WallType.OUTER_WINDOWABLE));
		floorPlanWalls.add(new WallSide(-8, -5, 3, Horizontal, new Vector2D(0, -1), WallType.OUTER_WINDOWABLE));
		floorPlanWalls.add(new WallSide(-5, 3, -8, Vertical, new Vector2D(1, 0), WallType.OUTER_WINDOWABLE));

		return new FloorPlanPoly(mainRects, floorPlanWalls, entranceRect);
	}

	public static FloorPlanPoly createPolyForComplex(FloorPlanAttribs attribs, SnapGrid snapGrid) {
		HashSet<Rectangle> mainRects = new HashSet<>(1);
		final float w = 20;
		final float h = 10;
		mainRects.add(Rectangle.fromMinMax(-w, -h, w, h));

		for (Rectangle rect : mainRects) {
			snapGrid.snap(rect, SnapRule.CLOSEST);
		}

		Rectangle entranceRect = new Rectangle(0, -h, attribs.hallwayWidth, attribs.hallwayWidth);
		entranceRect = snapGrid.snap(entranceRect, SnapRule.DONT_SHRINK_AT_ALL);
		ArrayList<WallSide> floorPlanWalls = new ArrayList<>(4);

		floorPlanWalls.add(new WallSide(-w, w, -h, Horizontal, new Vector2D(0, 1), WallType.OUTER_WINDOWABLE));
		floorPlanWalls.add(new WallSide(-h, h, w, Vertical, new Vector2D(-1, 0), WallType.OUTER_WINDOWABLE));
		floorPlanWalls.add(new WallSide(-w, w, h, Horizontal, new Vector2D(0, -1), WallType.OUTER_WINDOWABLE));
		floorPlanWalls.add(new WallSide(-h, h, -w, Vertical, new Vector2D(1, 0), WallType.OUTER_WINDOWABLE));

		return new FloorPlanPoly(mainRects, floorPlanWalls, entranceRect);
	}

	static FloorPlanPoly createPolyFromRoom(Room room, FloorPlanAttribs attribs, SnapGrid snapGrid) {
		HashSet<Rectangle> mainRects = new HashSet<>(room.subRects());
		for (Rectangle rect : mainRects) {
			snapGrid.snap(rect, SnapRule.CLOSEST);
		}

		ArrayList<WallSide> floorPlanWalls = new ArrayList<>(room.getWallLoop().size());
		for (RoomRectWall wall : room.getWallLoop()) {
			WallSide wallSide = WallSide.copyForSubFloorPlan(wall.wallSideOf(room));
			floorPlanWalls.add(wallSide);
		}

		Rectangle entranceRect = new Rectangle(0, 0, attribs.hallwayWidth, attribs.hallwayWidth);

		for (RoomRectWall wall : room.getWallLoop()) {
			WallSide wallSide = wall.wallSideOf(room);
			for (Opening opening : wallSide.getOpenings().values()) {
				if (opening.type == Opening.Type.DOOR) {
					Vector2D center = wallSide.getOpeningCenter(opening);
					Vector2D entranceCenter = center.add(wallSide.normal.scaled(entranceRect.shortestSideLength() * 0.5f));
					entranceRect = entranceRect.movedTo(entranceCenter.x - entranceRect.width * 0.5f,
														entranceCenter.y - entranceRect.height * 0.5f);
					break;
				}
			}
		}

		return new FloorPlanPoly(mainRects, floorPlanWalls, entranceRect);
	}

}
