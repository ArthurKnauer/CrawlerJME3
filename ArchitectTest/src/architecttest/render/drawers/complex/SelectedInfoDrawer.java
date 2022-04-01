/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architecttest.render.drawers.complex;

import static architect.logger.LogManager.decimalFormat;
import architect.room.Room;
import architect.room.RoomRect;
import architecttest.render.text.Text;
import static architecttest.render.text.Text.skipLines;
import static architecttest.render.utils.Color.GreenBluish;
import static architecttest.render.utils.Color.Orange;

/**
 *
 * @author AK47
 */
class SelectedInfoDrawer extends Drawer {

	@Override
	protected void drawForSelectedRoomRect(RoomRect selectedRoomRect) {
		Text.print(Orange, "neighbors " + selectedRoomRect.toString());
		Text.print(Orange, "bestRatioWithFreeNeighbor: " + decimalFormat.format(selectedRoomRect.bestRatioWithFreeNeighbor()));
		Text.print(Orange, "owner: " + selectedRoomRect.assignedTo());
		Text.print(Orange, "area: " + selectedRoomRect.area());
		skipLines(1);
	}

	@Override
	protected void drawForSelectedRoom(Room selectedRoom) {
		Text.print(GreenBluish, "common perimeter:");
		for (Room neighbor : selectedRoom.neighbors()) {
			Text.print(GreenBluish, neighbor.name() + ": " + decimalFormat.format(selectedRoom.commonPerimeter(neighbor)));
		}
		skipLines(1);
	}

}
