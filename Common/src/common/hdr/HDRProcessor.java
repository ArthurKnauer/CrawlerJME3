/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common.hdr;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.CenteredQuad;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import common.debug.DebugPictureList;
import common.render.SimpleProcessor;
import static java.lang.Float.min;
import lombok.Setter;

/**
 *
 * @author VTPlusAKnauer
 */
public class HDRProcessor extends SimpleProcessor {

	private static final int LUMINANCE_DOWN_STEPS = 3;

	private final Texture2D luminanceTexture[] = new Texture2D[LUMINANCE_DOWN_STEPS];
	private final FrameBuffer luminanceFrameBuffer[] = new FrameBuffer[LUMINANCE_DOWN_STEPS];
	private final Material hdrToLuminanceShader;
	private final Material luminanceCopyShader;

	private final Geometry sceneRect;
	private final DownsamplingQuadArray downsamplingQuadArray;

	private final Material toneMapShader;
	
	private Texture2D hdrScene;
	@Setter private float eyeAdjustmentSpeed = 2;

	public HDRProcessor(AssetManager assetManager) {
		sceneRect = new Geometry("renderRect", new CenteredQuad(2, 2));
		toneMapShader = new Material(assetManager, "MatDefs/HDR/ToneMapper.j3md");
		toneMapShader.setFloat("ExposureScale", 1);

		downsamplingQuadArray = new DownsamplingQuadArray();

		luminanceTexture[0] = new Texture2D(64, 64, Image.Format.Luminance16F);
		luminanceTexture[1] = new Texture2D(8, 8, Image.Format.Luminance16F);
		luminanceTexture[2] = new Texture2D(1, 1, Image.Format.Luminance16F);

		for (int i = 0; i < LUMINANCE_DOWN_STEPS; i++) {
			luminanceFrameBuffer[i] = new FrameBuffer(luminanceTexture[i].getWidth(), luminanceTexture[i].getHeight(), 1);
			luminanceFrameBuffer[i].setColorTexture(luminanceTexture[i]);

			luminanceTexture[i].setMagFilter(Texture.MagFilter.Bilinear);
			luminanceTexture[i].setMinFilter(Texture.MinFilter.BilinearNoMipMaps);
		}

		hdrToLuminanceShader = new Material(assetManager, "MatDefs/HDR/HDRToLuminance.j3md");
		luminanceCopyShader = new Material(assetManager, "MatDefs/HDR/LuminanceCopy.j3md");
		luminanceCopyShader.setFloat("LuminanceScale", 1.0f / DownsamplingQuadArray.QAUDS);
		
		toneMapShader.setTexture("ExposureMap", luminanceTexture[LUMINANCE_DOWN_STEPS - 1]);
	}
	
	
	public void setExposureScale(float exposureScale) {
		toneMapShader.setFloat("ExposureScale", exposureScale);
	}

	public void setHDRSceneTexture(Texture2D hdrScene) {
		this.hdrScene = hdrScene;
		hdrScene.setMagFilter(Texture.MagFilter.Bilinear);
		hdrScene.setMinFilter(Texture.MinFilter.BilinearNoMipMaps);

		toneMapShader.setTexture("HDRScene", hdrScene);
		hdrToLuminanceShader.setTexture("HDRScene", hdrScene);
	}
	
	public void addDebugPictures(DebugPictureList debugPictures) {
		for (int i = 0; i < LUMINANCE_DOWN_STEPS; i++) {
			String name = "lum" + luminanceTexture[i].getWidth();
			debugPictures.add(name, luminanceTexture[i]);
		}
	}

	@Override
	public void reshape(ViewPort viewPort, int width, int height) {
	}

	@Override
	public void preFrame(float tpf) {
		hdrToLuminanceShader.setFloat("BlendStrength", min(1, tpf * eyeAdjustmentSpeed));
	}

	@Override
	public void postQueue(RenderQueue renderQueue) {
		renderer.setFrameBuffer(luminanceFrameBuffer[0]);
		hdrToLuminanceShader.render(sceneRect, renderManager);

		for (int i = 1; i < LUMINANCE_DOWN_STEPS; i++) {
			renderer.setFrameBuffer(luminanceFrameBuffer[i]);
			renderer.setViewPort(0, 0, luminanceTexture[i].getWidth(), luminanceTexture[i].getHeight());
			renderer.clearBuffers(true, false, false);
			luminanceCopyShader.setTexture("LuminanceSRC", luminanceTexture[i - 1]);
			luminanceCopyShader.render(downsamplingQuadArray, renderManager);
		}

		renderer.setFrameBuffer(null);
		renderer.setViewPort(0, 0, hdrScene.getWidth(), hdrScene.getHeight());
		toneMapShader.render(sceneRect, renderManager);
	}

	@Override
	public void postFrame(FrameBuffer out) {
	}

	@Override
	public void cleanup() {
	}
}
