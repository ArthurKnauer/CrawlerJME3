/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architecttest.render.drawers.floorplan;

import architect.floorplan.FloorPlan;
import architect.math.Vector2D;
import architecttest.render.text.GLText;
import static architecttest.render.utils.Color.LightBlue;
import architect.walls.RoomRectWall;
import static org.lwjgl.opengl.GL11.*;

/**
 *
 * @author AK47
 */
class WallAttribsDrawer extends Drawer {

	@Override
	public void drawForFloorPlan(FloorPlan floorPlan) {
		glLineWidth(4);

		for (RoomRectWall wall : floorPlan.walls) {
			Vector2D min = wall.minPoint();
			Vector2D max = wall.maxPoint();
			glBegin(GL_LINES);
			glColor3f(0, 0, 1);
			glVertex3f(min.x, min.y, 0);
			glColor3f(1, 0, 1);
			glVertex3f(max.x, max.y, 0);
			glEnd();

			GLText.beginForWorld();
			GLText.print(LightBlue, wall.id + "", wall.center().x, wall.center().y, 0.1f);
			GLText.end();
		}
	}

}
