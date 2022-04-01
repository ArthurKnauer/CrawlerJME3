/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architecttest.render.drawers.floorplan;

import architect.floorplan.FloorPlan;
import static architect.logger.LogManager.decimalFormat;
import architect.math.Rectangle;
import architect.room.Room;
import architecttest.render.text.GLText;
import static architecttest.render.utils.Color.White;
import org.lwjgl.util.vector.Vector2f;

/**
 *
 * @author Arthur
 */
public class RoomInfoDrawer extends Drawer {

	@Override
	void drawForFloorPlan(FloorPlan floorPlan) {
		if (floorPlan.rooms != null) {
			floorPlan.rooms.forEach(this::printRoomInfo);
		}
	}

	private void printRoomInfo(Room room) {
		GLText.beginForWorld();
		float txtOffset = GLText.getScale() / 10.0f;
		Rectangle biggestSquareRR = room.biggestSquareSubRect();
		if (biggestSquareRR == null)
			biggestSquareRR = room.boundingRect();
		if (biggestSquareRR == null) {
			biggestSquareRR = Rectangle.ZERO;
		}

		Vector2f textPos = new Vector2f(biggestSquareRR.minX + 0.5f, biggestSquareRR.maxY - 0.5f);

		GLText.print(White, "" + room.type() + " " + room.name() + " (" + room.id + ")", textPos.x, textPos.y, 0.1f);
		GLText.print(White, "" + decimalFormat.format(room.areaToNeedRatio()), textPos.x, textPos.y - txtOffset, 0.1f);
		GLText.end();
	}
}
