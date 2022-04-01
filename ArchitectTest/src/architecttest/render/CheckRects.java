/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architecttest.render;

import architect.math.Rectangle;
import static architecttest.render.Shapes.drawRectDashed;
import architecttest.render.utils.RectangleParser;
import static java.lang.Math.sin;
import static java.lang.System.currentTimeMillis;
import java.util.LinkedList;
import javax.swing.JOptionPane;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glLineWidth;

/**
 *
 * @author AK47
 */
public class CheckRects {

	private final LinkedList<Rectangle> checkRects = new LinkedList<>();

	public void add() {
		String inputText = (String) JOptionPane.showInputDialog(null,
																"Enter rectangle, 'minX, minY, maxX, maxY',"
																+ " or [minX, maxX]x, [minY, maxY]y :", "Add Check Rectangle",
																JOptionPane.PLAIN_MESSAGE, null, null, "0");
		if (inputText != null)
			RectangleParser.parse(inputText).ifPresent(checkRects::add);
	}

	public void removeLast() {
		if (!checkRects.isEmpty())
			checkRects.removeLast();
	}

	public void draw() {
		float varying = 0.5f + 0.5f * (float) Math.pow(sin(currentTimeMillis() * 0.001), 2);

		glLineWidth(4);
		glColor4f(1, 0.5f, 0, varying);
		for (Rectangle rect : checkRects) {
			drawRectDashed(rect, 0, 0.001f);
		}

		glLineWidth(1);
		glColor4f(0, 0, 0, varying);
		for (Rectangle rect : checkRects) {
			drawRectDashed(rect, 0, 0.001f);
		}
	}
}
