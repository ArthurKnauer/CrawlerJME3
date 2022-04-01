package architecttest.render.text;

import architecttest.render.utils.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import static java.awt.geom.AffineTransform.getScaleInstance;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import static java.nio.ByteBuffer.allocateDirect;
import static java.nio.ByteOrder.nativeOrder;
import java.nio.IntBuffer;
import lombok.experimental.UtilityClass;
import static org.lwjgl.opengl.Display.getHeight;
import static org.lwjgl.opengl.Display.getWidth;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.gluOrtho2D;
import org.lwjgl.util.vector.Vector3f;

@UtilityClass
public class GLText {

	private static int textureID;
	private static int displayListBase; // Base Display List For The Font Set

	private static final java.awt.Color TEXT_COLOR = new java.awt.Color(0xFFFFFFFF, true);
	private static final java.awt.Color TEXT_OUTLINE_COLOR = new java.awt.Color(0xFF000000, true);
	private static final java.awt.Color TEXT_BG_COLOR = new java.awt.Color(0x33000000, true);

	private static final Vector3f scale = new Vector3f(1, 1, 1);

	public static void beginForWorld() {
		glPushAttrib(GL_ENABLE_BIT);
		glEnable(GL_BLEND);
		glEnable(GL_TEXTURE_2D);
		glDisable(GL_DEPTH_TEST);
		glDisable(GL_LIGHTING);
		glDisable(GL_CULL_FACE);
	}

	public static void beginForScreen() {
		beginForWorld();

		// Select The Projection Matrix
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();

		// Select The Modelview Matrix
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();

		// make sure that text is always same size, independant of the window
		float w = getWidth() / 350.f;
		float h = getHeight() / 350.f;
		gluOrtho2D(-1, -1 + w, 1 - h, 1);
	}

	public static void end() {
		glPopAttrib();
	}

	public static void setScale(float s) {
		scale.set(s, s, 1);
	}

	public static float getScale() {
		return scale.x;
	}

	public static void print(Color color, String msg, float x, float y, float z) {
		if (msg != null) {
			glPushAttrib(GL_TEXTURE_2D);
			glEnable(GL_TEXTURE_2D);

			glColor3f(color.red, color.green, color.blue);

			glPushMatrix();
			glTranslatef(x, y, z);
			glScalef(scale.x, scale.y, scale.z);
			glBindTexture(GL_TEXTURE_2D, textureID);
			for (int i = 0; i < msg.length(); i++) {
				char c = msg.charAt(i);
				if (c == '\t') {
					glCallList(displayListBase + ' ');
					glCallList(displayListBase + ' ');
					glCallList(displayListBase + ' ');
					glCallList(displayListBase + ' ');
				}
				glCallList(displayListBase + c);
			}
			glPopMatrix();

			glPopAttrib();
		}
	}

	public static void buildFont() { // Build Our Bitmap Font
		Font font; // Font object

		/*
		 * Note that I have set the font to Courier New. This font is not
		 * guraunteed to be on all systems. However it is very common so it is
		 * likely to be there. You can replace this name with any named font on
		 * your system or use the Java default names that are guraunteed to be
		 * there. Also note that this will work well with monospace fonts, but
		 * does not look as good with proportional fonts.
		 */
		//UIManager.getDefaults().getFont("TabbedPane.font");
		String fontName = "Arial"; // Name of the font to use
		BufferedImage fontImage; // image for creating the bitmap
		int bitmapSize = 512; // set the size for the bitmap texture
		boolean sizeFound = false;
		boolean directionSet = false;
		int delta = 0;
		int fontSize = 16;

		/*
		 * To find out how much space a Font takes, you need to use a the
		 * FontMetrics class. To get the FontMetrics, you need to get it from a
		 * Graphics context. A Graphics context is only available from a
		 * displayable surface, ie any class that subclasses Component or any
		 * Image. First the font is set on a Graphics object. Then get the
		 * FontMetrics and find out the width and height of the widest character
		 * (W). Then take the largest of the 2 values and find the maximum size
		 * font that will fit in the size allocated.
		 */
		while (!sizeFound) {
			font = new Font(fontName, Font.PLAIN, fontSize); // Font Name
			// use BufferedImage.TYPE_4BYTE_ABGR to allow alpha blending
			fontImage = new BufferedImage(bitmapSize, bitmapSize, BufferedImage.TYPE_4BYTE_ABGR);
			Graphics2D g = (Graphics2D) fontImage.getGraphics();
			g.setFont(font);
			FontMetrics fm = g.getFontMetrics();
			int width = fm.stringWidth("W");
			int height = fm.getHeight();
			int lineWidth = (width > height) ? width * 16 : height * 16;
			if (!directionSet) {
				if (lineWidth > bitmapSize) {
					delta = -2;
				}
				else {
					delta = 2;
				}
				directionSet = true;
			}
			if (delta > 0) {
				if (lineWidth < bitmapSize) {
					fontSize += delta;
				}
				else {
					sizeFound = true;
					fontSize -= delta;
				}
			}
			else if (delta < 0) {
				if (lineWidth > bitmapSize) {
					fontSize += delta;
				}
				else {
					sizeFound = true;
					fontSize -= delta;
				}
			}
		}

		/*
		 * Now that a font size has been determined, create the final image, set
		 * the font and draw the standard/extended ASCII character set for that
		 * font.
		 */
		font = new Font(fontName, Font.PLAIN, fontSize); // Font Name
		// use BufferedImage.TYPE_4BYTE_ABGR to allow alpha blending
		fontImage = new BufferedImage(bitmapSize, bitmapSize, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D graphics2D = (Graphics2D) fontImage.getGraphics();
		graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		graphics2D.setFont(font);
		graphics2D.setBackground(TEXT_BG_COLOR);
		graphics2D.clearRect(0, 0, bitmapSize, bitmapSize);

		FontMetrics fm = graphics2D.getFontMetrics();
		for (int i = 0; i < 256; i++) {
			int x = i % 16;
			int y = i / 16;

			graphics2D.setColor(TEXT_OUTLINE_COLOR);
			for (int k = 0; k <= 4; k++) {
				for (int l = -2; l <= 2; l++) {
					graphics2D.drawString(Character.toString((char) i), (x * 32) + k, (y * 32) + fm.getAscent() + l);
				}
			}

			graphics2D.setColor(TEXT_COLOR);
			graphics2D.drawString(Character.toString((char) i), (x * 32) + 2, (y * 32) + fm.getAscent());
		}

		/*
		 * The following code is taken directly for the LWJGL example code. It
		 * takes a Java Image and converts it into an OpenGL texture. This is a
		 * very powerful feature as you can use this to generate textures on the
		 * fly out of anything.
		 */
		// Flip Image
		AffineTransform tx = getScaleInstance(1, -1);
		tx.translate(0, -fontImage.getHeight(null));
		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		fontImage = op.filter(fontImage, null);

		// Put Image In Memory
		ByteBuffer scratch = allocateDirect(4 * fontImage.getWidth() * fontImage.getHeight());

		byte data[] = (byte[]) fontImage.getRaster().getDataElements(0, 0, fontImage.getWidth(),
																	 fontImage.getHeight(), null);
		scratch.clear();
		scratch.put(data);
		scratch.rewind();

		// Create A IntBuffer For Image Address In Memory
		IntBuffer buf = allocateDirect(4).order(nativeOrder()).asIntBuffer();
		glGenTextures(buf); // Create Texture In OpenGL
		textureID = buf.get(0);

		glBindTexture(GL_TEXTURE_2D, textureID);
		// Typical Texture Generation Using Data From The Image

		// Linear Filtering
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		// Linear Filtering
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		// Generate The Texture
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, fontImage.getWidth(), fontImage.getHeight(),
					 0, GL_RGBA, GL_UNSIGNED_BYTE, scratch);

		displayListBase = glGenLists(256); // Storage For 256 Characters

		/*
		 * Generate the display lists. One for each character in the
		 * standard/extended ASCII chart.
		 */
		float textureDelta = 1.0f / 16.0f;
		for (int i = 0; i < 256; i++) {
			float u = ((float) (i % 16)) / 16.0f;
			int d = i / 16;
			float v = 1.0f - (d / 16.0f);
			glNewList(displayListBase + i, GL_COMPILE);
			glBindTexture(GL_TEXTURE_2D, textureID);
			glBegin(GL_QUADS);
			glTexCoord2f(u, v);
			glVertex3f(-0.0450f, 0.0450f, 0.0f);
			glTexCoord2f((u + textureDelta), v);
			glVertex3f(0.0450f, 0.0450f, 0.0f);
			glTexCoord2f((u + textureDelta), v - textureDelta);
			glVertex3f(0.0450f, -0.0450f, 0.0f);
			glTexCoord2f(u, v - textureDelta);
			glVertex3f(-0.0450f, -0.0450f, 0.0f);
			glEnd();
			glTranslatef(0.00315f * fm.stringWidth("" + ((char) i)), 0.0f, 0.0f);
			glEndList();
		}
	}
}
