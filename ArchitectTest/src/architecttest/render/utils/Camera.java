package architecttest.render.utils;

import static java.lang.Math.*;
import static org.lwjgl.util.glu.GLU.gluLookAt;
import static org.lwjgl.util.glu.GLU.gluPerspective;
import org.lwjgl.util.vector.Vector3f;

public class Camera {

	public Vector3f position, direction, up;
	public float nearPlane, farPlane, verticalFOV, aspectRatio;

	public Camera() {

	}

	public Camera(Vector3f position, Vector3f direction, Vector3f up,
				  float nearPlane, float farPlane, float verticalFOV) {
		this.position = new Vector3f(position);
		this.direction = new Vector3f(direction);
		this.up = new Vector3f(up);

		this.nearPlane = nearPlane;
		this.farPlane = farPlane;
		this.verticalFOV = verticalFOV;
	}

	public void setGLProjectionMatrix(int screenWidth, int screenHeight) {
		aspectRatio = (float) screenWidth / screenHeight;
		gluPerspective(verticalFOV, aspectRatio, nearPlane, farPlane);
	}

	public void setGLModelMatrix() {
		gluLookAt(position.x, position.y, position.z,
				  position.x + direction.x, position.y + direction.y, position.z + direction.z,
				  up.x, up.y, up.z);
	}

	public Vector3f castScreenRay(int x, int y, int screenWidth, int screenHeight) {
		aspectRatio = (float) screenWidth / screenHeight;
		float scale = (float) tan(toRadians(verticalFOV * 0.5)) * abs(position.z);
		float relativeX = scale * (x * 2 - screenWidth) / screenWidth * aspectRatio;
		float relativeY = scale * (y * 2 - screenHeight) / screenHeight;

		return new Vector3f(relativeX + position.x, relativeY + position.y, 0);
	}
}
