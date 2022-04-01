/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common.render.lpv;

import com.jme3.math.Matrix4f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import lombok.Getter;

/**
 *
 * @author VTPlusAKnauer
 */
abstract class ShadowMapLight {

	private static final Matrix4f PROJECTION_BIAS_MATRIX = new Matrix4f(0.5f, 0.0f, 0.0f, 0.5f,
																		0.0f, 0.5f, 0.0f, 0.5f,
																		0.0f, 0.0f, 0.5f, 0.5f,
																		0.0f, 0.0f, 0.0f, 1.0f);

	@Getter private final FrameBuffer frameBuffer;
	@Getter private final Texture2D shadowMap;
	@Getter private Matrix4f lightViewProjectionMatrix;

	@Getter protected final Camera shadowCamera;
	@Getter protected final Vector3f direction = new Vector3f(0, 1, 0);

	ShadowMapLight(int width, int height) {
		shadowMap = new Texture2D(width, height, Image.Format.Depth);
		shadowMap.setShadowCompareMode(Texture.ShadowCompareMode.LessOrEqual);
		shadowMap.setMagFilter(Texture.MagFilter.Bilinear);
		shadowMap.setMinFilter(Texture.MinFilter.BilinearNoMipMaps);

		frameBuffer = new FrameBuffer(width, height, 1);
		frameBuffer.setDepthTexture(shadowMap);

		shadowCamera = new Camera(width, height);
	}

	void renderShadowMap(RenderManager renderManager, ViewPort viewPort) {
		renderManager.setCamera(shadowCamera, false);
		renderManager.getRenderer().setFrameBuffer(frameBuffer);
		renderManager.getRenderer().clearBuffers(false, true, false);
		viewPort.getQueue().renderShadowQueue(RenderQueue.ShadowMode.Cast, renderManager, shadowCamera, false);
	}

	public void updateLightViewProjectionMatrix() {
		lightViewProjectionMatrix = PROJECTION_BIAS_MATRIX.mult(shadowCamera.getViewProjectionMatrix());
	}
}
