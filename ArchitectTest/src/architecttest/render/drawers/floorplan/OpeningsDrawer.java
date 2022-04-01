/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architecttest.render.drawers.floorplan;

import static architect.Constants.EPSILON;
import architect.floorplan.FloorPlan;
import architect.room.Room;
import architect.walls.Opening;
import architect.walls.WallSide;
import static org.lwjgl.opengl.GL11.*;

/**
 *
 * @author AK47
 */
class OpeningsDrawer extends Drawer {

	@Override
	public void drawForFloorPlan(FloorPlan floorPlan) {
		if (floorPlan.rooms != null) {
			for (Room room : floorPlan.rooms) {
				drawRoomDoorsAndWindows(room);
			}
		}
	}

	private void drawRoomDoorsAndWindows(Room room) {
		glLineWidth(5);
		glBegin(GL_LINES);
		for (WallSide wall : room.getWallSides()) {
			for (Opening opening : wall.getOpenings().values()) {
				if (wall.isHorizontal()) {
					if (opening.bottom > EPSILON)
						glColor3f(0.25f, 0.25f, 1); // window
					else
						glColor3f(1, 1, 1); // door
					glVertex3f(wall.min + opening.start, wall.pos, 0.1f);
					glColor3f(0, 0, 1);
					glVertex3f(wall.min + opening.end, wall.pos, 0.1f);
				}
				else {
					if (opening.bottom > EPSILON)
						glColor3f(0.25f, 0.25f, 1); // window
					else
						glColor3f(1, 1, 1); // door
					glVertex3f(wall.pos, wall.min + opening.start, 0.1f);
					glColor3f(0, 0, 1);
					glVertex3f(wall.pos, wall.min + opening.end, 0.1f);
				}
			}
		}
		glEnd();
	}

}
