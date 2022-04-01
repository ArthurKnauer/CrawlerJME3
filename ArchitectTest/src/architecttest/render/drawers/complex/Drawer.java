/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architecttest.render.drawers.complex;

import architect.Architect;
import architect.floorplan.FloorPlan;
import architect.math.Vector2D;
import architect.room.Room;
import architect.room.RoomRect;

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

	public boolean isEnabled() {
		return enabled;
	}

	public final void draw(DrawerInfo info) {
		if (enabled) {
			drawAlways();
			info.getComplexFloorPlan().ifPresent(this::drawForComplexFloorPlan);
			info.getCurrentArchitect().ifPresent(this::drawForCurrentArchitect);
			info.getMousePoint().ifPresent(this::drawForMousePoint);
			info.getSelectedRoom().ifPresent(this::drawForSelectedRoom);
			info.getSelectedRoomRect().ifPresent(this::drawForSelectedRoomRect);
		}
	}

	protected void drawAlways() {
	}
	
	protected void drawForComplexFloorPlan(FloorPlan complexFloorPlan) {
	}

	protected void drawForCurrentArchitect(Architect currentArchitect) {
	}
	
	protected void drawForMousePoint(Vector2D mousePoint) {
	}

	protected void drawForSelectedRoom(Room selectedRoom) {
	}

	protected void drawForSelectedRoomRect(RoomRect selectedRoomRect) {
	}
}
