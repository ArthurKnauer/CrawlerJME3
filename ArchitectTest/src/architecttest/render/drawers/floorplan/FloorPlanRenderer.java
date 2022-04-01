package architecttest.render.drawers.floorplan;

import architect.floorplan.FloorPlan;
import architecttest.input.KeyboardInput;
import static org.lwjgl.opengl.GL11.glLineWidth;

public class FloorPlanRenderer {

	private final WallsDrawer walls = new WallsDrawer();
	private final RoomInfoDrawer roomInfo = new RoomInfoDrawer();
	private final RoomRectInfoDrawer rrinfo = new RoomRectInfoDrawer();
	private final ErrorsDrawer errors = new ErrorsDrawer();
	private final SnapLinesDrawer snapLines = new SnapLinesDrawer();
	private final OpeningsDrawer openings = new OpeningsDrawer();
	private final WallAttribsDrawer wallAttribs = new WallAttribsDrawer();
	
	public FloorPlanRenderer(KeyboardInput keyboardInput) {
		roomInfo.disable();
		rrinfo.disable();
		snapLines.disable();
		wallAttribs.disable();
		
		keyboardInput.bindMethod("RoomInfo", roomInfo::toggle);
		keyboardInput.bindMethod("RoomRectInfo", rrinfo::toggle);
		keyboardInput.bindMethod("SnapLines", snapLines::toggle);
		keyboardInput.bindMethod("Openings", openings::toggle);
		keyboardInput.bindMethod("WallAttribs", wallAttribs::toggle);
	}

	public void draw(FloorPlan floorPlan) {
		glLineWidth(1);

		roomInfo.draw(floorPlan);
		rrinfo.draw(floorPlan);
		snapLines.draw(floorPlan);
		walls.draw(floorPlan);
		errors.draw(floorPlan);
		openings.draw(floorPlan);
		wallAttribs.draw(floorPlan);
	}
}
