/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architecttest.render.drawers.complex;

import architect.room.Room;
import architect.room.RoomRect;
import static architecttest.render.Shapes.drawRect;
import static org.lwjgl.opengl.GL11.glLineWidth;

/**
 *
 * @author AK47
 */
class SelectedRoomChildrenDrawer extends Drawer {

	@Override
	public void drawForSelectedRoom(Room selectedRoom) {
		glLineWidth(3);
		for (RoomRect rr : selectedRoom.subRects()) {
			drawRect(rr, 0.0f);
		}
		glLineWidth(1);
	}

}
