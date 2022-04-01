/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER;
import org.lwjgl.opengl.Util;

/**
 *
 * @author AK
 */
public class ShaderProgramWithGeom extends ShaderProgram {

	private final int geomShaderID;

	public ShaderProgramWithGeom() {
		geomShaderID = glCreateShader(GL_GEOMETRY_SHADER);
	}

	public void attachGeometryShader(String file) {
		attachShader(file, geomShaderID);
		Util.checkGLError();
	}
	
}
