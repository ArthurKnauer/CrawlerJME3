package test;

import lombok.Getter;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.opengl.GL20;
import static org.lwjgl.opengl.GL20.*;
import org.lwjgl.opengl.Util;

public class ShaderProgram {

	private final int programID;
	private final int vertexShaderID;
	private final int fragmentShaderID;
	@Getter private boolean linked;

	public ShaderProgram() {
		programID = glCreateProgram();
		vertexShaderID = glCreateShader(GL_VERTEX_SHADER);
		fragmentShaderID = glCreateShader(GL_FRAGMENT_SHADER);
	}

	public void attachVertexShader(String file) {
		attachShader(file, vertexShaderID);
		Util.checkGLError();
	}

	public void attachFragmentShader(String file) {
		attachShader(file, fragmentShaderID);
		Util.checkGLError();
	}

	protected void attachShader(String file, int shaderID) {
		String shaderCode = FileUtil.readFromFile(file);
		glShaderSource(shaderID, shaderCode);
		glCompileShader(shaderID);

		if (glGetShaderi(shaderID, GL_COMPILE_STATUS) == GL_FALSE)
			throw new RuntimeException("Error creating shader\n"
									   + glGetShaderInfoLog(shaderID, glGetShaderi(shaderID, GL_INFO_LOG_LENGTH)));

		glAttachShader(programID, shaderID);
		Util.checkGLError();
	}

	public void link() {
		glLinkProgram(programID);

		if (glGetProgrami(programID, GL_LINK_STATUS) == GL_FALSE)
			throw new RuntimeException("Unable to link shader program:");
		
		Util.checkGLError();
		linked = true;
	}

	public void bind() {
		glUseProgram(programID);
		Util.checkGLError();
	}

	public static void unbind() {
		glUseProgram(0);
		Util.checkGLError();
	}

	public void dispose() {
		unbind();

		glDetachShader(programID, vertexShaderID);
		glDetachShader(programID, fragmentShaderID);

		glDeleteShader(vertexShaderID);
		glDeleteShader(fragmentShaderID);

		glDeleteProgram(programID);
		Util.checkGLError();
	}

	public void setTextureUnit0(String textureName) {
		if (isLinked()) {		
			int location = GL20.glGetUniformLocation(programID, textureName);
			Util.checkGLError();
			//First of all, we retrieve the location of the sampler in memory.
			GL20.glUniform1i(location, 0);
			//Then we pass the 0 value to the sampler meaning it is to use texture unit 0.
			Util.checkGLError();
		}
	}
	
	public void setFloat(String name, float value) {
		if (isLinked()) {		
			int location = GL20.glGetUniformLocation(programID, name);
			glUniform1f(location, value);
			Util.checkGLError();
		}
	}

}
