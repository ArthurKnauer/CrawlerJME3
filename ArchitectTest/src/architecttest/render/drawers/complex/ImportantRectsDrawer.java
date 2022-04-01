/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architecttest.render.drawers.complex;

import architect.math.Rectangle;
import architect.room.Room;
import static architecttest.render.Shapes.drawRectFilled;
import static org.lwjgl.opengl.GL11.glColor4f;

/**
 *
 * @author AK47
 */
class ImportantRectsDrawer extends Drawer {

	@Override
	public void drawForSelectedRoom(Room selectedRoom) {
		glColor4f(1, 1, 1, 0.5f);
		for (Rectangle rect : selectedRoom.importantRects()) {
			drawRectFilled(rect);
		}
	}

}
