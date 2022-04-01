package test;

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL32.*;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.opengl.Util;

public class RenderTo3DTextureTest {

	private int texture3Did;
	private int framebufferID;

	private final int viewportSize = 512;
	private final int textureDepth = 16;

	private ShaderProgramWithGeom to3DShader;
	private ShaderProgram from3DShader;

	public static void main(String[] argv) {
		RenderTo3DTextureTest fboExample = new RenderTo3DTextureTest();
		fboExample.start();
	}
	private int vertexCount;
	private int vaoId;
	private int vboId;

	private void start() {
		try {
			Display.setDisplayMode(new DisplayMode(viewportSize, viewportSize));
			Display.create();
		} catch (LWJGLException e) {
			System.exit(0);
		}

		initOpenGL();
		setupQuad();

		while (!Display.isCloseRequested()) {
			renderTo3DTexture();
			renderFrom3DTexture();

			Display.update();
		}

		Display.destroy();
	}

	private void initOpenGL() {
		System.out.println("OpenGL version: " + glGetString(GL_VERSION));
		System.out.println("GLSL version: " + glGetString(GL_SHADING_LANGUAGE_VERSION));

		glViewport(0, 0, viewportSize, viewportSize);                                // Reset The Current Viewport
		glMatrixMode(GL_PROJECTION);                               // Select The Projection Matrix
		glLoadIdentity();                                          // Reset The Projection Matrix
		glMatrixMode(GL_MODELVIEW);                                // Select The Modelview Matrix
		glLoadIdentity();                                          // Reset The Modelview Matrix

		// init render to 3d texture fbo
		framebufferID = glGenFramebuffers();                                         // create a new framebuffer
		Util.checkGLError();

		glBindFramebuffer(GL_FRAMEBUFFER, framebufferID);                        // switch to the new framebuffer
		Util.checkGLError();

		// initialize color texture
		texture3Did = glGenTextures();                                               // and a new texture used as a color buffer
		glBindTexture(GL_TEXTURE_3D, texture3Did);                                   // Bind the colorbuffer texture
		Util.checkGLError();

		glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		Util.checkGLError();

		glTexImage3D(GL_TEXTURE_3D, 0, GL_RGB8, viewportSize, viewportSize, textureDepth, 0, GL_RGB, GL_BYTE, (java.nio.ByteBuffer) null);  // Create the texture data
		Util.checkGLError();

		int colorbuffer = glGenRenderbuffers();
		glBindRenderbuffer(GL_RENDERBUFFER, colorbuffer);

		glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, texture3Did, 0);

		Util.checkGLError();

		glBindFramebuffer(GL_FRAMEBUFFER, 0);                                    // Switch back to normal framebuffer rendering

		Util.checkGLError();

		to3DShader = new ShaderProgramWithGeom();
		to3DShader.attachVertexShader("test/to3DTexture.vs");
		to3DShader.attachFragmentShader("test/to3DTexture.fs");
		to3DShader.attachGeometryShader("test/to3DTexture.gs");
		to3DShader.link();
		Util.checkGLError();

		from3DShader = new ShaderProgram();
		from3DShader.attachVertexShader("test/from3DTexture.vs");
		from3DShader.attachFragmentShader("test/from3DTexture.fs");
		from3DShader.link();
		Util.checkGLError();
	}

	private void renderTo3DTexture() {
		// FBO render pass
		glViewport(0, 0, viewportSize, viewportSize);                                    // set The Current Viewport to the fbo size
		glBindTexture(GL_TEXTURE_3D, 0);                                // unlink textures because if we dont it all is gonna fail
		glBindFramebuffer(GL_FRAMEBUFFER, framebufferID);        // switch to rendering on our FBO
		Util.checkGLError();

		to3DShader.bind();// set color to yellow
		renderQuad();

		ShaderProgram.unbind();
	}

	private void renderFrom3DTexture() {
		glViewport(0, 0, viewportSize, viewportSize);                                    // set The Current Viewport

		// Normal render pass, draw cube with texture
		glEnable(GL_TEXTURE_3D);                                        // enable texturing
		glBindFramebuffer(GL_FRAMEBUFFER, 0);                    // switch to rendering on the framebuffer

		from3DShader.bind();// set color to yellow
		from3DShader.setTextureUnit0("texture3d");
		float slice = (System.currentTimeMillis() % 2000) / 2000.0f;
		from3DShader.setFloat("slice", slice);

		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_3D, texture3Did);
		Util.checkGLError();

		drawScreenQuads(1);                                                      // draw the box
		ShaderProgram.unbind();

		glDisable(GL_TEXTURE_3D);
		glFlush();
	}

	private void drawScreenQuads(int depthSamples) {
		glBegin(GL_TRIANGLES);

		for (int slice = 0; slice < depthSamples; slice++) {
			glVertex3f(0, 0, slice);  // Bottom Left
			glVertex3f(1, 0, slice);  // Bottom Right
			glVertex3f(1, 1, slice);  // Top Right 

			glVertex3f(1, 1, slice);  // Top Right
			glVertex3f(0, 1, slice);  // Top Left 
			glVertex3f(0, 0, slice);  // Bottom Left
		}

		glEnd();
	}

	private void setupQuad() {
		// OpenGL expects vertices to be defined counter clockwise by default
		float[] vertices = {
			// Left bottom triangle
			0, 1, 0f,
			0, 0, 0f,
			1, 0, 0f,
			// Right top triangle
			1, 0, 0f,
			1, 1, 0f,
			0, 1, 0f
		};
		// Sending data to OpenGL requires the usage of (flipped) byte buffers
		FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer(vertices.length);
		verticesBuffer.put(vertices);
		verticesBuffer.flip();

		vertexCount = 6;

		// Create a new Vertex Array Object in memory and select it (bind)
		// A VAO can have up to 16 attributes (VBO's) assigned to it by default
		vaoId = glGenVertexArrays();
		glBindVertexArray(vaoId);

		// Create a new Vertex Buffer Object in memory and select it (bind)
		// A VBO is a collection of Vectors which in this case resemble the location of each vertex.
		vboId = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vboId);
		glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);
		// Put the VBO in the attributes list at index 0
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		// Deselect (bind to 0) the VBO
		glBindBuffer(GL_ARRAY_BUFFER, 0);

		// Deselect (bind to 0) the VAO
		glBindVertexArray(0);
		Util.checkGLError();
	}

	private void renderQuad() {
		// Bind to the VAO that has all the information about the quad vertices
		glBindVertexArray(vaoId);
		glEnableVertexAttribArray(0);

		// Draw the vertices
		// GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vertexCount);
		glDrawArraysInstanced(GL_TRIANGLES, 0, vertexCount, textureDepth);

		// Put everything back to default (deselect)
		glDisableVertexAttribArray(0);
		glBindVertexArray(0);
	}
}
