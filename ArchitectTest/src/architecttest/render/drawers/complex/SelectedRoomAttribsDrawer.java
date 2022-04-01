/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architecttest.render.drawers.complex;

import architect.math.Vector2D;
import architect.room.Room;
import static architecttest.render.Shapes.drawArrow;
import architecttest.render.text.GLText;
import architect.walls.RoomRectWall;
import architect.walls.WallSide;
import architect.walls.WallType;
import static java.lang.Math.abs;
import static java.lang.Math.sin;
import static java.lang.System.currentTimeMillis;
import static org.lwjgl.opengl.GL11.*;

/**
 *
 * @author AK47
 */
class SelectedRoomAttribsDrawer extends Drawer {

	@Override
	public void drawForSelectedRoom(Room selectedRoom) {
		glLineWidth(4);

		// draw wall roomRect connections 
		for (RoomRectWall wall : selectedRoom.getWallLoop()) {
			if (wall.type == WallType.INTERNAL) {
				drawArrow(wall.center(), wall.rrA.centerPoint(), GLText.getScale() / 50.0f, 0.1f);
				drawArrow(wall.center(), wall.rrB.centerPoint(), GLText.getScale() / 50.0f, 0.1f);
			}
		}

		// draw wall sides
		glBegin(GL_LINES);
		for (WallSide wall : selectedRoom.getWallSides()) {
			Vector2D normal = wall.normal.scaled(0.2f * (float) (abs((sin(currentTimeMillis() * 0.005)))));
			if (wall.isHorizontal()) {
				glColor3f(0, 1, 1);
				glVertex3f(wall.min + normal.x, wall.pos + normal.y, 0.1f);
				glColor3f(1, 0, 0);
				glVertex3f(wall.max + normal.x, wall.pos + normal.y, 0.1f);
			}
			else {
				glColor3f(0, 1, 1);
				glVertex3f(wall.pos + normal.x, wall.min + normal.y, 0.1f);
				glColor3f(1, 0, 0);
				glVertex3f(wall.pos + normal.x, wall.max + normal.y, 0.1f);
			}
		}
		glEnd();

		glLineWidth(1);
	}

}
