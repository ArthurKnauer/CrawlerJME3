package common.render.lpv;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.Vector2f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import lombok.Getter;

/**
 *
 * @author VTPlusAKnauer
 */
public class DeferredShader {

	private final Geometry sceneQuad;
	private final Material lpvDeferredShader;

	@Getter private final FrameBuffer frameBuffer;
	@Getter private final Texture2D diffuseMap;
	@Getter private final Texture2D normalMap;
	@Getter private final Texture2D depthMap;

	DeferredShader(AssetManager assetManager, int width, int height, LPVFrameBuffer lpvFrameBuffer, ShadowMapSunLight sunLight, ShadowMapFlashLight flashLight) {
		sceneQuad = new Geometry("sceneRect", new Quad(1, 1));

		diffuseMap = new Texture2D(width, height, Image.Format.RGBA8);
		diffuseMap.setMagFilter(Texture.MagFilter.Nearest);
		diffuseMap.setMinFilter(Texture.MinFilter.NearestNoMipMaps);

		normalMap = new Texture2D(width, height, Image.Format.RGBA8);
		normalMap.setMagFilter(Texture.MagFilter.Nearest);
		normalMap.setMinFilter(Texture.MinFilter.NearestNoMipMaps);

		depthMap = new Texture2D(width, height, Image.Format.Depth32);
		depthMap.setMagFilter(Texture.MagFilter.Nearest);
		depthMap.setMinFilter(Texture.MinFilter.NearestNoMipMaps);

		frameBuffer = new FrameBuffer(width, height, 1);
		frameBuffer.setMultiTarget(true);
		frameBuffer.addColorTexture(diffuseMap);
		frameBuffer.addColorTexture(normalMap);
		frameBuffer.setDepthTexture(depthMap);

		lpvDeferredShader = new Material(assetManager, "MatDefs/LPVDeferred/LPVDeferred.j3md");
		lpvDeferredShader.setTexture("DiffuseMap", diffuseMap);
		lpvDeferredShader.setTexture("NormalMap", normalMap);
		lpvDeferredShader.setTexture("DepthMap", depthMap);
		lpvDeferredShader.setTexture("ShadowMap", sunLight.getShadowMap());

		lpvDeferredShader.setTexture("ShadowMapFL", flashLight.getShadowMap());
		lpvDeferredShader.setTexture("FlashLight", flashLight.getTexture());

		lpvFrameBuffer.setLPVTextures(lpvDeferredShader, 0);
	}

	void setLPVShape(LPVShape lpvShape) {
		lpvDeferredShader.setVector3("LPVMinPos", lpvShape.getMinPos());
		lpvDeferredShader.setVector3("LPVScale", lpvShape.getScale());
		lpvDeferredShader.setVector3("LPVCellSize", lpvShape.getCellSize());
	}

	void setFluxScale(float fluxScale) {
		lpvDeferredShader.setFloat("LPVFluxScale", fluxScale);
	}

	void setSunLight(ShadowMapSunLight sunLight) {
		lpvDeferredShader.setVector3("LightDirNeg", sunLight.getDirection().negate());
		lpvDeferredShader.setMatrix4("LightViewProjectionMatrix", sunLight.getLightViewProjectionMatrix());
	}

	void setFlashLight(ShadowMapFlashLight flashLight) {
		lpvDeferredShader.setVector3("LightDirNegFL", flashLight.getDirection().negate());
		lpvDeferredShader.setVector3("LightPosFL", flashLight.getPosition());
		lpvDeferredShader.setMatrix4("LightViewProjectionMatrixFL", flashLight.getLightViewProjectionMatrix());
	}

	void setCamera(Camera viewCam) {
		lpvDeferredShader.setVector3("CameraPos", viewCam.getLocation());
		lpvDeferredShader.setVector2("ProjectionValues", new Vector2f(viewCam.getProjectionMatrix().m22, viewCam.getProjectionMatrix().m23));
	}

	void setFrustumRays(FrustumRays frustumRays) {
		lpvDeferredShader.setVector3("FrustumLowerLeftRay", frustumRays.getLowerLeftRay());
		lpvDeferredShader.setVector3("FrustumLowerRightRay", frustumRays.getLowerRightRay());
		lpvDeferredShader.setVector3("FrustumUpperRightRay", frustumRays.getUpperRightRay());
		lpvDeferredShader.setVector3("FrustumUpperLeftRay", frustumRays.getUpperLeftRay());
	}

	void render(RenderManager renderManager) {
		lpvDeferredShader.render(sceneQuad, renderManager);
	}

}
