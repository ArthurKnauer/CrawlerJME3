/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common.render.lpv;

import com.jme3.math.Vector3f;

/**
 *
 * @author VTPlusAKnauer
 */
final class ShadowMapSunLight extends ShadowMapLight {

	ShadowMapSunLight(int width, int height) {
		super(width, height);

		shadowCamera.setFrustum(20.1f, 80.0f, -25, 25, 10.0f, -10f);
		shadowCamera.setParallelProjection(true);
	}

	public void setDirection(Vector3f direction) {
		this.direction.set(direction);

		shadowCamera.getRotation().lookAt(direction, shadowCamera.getUp());
		shadowCamera.setLocation(direction.mult(-50));

		updateLightViewProjectionMatrix();
	}
}
