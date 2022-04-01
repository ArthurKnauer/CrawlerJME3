/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architecttest.render.drawers.complex;

import architect.room.RoomRect;
import static architecttest.render.Shapes.drawArrow;
import architecttest.render.text.GLText;

/**
 *
 * @author AK47
 */
class SelectedRoomRectNeighborsDrawer extends Drawer {

	@Override
	protected void drawForSelectedRoomRect(RoomRect selectedRoomRect) {
		float arrowSize = GLText.getScale() / 50.0f;

		for (RoomRect neighbor : selectedRoomRect.neighbors()) {
			drawArrow(selectedRoomRect.centerPoint(), neighbor.centerPoint(), arrowSize, 0.3f);
		}
	}

}
