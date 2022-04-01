/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architecttest.render.drawers.complex;

import architect.math.Rectangle;
import architect.math.Vector2D;
import architect.room.Room;
import static architecttest.render.Shapes.drawArrow;
import static architecttest.render.Shapes.drawRect;
import architecttest.render.text.GLText;
import static architecttest.render.utils.Color.LightBlue;
import static architecttest.render.utils.Color.Yellow;
import architect.walls.RoomRectWall;
import architect.walls.WallNode;
import architect.walls.WallType;
import static org.lwjgl.opengl.GL11.*;

/**
 *
 * @author AK47
 */
class SelectedRoomDetailsDrawer extends Drawer {

	@Override
	protected void drawForSelectedRoom(Room selectedRoom) {
		drawRoomWallNodeLoop(selectedRoom);
		drawRoomAnalyticalRects(selectedRoom);
		drawRoomWallPressure(selectedRoom);
		printRoomWallsInfo(selectedRoom);
	}

	private void drawRoomWallNodeLoop(Room room) {
		glLineWidth(4);
		glColor3f(0.25f, 0.25f, 1);
		glBegin(GL_LINE_LOOP);
		for (WallNode node : room.getWallNodeLoop()) {
			glVertex3f(node.position.x, node.position.y, 0.1f);
		}
		glEnd();

		glColor3f(0, 0, 0.5f);
		glBegin(GL_POINTS);
		for (WallNode node : room.getWallNodeLoop()) {
			glVertex3f(node.position.x, node.position.y, 0.1f);
		}
		glEnd();
	}

	private void drawRoomAnalyticalRects(Room room) {
		glLineWidth(3);
		drawBiggestFittingRect(room);
		drawBoundingRect(room);
		drawOptimalRect(room);
	}

	private void drawRoomWallPressure(Room room) {
		for (RoomRectWall wall : room.getWallLoop()) {
			if (wall.type == WallType.INTERNAL) {
				Vector2D start = new Vector2D((wall.min + wall.max) * 0.5f, wall.pos);
				Vector2D end = new Vector2D(start.x, wall.optimalPos);

				if (wall.isVertical()) { // switch x and y
					start = new Vector2D(start.y, start.x);
					end = new Vector2D(wall.optimalPos, start.y);
				}

				drawArrow(start, end, GLText.getScale() / 50.0f, 0.1f);
			}
		}
	}

	private void printRoomWallsInfo(Room room) {
		GLText.beginForWorld();
		for (RoomRectWall wall : room.getWallLoop()) {
			if (wall.isHorizontal())
				GLText.print(Yellow, "" + wall.id, (wall.max + wall.min) * 0.5f, wall.pos, 0.3f);
			else
				GLText.print(Yellow, "" + wall.id, wall.pos, (wall.max + wall.min) * 0.5f, 0.3f);
		}

		for (WallNode node : room.getWallNodeLoop()) {
			GLText.print(LightBlue, "" + node.id, node.position.x, node.position.y, 0.3f);
		}
		GLText.end();
	}

	private void drawBiggestFittingRect(Room room) {
		float scaleOffset = 0.1f;
		glColor3f(0, 0.75f, 0.25f);
		if (room.biggestRect() != null) {
			Rectangle biggestRect = room.biggestRect().scaledByOffset(-scaleOffset, -scaleOffset);
			drawRect(biggestRect, 0.15f);
		}
	}

	private static void drawBoundingRect(Room room) {
		float scaleOffset = 0.1f;
		glColor3f(1.0f, 0.25f, 0.25f);
		Rectangle bRect = room.boundingRect().scaledByOffset(scaleOffset, scaleOffset);
		drawRect(bRect, 0.15f);
	}

	private void drawOptimalRect(Room room) {
		if (room.getOptimalRect().isValid()) {
			glColor3f(0, 1.0f, 0);
			drawRect(room.getOptimalRect().rect, 0.15f);
		}
	}

}
