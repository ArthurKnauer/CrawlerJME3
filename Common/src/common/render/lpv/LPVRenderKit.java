/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common.render.lpv;

import com.jme3.material.Material;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.Renderer;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.texture.FrameBuffer;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

/**
 *
 * @author VTPlusAKnauer
 */
public class LPVRenderKit {

	private final RenderManager renderManager;
	private final Renderer renderer;
	private final ViewPort viewPort;
	private final LPVFrameBuffer lpvFrameBuffer;
	private final LPVFillGeometry volumeFillGeom;

	@Getter private final Camera viewCam;
	@Getter private final FrustumRays frustumRays;
	@Getter private final ShadowMapSunLight sunLight;

	@Builder
	private LPVRenderKit(@NonNull RenderManager renderManager,
						 @NonNull LPVFrameBuffer lpvFrameBuffer,
						 @NonNull LPVFillGeometry volumeFillGeom,
						 @NonNull ViewPort viewPort,
						 @NonNull FrustumRays frustumRays,
						 @NonNull ShadowMapSunLight sunLight) {
		this.renderManager = renderManager;
		this.renderer = renderManager.getRenderer();
		this.lpvFrameBuffer = lpvFrameBuffer;
		this.volumeFillGeom = volumeFillGeom;
		this.viewPort = viewPort;
		this.viewCam = viewPort.getCamera();
		this.frustumRays = frustumRays;
		this.sunLight = sunLight;
	}

	public void setCamera(Camera camera, boolean ortho) {
		renderManager.setCamera(camera, ortho);
	}

	public void setForcedTechnique(String rsm) {
		renderManager.setForcedTechnique("RSM");
	}

	public void setForcedMeshMode(Mesh.Mode mode) {
		renderer.setForcedMeshMode(mode);
	}

	public void setFrameBuffer(FrameBuffer frameBuffer) {
		renderer.setFrameBuffer(frameBuffer);
	}

	public void setFrameBufferFirstLPV() {
		renderer.setViewPort(0, 0, lpvFrameBuffer.getWidth(), lpvFrameBuffer.getHeight());
		renderer.setFrameBuffer(lpvFrameBuffer.getBuffer(0));
	}

	public void setFrameBufferSecondLPV() {
		renderer.setViewPort(0, 0, lpvFrameBuffer.getWidth(), lpvFrameBuffer.getHeight());
		renderer.setFrameBuffer(lpvFrameBuffer.getBuffer(1));
	}

	public void setFrameBufferGV() {
		renderer.setViewPort(0, 0, lpvFrameBuffer.getWidth(), lpvFrameBuffer.getHeight());
		renderer.setFrameBuffer(lpvFrameBuffer.getGvFrameBuffer());
	}

	public void clearBuffers(boolean color, boolean depth, boolean stencil) {
		renderer.clearBuffers(color, depth, stencil);
	}

	public void renderShadowQueue() {
		viewPort.getQueue().renderShadowQueue(RenderQueue.ShadowMode.Cast, renderManager, sunLight.getShadowCamera(), false);
	}

	public void renderVolumeFill(Material shader) {
		render(shader, volumeFillGeom);
	}

	public void render(Material shader, Geometry geometry) {
		shader.render(geometry, renderManager);
	}

	void setLPVFilterNearest() {
		lpvFrameBuffer.setFilterNearest();
	}

	void setLPVFilterBilinear() {
		lpvFrameBuffer.setFilterBilinear();
	}

	void setShaderTexturesFirstLPV(Material shader) {
		lpvFrameBuffer.setLPVTextures(shader, 0);
	}

	void setShaderTexturesSecondLPV(Material shader) {
		lpvFrameBuffer.setLPVTextures(shader, 1);
	}
}
