/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architecttest.render.text;

import architecttest.render.utils.Color;
import java.util.List;
import lombok.experimental.UtilityClass;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector2f;

/**
 *
 * @author VTPlusAKnauer
 */
@UtilityClass
public class Text {

	private static final float lineHeight = 0.05f;
	private static final Vector2f carret = new Vector2f();

	public static void setCaret(float x, float y) {
		carret.set(x, y);
	}

	public static void setCaretBottom(float x) {
		//TODO: find out how to compute this constant (was experimentaly determined)
		float bottom = 1.05f - 0.00281604f * Display.getHeight();
		carret.set(x, bottom);
	}

	public static void skipLines(int lines) {
		carret.y -= lineHeight * lines;
	}

	public static void print(Color color, String text) {
		GLText.print(color, text, carret.x, carret.y, 0);
		carret.y -= lineHeight;
	}

	public static void printList(Color color, List<String> stringList) {
		for (String str : stringList) {
			print(color, str);
		}
	}
}
