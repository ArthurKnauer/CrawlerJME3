/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package architect.floorplan;

import architect.math.Rectangle;
import architect.room.Room;
import architect.room.RoomRect;
import architect.room.RoomStats;
import architect.room.RoomType;
import architect.snapgrid.SnapGrid;
import architect.utils.UniqueID;
import architect.walls.RoomRectWall;
import architect.walls.WallNode;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.function.Predicate;
import java.util.stream.Stream;
import lombok.Getter;

/**
 *
 * @author VTPlusAKnauer
 */
public class FloorPlan {

	public final FloorPlanPoly floorPlanPoly;
	public final SnapGrid snapGrid;

	public final HashSet<RoomRect> roomRects;
	public final HashSet<Room> rooms;

	public final RoomsToBuild roomsToBuild;
	public final EnumMap<RoomType, RoomStats> roomStats;

	public final ArrayList<RoomRectWall> walls;
	public final ArrayList<WallNode> wallNodes;

	public final FloorPlanAttribs attribs;
	
	@Getter private final HousingUnitType housingType;

	public final int id;

	public FloorPlan(FloorPlanPoly floorPlanPoly, HousingUnitType housingType, RoomsToBuild roomsToBuild, FloorPlanAttribs attribs, SnapGrid snapGrid) {
		this.id = UniqueID.nextID(getClass());

		this.floorPlanPoly = floorPlanPoly;
		this.housingType = housingType;
		this.roomsToBuild = roomsToBuild;
		this.attribs = attribs;
		this.snapGrid = snapGrid;

		roomStats = new EnumMap<>(RoomType.class);
		roomRects = new HashSet<>();
		rooms = new HashSet<>();

		wallNodes = new ArrayList<>();
		walls = new ArrayList<>();

		roomStats.put(RoomType.None, new RoomStats(1, true, RoomType.None, RoomType.None, false, 2.5f));
		roomStats.put(RoomType.Hallway, new RoomStats(0, false, RoomType.LivingRoom, RoomType.LivingRoom, true,
													  attribs.hallwayWidth - attribs.minSnapLineDist * 0.5f));
		roomStats.put(RoomType.Kitchen, new RoomStats(0.7f, true, RoomType.LivingRoom, RoomType.LivingRoom, true, 2.5f));
		roomStats.put(RoomType.LivingRoom, new RoomStats(1.5f, true, RoomType.None, RoomType.None, true, 2.5f));
		roomStats.put(RoomType.Bedroom, new RoomStats(1, true, RoomType.None, RoomType.None, false, 2.5f));
		roomStats.put(RoomType.Bathroom, new RoomStats(0.5f, false, RoomType.None, RoomType.None, false, 2.5f));
		roomStats.put(RoomType.Closet, new RoomStats(0.3f, false, RoomType.None, RoomType.None, false, 2.5f));
		roomStats.put(RoomType.Apartment, new RoomStats(1, true, RoomType.None, RoomType.None, false, 4f));

		updateRoomNeedArea();
	}

	private void updateRoomNeedArea() {
		float totalArea = floorPlanPoly.area();

		float totalRelativeArea = (float) roomsToBuild.list().stream()
				.mapToDouble(roomType -> roomStats.get(roomType).relativeArea)
				.sum();

		for (RoomStats rs : roomStats.values()) {
			rs.needsArea = totalArea * (rs.relativeArea / totalRelativeArea);
		}
	}

	public int roomsToBuildCount(Predicate<RoomStats> predicate) {
		return (int) roomsToBuild.list().stream().map(roomType -> roomStats.get(roomType)).filter(predicate).count();
	}

	public Stream<RoomStats> roomsToBuild() {
		return roomsToBuild.list().stream().map(roomType -> roomStats.get(roomType));
	}

	public Stream<RoomStats> roomsToBuild(Predicate<RoomStats> predicate) {
		return roomsToBuild.list().stream().map(roomType -> roomStats.get(roomType)).filter(predicate);
	}

	public Rectangle getBoundingRect() {
		return floorPlanPoly.boundingRect();
	}
}
