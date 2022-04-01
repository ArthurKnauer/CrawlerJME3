package architecttest.render.utils;

import static java.lang.Math.*;
import org.lwjgl.util.vector.Matrix4f;
import static org.lwjgl.util.vector.Matrix4f.transform;
import org.lwjgl.util.vector.Vector3f;
import static org.lwjgl.util.vector.Vector3f.cross;
import static org.lwjgl.util.vector.Vector3f.sub;
import org.lwjgl.util.vector.Vector4f;

public class OrbitCamera extends Camera {

	private final Vector3f center;
	private final Vector3f defaultPos;
	private final Vector3f rotRight;
	private final Vector3f rotUp;
	private float rotX, rotY;

	public OrbitCamera(Vector3f center, Vector3f defaultPos, Vector3f up, float nearPlane, float farPlane,
					   float verticalFOV, float aspectRatio) {
		this.center = center;
		this.defaultPos = defaultPos;

		direction = new Vector3f();

		sub(center, defaultPos, direction);
		direction.normalise();

		this.position = new Vector3f(defaultPos);
		this.up = new Vector3f(up);
		rotUp = new Vector3f(up);
		rotRight = new Vector3f();
		cross(direction, rotUp, rotRight);

		this.nearPlane = nearPlane;
		this.farPlane = farPlane;
		this.verticalFOV = verticalFOV;
		this.aspectRatio = aspectRatio;
	}

	public void orbit(float dX, float dY) {
		rotX -= dX;
		rotY += dY;

		float range = (float) (PI * 0.5f);
		if (rotX > range)
			rotX = range;
		else if (rotX < -range)
			rotX = -range;

		if (rotY > range)
			rotY = range;
		else if (rotY < -range)
			rotY = -range;

		// default direction and up
		sub(center, defaultPos, direction);
		up.set(rotUp.x, rotUp.y, rotUp.z);

		Matrix4f rot = new Matrix4f();
		rot.setIdentity();
		rot.rotate(rotY, rotRight);
		rot.rotate(rotX, rotUp);

		Vector4f dir4 = new Vector4f(direction.x, direction.y, direction.z, 1);
		Vector4f up4 = new Vector4f(up.x, up.y, up.z, 1);

		transform(rot, dir4, dir4);
		transform(rot, up4, up4);

		direction.set(dir4.x, dir4.y, dir4.z);
		up.set(up4.x, up4.y, up4.z);

		sub(center, direction, position);
		direction.normalise();
		up.normalise();

	}

	public void pan(float dX, float dY) {
		float zoomSpeed = verticalFOV * 0.05f;
		center.x += dX * zoomSpeed;
		center.y += dY * zoomSpeed;
		defaultPos.x += dX * zoomSpeed;
		defaultPos.y += dY * zoomSpeed;

		orbit(0, 0);
	}

	public void zoom(float f) {
		verticalFOV += f;
		if (verticalFOV < 10)
			verticalFOV = 10;
		else if (verticalFOV > 140)
			verticalFOV = 140;
	}
}
