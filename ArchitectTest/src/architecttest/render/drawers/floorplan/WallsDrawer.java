/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architecttest.render.drawers.floorplan;

import architect.floorplan.FloorPlan;
import architect.math.Rectangle;
import architect.room.Room;
import architect.room.RoomRect;
import static architecttest.render.Shapes.*;
import architecttest.render.utils.Color;
import static architecttest.render.utils.Color.*;
import static org.lwjgl.opengl.GL11.*;

/**
 *
 * @author VTPlusAKnauer
 */
class WallsDrawer extends Drawer {

	static final Color roomColors[] = {Red, Green, Blue, Yellow, Magenta, Cyan, LightRed,
									   LightGreen, LightBlue, Grey, Orange, Pink, GreenYellowish,
									   GreenBluish, Violet, BlueGreenish};

	@Override
	public void drawForFloorPlan(FloorPlan floorPlan) {
		glColor3f(1, 1, 1);
		glLineWidth(2);
		drawPoly(floorPlan.floorPlanPoly);

		// draw windowless outer sides
		glLineWidth(1);
		drawShadedWalls(floorPlan.floorPlanPoly, 0.3f);

		// draw rooms		
		if (floorPlan.rooms != null) {
			int roomIdx = 0;
			for (Room room : floorPlan.rooms) {
				Color color = roomColors[(floorPlan.id + roomIdx) % roomColors.length];
				drawRoom(room, color);
				roomIdx++;
			}
		}

		// draw roomrects
		glColor3f(0.75f, 0.75f, 0.75f);
		for (RoomRect rr : floorPlan.roomRects) {
			drawRect((Rectangle) rr, 0);
		}

		// draw entrance
		glLineWidth(3);
		glColor4f(1, 0.5f, 0.5f, 0.75f);
		drawRectDashed(floorPlan.floorPlanPoly.entrance, 0.0f, 0.005);
	}

	private void drawRoom(Room room, Color color) {
		float alpha = 0.5f;
		if (!room.stats().needsWindow)
			alpha = 0.2f;
		glColor4f(color.red, color.green, color.blue, alpha);
		for (RoomRect rr : room.subRects()) {
			drawRectFilled(rr);
		}
	}

}
