/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architecttest.render.drawers.floorplan;

import architect.floorplan.FloorPlan;

/**
 *
 * @author AK47
 */
abstract class Drawer {

	private boolean enabled = true;

	public void toggle() {
		enabled = !enabled;
	}

	public void disable() {
		enabled = false;
	}

	public final void draw(FloorPlan floorPlan) {
		if (enabled && floorPlan != null) {		
			drawForFloorPlan(floorPlan);
		}
	}

	abstract void drawForFloorPlan(FloorPlan floorPlan);
}
