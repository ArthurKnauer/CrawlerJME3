/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architecttest.render.drawers.floorplan;

import architect.floorplan.FloorPlan;
import architect.room.RoomRect;
import architecttest.render.text.GLText;
import static architecttest.render.utils.Color.White;
import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 *
 * @author Arthur
 */
public class RoomRectInfoDrawer extends Drawer {

	@Override
	void drawForFloorPlan(FloorPlan floorPlan) {
		for (RoomRect rr : floorPlan.roomRects) {
			printRoomRectID(rr);
		}
	}

	private void printRoomRectID(RoomRect rr) {
		GLText.beginForWorld();
		GLText.print(White, "" + rr.getID(), max(rr.minX + 0.15f, rr.maxX - 0.35f), min(rr.maxY - 0.15f, rr.minY + 0.25f), 0);
		GLText.end();
	}

}
