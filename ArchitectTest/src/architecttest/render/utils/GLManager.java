package architecttest.render.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import static java.lang.management.ManagementFactory.getRuntimeMXBean;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.regex.Pattern;
import static java.util.regex.Pattern.compile;
import static javax.imageio.ImageIO.read;
import static org.lwjgl.BufferUtils.createByteBuffer;
import static org.lwjgl.BufferUtils.createFloatBuffer;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import static org.lwjgl.opengl.Display.*;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.*;

public class GLManager {

	private GLManager() {
	}
	private final static Pattern debugPattern = compile("-Xdebubg|jdwp");

	private static boolean isDebugging() {
		for (String arg : getRuntimeMXBean().getInputArguments()) {
			if (debugPattern.matcher(arg).find()) {
				return true;
			}
		}
		return false;
	}

	public static void createWindow(String title) throws LWJGLException, IOException {
		ByteBuffer[] list = {createBuffer(read(new File("icon16.png"))),
							 createBuffer(read(new File("icon32.png")))};

		if (isDebugging()) { // debug -> change icon
			list[0] = createBuffer(read(new File("icon16D.png")));
			list[1] = createBuffer(read(new File("icon32D.png")));
		}

		setIcon(list);

		setVSyncEnabled(false);
		setFullscreen(false);
		setResizable(true);
		setDisplayMode(new DisplayMode(1024, 640));
		setTitle(title);
		create();
	}

	public static void init() {
		glShadeModel(GL_SMOOTH); // Enable Smooth Shading
		glEnable(GL_COLOR_MATERIAL);

		glClearColor(0.0f, 0.0f, 0.0f, 0.0f); // Black Background
		glCullFace(GL_BACK);
		glEnable(GL_CULL_FACE);
		glClearDepth(1.0f); // Depth Buffer Setup
		glEnable(GL_DEPTH_TEST); // Enables Depth Testing
		glDepthFunc(GL_LEQUAL); // The Orientation Of Depth Testing To Do

		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glEnable(GL_ALPHA_TEST);
		glEnable(GL_BLEND);

		glHint(GL_POLYGON_SMOOTH_HINT, GL_NICEST);

		glEnable(GL_TEXTURE_2D);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

		// Really Nice Perspective Calculations
		glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);

		glPointSize(10.0f);

		// glEnable(GL_LIGHTING);
		glEnable(GL_LIGHT0);

		float[] ambientLight = {0.0f, 0.0f, 0.0f, 1.0f};
		float[] diffuseLight = {1.0f, 1.0f, 1.0f, 1.0f};
		float[] specularLight = {0.5f, 0.5f, 0.5f, 1.0f};
		float[] position = {0, 0, 10, 0.0f};
		// float[] lightDir = { 0.0f, 0.0f, -1.0f, 1.0f };

		FloatBuffer al = createFloatBuffer(4);
		al.put(ambientLight);
		al.rewind();
		FloatBuffer dl = createFloatBuffer(4);
		dl.put(diffuseLight);
		dl.rewind();
		FloatBuffer sl = createFloatBuffer(4);
		sl.put(specularLight);
		sl.rewind();
		FloatBuffer pos = createFloatBuffer(4);
		pos.put(position);
		pos.rewind();
        // FloatBuffer ld = org.lwjgl.BufferUtils.createFloatBuffer(4);
		// ld.put(lightDir); ld.rewind();

		// Assign created components to GL_LIGHT0
		glLight(GL_LIGHT0, GL_AMBIENT, al);
		glLight(GL_LIGHT0, GL_DIFFUSE, dl);
		glLight(GL_LIGHT0, GL_SPECULAR, sl);
		glLight(GL_LIGHT0, GL_POSITION, pos);
		glLightf(GL_LIGHT0, GL_QUADRATIC_ATTENUATION, 0.02f);

		glLighti(GL_LIGHT0, GL_SPOT_CUTOFF, 20);
		glLightf(GL_LIGHT0, GL_SPOT_EXPONENT, 0.5f);
		// glLight(GL_LIGHT0, GL_SPOT_DIRECTION, ld);
	}

	public static void destroy() {
		Display.destroy();
	}

	public static void beginRendering(Camera camera) {
		glViewport(0, 0, getWidth(), getHeight());
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		// Select The Projection Matrix
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		camera.setGLProjectionMatrix(getWidth(), getHeight());

		// Select The Modelview Matrix
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
		camera.setGLModelMatrix();
	}

	public static void beginRenderingMenu() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		// Select The Projection Matrix
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(0, getWidth(), getHeight(), 0, 0.1, 1000);

		// Select The Modelview Matrix
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
		gluLookAt(0, 0, 1, 0, 0, 0, 0, 1, 0);
	}

	public static void endRendering() {
		update();
	}

	private static ByteBuffer createBuffer(BufferedImage image) {
		int[] pixels = new int[image.getWidth() * image.getHeight()];
		image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

		// 4 for RGBA, 3 for RGB
		ByteBuffer buffer = createByteBuffer(image.getWidth() * image.getHeight() * 4);

		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				int pixel = pixels[y * image.getWidth() + x];
				buffer.put((byte) ((pixel >> 16) & 0xFF)); // Red component
				buffer.put((byte) ((pixel >> 8) & 0xFF)); // Green component
				buffer.put((byte) (pixel & 0xFF)); // Blue component
				buffer.put((byte) ((pixel >> 24) & 0xFF)); // Alpha component.
				// Only for RGBA
			}
		}

		buffer.flip(); // FOR THE LOVE OF GOD DO NOT FORGET THIS
		return buffer;
	}
}
