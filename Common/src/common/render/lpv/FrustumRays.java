/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common.render.lpv;

import com.jme3.math.Matrix4f;
import com.jme3.math.Vector3f;
import com.jme3.math.Vector4f;
import com.jme3.renderer.Camera;
import lombok.Getter;

/**
 *
 * @author VTPlusAKnauer
 */
public class FrustumRays {

	@Getter private final Vector3f lowerLeftRay;
	@Getter private final Vector3f lowerRightRay;
	@Getter private final Vector3f upperRightRay;
	@Getter private final Vector3f upperLeftRay;

	public FrustumRays() {
		lowerLeftRay = new Vector3f();
		lowerRightRay = new Vector3f();
		upperRightRay = new Vector3f();
		upperLeftRay = new Vector3f();
	}

	public void build(Camera camera) {
		lowerLeftRay.set(-1.0f, -1.0f, 0.0f);
		lowerRightRay.set(1.0f, -1.0f, 0.0f);
		upperRightRay.set(1.0f, 1.0f, 0.0f);
		upperLeftRay.set(-1.0f, 1.0f, 0.0f);

		Matrix4f invProjection = camera.getProjectionMatrix().invert();
		Matrix4f invCamRotation = camera.getViewMatrix().invert();
		invCamRotation.setTranslation(0, 0, 0);

		projectRay(invProjection, invCamRotation, lowerLeftRay);
		projectRay(invProjection, invCamRotation, lowerRightRay);
		projectRay(invProjection, invCamRotation, upperRightRay);
		projectRay(invProjection, invCamRotation, upperLeftRay);
	}

	private static void projectRay(Matrix4f invProjection, Matrix4f invCamRotation, Vector3f inRay) {
		Vector4f ray = invProjection.mult(new Vector4f(inRay, 1));
		ray.multLocal(1.0f / ray.z);
		ray.setW(1.0f);

		ray = invCamRotation.mult(ray);

		inRay.set(ray);
	}
}
