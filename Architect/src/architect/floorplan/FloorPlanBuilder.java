package architect.floorplan;

import architect.room.Room;
import architect.room.RoomType;
import architect.snapgrid.SnapGrid;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

/**
 *
 * @author VTPlusAKnauer
 */
public class FloorPlanBuilder {

	public static FloorPlan fromScript(String fileName) {
		LuaValue globals = JsePlatform.standardGlobals();
		globals.get("dofile").call(LuaValue.valueOf(fileName));
		LuaFunction createArchitect = (LuaFunction) globals.get("createFloorPlan");
		return (FloorPlan) createArchitect.call().touserdata(FloorPlan.class);
	}

	public static FloorPlan fromSingleRoom(FloorPlan floorPlan, Room room) {
		FloorPlan result = new FloorPlan(FloorPlanPolyCreator.createPolyFromRoom(room, floorPlan.attribs, floorPlan.snapGrid),
										 HousingUnitType.fromRoomType(room.type()),
										 RoomsToBuild.Builder.start().build(), floorPlan.attribs, floorPlan.snapGrid);
		result.rooms.add(room);
		return result;
	}

	public static FloorPlan fromPoly(Room room) {
		FloorPlanAttribs attribs = FloorPlanAttribs.Builder.start().build();
		SnapGrid snapGrid = new SnapGrid(attribs.minSnapLineDist);
		return start()
				.setFloorPlanAttribs(attribs)
				.setSnapGrid(snapGrid)
				.setFloorPlanPoly(FloorPlanPolyCreator.createPolyFromRoom(room, attribs, snapGrid))
				.setHousingUnitType(HousingUnitType.fromRoomType(room.type()))
				.setRoomsToBuild(RoomsToBuild.Builder.start()
						.addRoomType(RoomType.LivingRoom)
						.addRoomType(RoomType.Bedroom)
						.addRoomType(RoomType.Bedroom)
						.addRoomType(RoomType.Kitchen)
						.addRoomType(RoomType.Bathroom).build())
				.build();
	}

	public static FloorPlanBuilder start() {
		return new FloorPlanBuilder();
	}

	private FloorPlanPoly floorPlanPoly;
	private RoomsToBuild roomsToBuild;
	private FloorPlanAttribs attribs;
	private SnapGrid snapGrid;
	private HousingUnitType housingUnitType;

	private FloorPlanBuilder() {
	}

	public FloorPlan build() {
		if (floorPlanPoly == null)
			throw new IllegalStateException("Trying to create FloorPlan without FloorPlanPoly");
		if (roomsToBuild == null)
			throw new IllegalStateException("Trying to create FloorPlan without RoomsToBuild");
		if (attribs == null)
			throw new IllegalStateException("Trying to create FloorPlan without FloorPlanAttribs");
		if (snapGrid == null)
			throw new IllegalStateException("Trying to create FloorPlan without SnapGrid");

		return new FloorPlan(floorPlanPoly, housingUnitType, roomsToBuild, attribs, snapGrid);
	}

	public FloorPlanBuilder setFloorPlanPoly(FloorPlanPoly floorPlanPoly) {
		this.floorPlanPoly = floorPlanPoly;
		return this;
	}

	public FloorPlanBuilder setRoomsToBuild(RoomsToBuild roomsToBuild) {
		this.roomsToBuild = roomsToBuild;
		return this;
	}

	public FloorPlanBuilder setFloorPlanAttribs(FloorPlanAttribs attribs) {
		this.attribs = attribs;
		return this;
	}

	public FloorPlanBuilder setSnapGrid(SnapGrid snapGrid) {
		this.snapGrid = snapGrid;
		return this;
	}
	
	public FloorPlanBuilder setHousingUnitType(HousingUnitType housingUnitType) {
		this.housingUnitType = housingUnitType;
		return this;
	}

}
