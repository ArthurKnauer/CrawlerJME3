/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architecttest.render;

import architect.floorplan.FloorPlanPoly;
import architect.math.Rectangle;
import architect.math.Vector2D;
import architect.rectpoly.RectilinearPoly;
import architect.walls.WallSide;
import architect.walls.WallType;
import static java.lang.Math.*;
import static java.lang.System.currentTimeMillis;
import java.util.List;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.util.vector.Vector2f;
import static org.lwjgl.util.vector.Vector2f.angle;
import org.lwjgl.util.vector.Vector3f;
import static org.lwjgl.util.vector.Vector3f.add;
import static org.lwjgl.util.vector.Vector3f.sub;

/**
 *
 * @author VTPlusAKnauer
 */
public class Shapes {

	public static void drawDashedLine(Vector3f start, Vector3f end) {
		drawDashedLineAnimated(start, end, 0);
	}

	public static void drawDashedLineAnimated(Vector3f start, Vector3f end, double animSpeed) {
		float animOffset = (float) Math.sin(System.currentTimeMillis() * animSpeed);

		Vector3f dir = sub(end, start, null);
		float length = dir.length();
		int dashes = (int) (length / 0.1f);
		dir = (Vector3f) dir.scale(0.1f / length);

		Vector3f offset = new Vector3f(dir);
		offset.scale(animOffset);

		start = add(start, offset, null);

		glBegin(GL_LINES);
		for (int d = 0; d < dashes; d++) {
			if (d % 2 == 0)
				glVertex3f(start.x, start.y, start.z);
			start = add(start, dir, null);
			if (d % 2 == 0)
				glVertex3f(start.x, start.y, start.z);
		}
		glEnd();
	}

	public static void drawPoly(RectilinearPoly poly) {
		List<Vector2D> nodeLoop = poly.nodeLoop();

		glBegin(GL_LINE_LOOP);
		//Vector2D previousNode = nodeLoop.get(nodeLoop.size() - 1);
		for (Vector2D node : nodeLoop) {
			glVertex3f(node.x, node.y, 0);
		}
		glEnd();
	}

	public static void drawArrow(Vector2D start, Vector2D end, float thickness, float z) {
		if (start == null || end == null)
			return;

		float length = start.distanceTo(end);
		glLineWidth(1);
		glPushMatrix();
		glTranslatef(start.x, start.y, 0);
		glRotatef(angle(new Vector2f(1, 0), new Vector2f(end.x - start.x, end.y - start.y)) * 180.0f / (float) PI,
				  0, 0, end.y < start.y ? -1 : 1);

		glColor4f(1, 1, 1, 0.5f);
		glBegin(GL_QUADS);
		glVertex3f(0, thickness, z);
		glVertex3f(0, -thickness, z);
		glVertex3f(length - thickness * 3, -thickness, z);
		glVertex3f(length - thickness * 3, thickness, z);
		glEnd();

		glBegin(GL_TRIANGLES);
		glVertex3f(length - thickness * 3, -thickness * 3, z);
		glVertex3f(length, 0, z);
		glVertex3f(length - thickness * 3, thickness * 3, z);
		glEnd();

		glColor3f(0, 0, 0);
		glBegin(GL_LINE_LOOP);
		glVertex3f(0, thickness, z);
		glVertex3f(length - thickness * 3, thickness, z);
		glVertex3f(length - thickness * 3, thickness * 3, z);
		glVertex3f(length, 0, z);
		glVertex3f(length - thickness * 3, -thickness * 3, z);
		glVertex3f(length - thickness * 3, -thickness, z);
		glVertex3f(0, -thickness, z);
		glVertex3f(0, thickness, z);
		glEnd();

		glPopMatrix();
	}

	public static void drawRect(Rectangle rect, float z) {
		glBegin(GL_LINE_LOOP);
		glVertex3f(rect.minX, rect.minY, z);
		glVertex3f(rect.maxX, rect.minY, z);
		glVertex3f(rect.maxX, rect.maxY, z);
		glVertex3f(rect.minX, rect.maxY, z);
		glEnd();
	}

	public static void drawRectDashed(Rectangle rect, float z, double animSpeed) {
		Vector3f cornerA = new Vector3f(rect.minX, rect.minY, z);
		Vector3f cornerB = new Vector3f(rect.maxX, rect.minY, z);
		Vector3f cornerC = new Vector3f(rect.maxX, rect.maxY, z);
		Vector3f cornerD = new Vector3f(rect.minX, rect.maxY, z);

		drawDashedLineAnimated(cornerA, cornerB, animSpeed);
		drawDashedLineAnimated(cornerB, cornerC, animSpeed);
		drawDashedLineAnimated(cornerC, cornerD, animSpeed);
		drawDashedLineAnimated(cornerD, cornerA, animSpeed);
	}

	public static void drawRectFilled(Rectangle rect) {
		glBegin(GL_QUADS);
		glVertex3f(rect.minX, rect.minY, 0);
		glVertex3f(rect.maxX, rect.minY, 0);
		glVertex3f(rect.maxX, rect.maxY, 0);
		glVertex3f(rect.minX, rect.maxY, 0);
		glEnd();
	}

	public static void drawRectStriped(Rectangle rect) {
		glBegin(GL_QUADS);
		float offset = (currentTimeMillis() % 1000) * 0.001f * 0.8f;
		for (float x = offset - 0.4f; x < rect.width; x += 0.8f) {
			glVertex3f(max(rect.minX, rect.minX + x), rect.minY, 0);
			glVertex3f(min(rect.minX + x + 0.4f, rect.maxX), rect.minY, 0);
			glVertex3f(min(rect.minX + x + 0.4f, rect.maxX), rect.maxY, 0);
			glVertex3f(max(rect.minX, rect.minX + x), rect.maxY, 0);
		}
		glEnd();
	}

	public static void drawShadedWalls(FloorPlanPoly fpPoly, float shadeInterval) {
		glBegin(GL_LINES);
		for (WallSide wall : fpPoly.edgeLoop()) {
			if (wall.type == WallType.OUTER_WINDOWLESS) {
				for (float shadeLinePos = wall.min; shadeLinePos < wall.max; shadeLinePos += shadeInterval) {
					if (wall.isHorizontal()) {
						glVertex3f(shadeLinePos, wall.pos, 0);
						glVertex3f(shadeLinePos + shadeInterval, wall.pos - wall.normal.y * shadeInterval, 0);
					}
					else { // wall is vertical
						glVertex3f(wall.pos, shadeLinePos, 0);
						glVertex3f(wall.pos - wall.normal.x * shadeInterval, shadeLinePos + shadeInterval, 0);
					}
				}
			}
		}
		glEnd();
	}
}
