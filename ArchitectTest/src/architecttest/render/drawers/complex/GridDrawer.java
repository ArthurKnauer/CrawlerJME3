/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architecttest.render.drawers.complex;

import architect.floorplan.FloorPlan;
import architect.math.Rectangle;
import architecttest.render.text.GLText;
import static architecttest.render.utils.Color.Green;
import static org.lwjgl.opengl.GL11.*;

/**
 *
 * @author AK47
 */
class GridDrawer extends Drawer {

	@Override
	public void drawForComplexFloorPlan(FloorPlan complexFloorPlan) {
		// draw grid lines
		glColor3f(0.0f, 0.3f, 0.0f);
		glBegin(GL_LINES);
		for (float meters = -50; meters < 50; meters += 1.0) {
			glVertex3f(meters, -50, 0);
			glVertex3f(meters, 50, 0);
			glVertex3f(-50, meters, 0);
			glVertex3f(50, meters, 0);
		}
		glEnd();

		// draw grid pos numbers
		Rectangle boundingRect = complexFloorPlan.getBoundingRect();
		GLText.beginForWorld();
		for (int x = (int) boundingRect.minX - 1; x < boundingRect.maxX + 1; x++) {
			GLText.print(Green, "" + x, x, boundingRect.minY - 0.75f, 0);
		}
		for (int y = (int) boundingRect.minY - 1; y < boundingRect.maxY + 1; y++) {
			GLText.print(Green, "" + y, boundingRect.maxX + 0.75f, y, 0);
		}
		GLText.end();
	}

}
