/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architecttest.render.drawers.floorplan;

import architect.floorplan.FloorPlan;
import architect.math.Rectangle;
import architect.snapgrid.SnapGrid;
import static architecttest.render.Shapes.drawDashedLine;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glLineWidth;
import org.lwjgl.util.vector.Vector3f;

/**
 *
 * @author AK47
 */
class SnapLinesDrawer extends Drawer {

	@Override
	public void drawForFloorPlan(FloorPlan floorPlan) {
		SnapGrid snapGrid = floorPlan.snapGrid;
		Rectangle boundingRect = floorPlan.floorPlanPoly.boundingRect();

		glColor3f(0.3f, 0.3f, 0.3f);
		glLineWidth(1.5f);
		for (float y : snapGrid.getYSnapLines()) {
			drawDashedLine(new Vector3f(boundingRect.minX - 1, y, 0),
						   new Vector3f(boundingRect.maxX + 1, y, 0));
		}
		for (float x : snapGrid.getXSnapLines()) {
			drawDashedLine(new Vector3f(x, boundingRect.minY - 1, 0),
						   new Vector3f(x, boundingRect.maxY + 1, 0));
		}
		glLineWidth(1.0f);
	}

}
