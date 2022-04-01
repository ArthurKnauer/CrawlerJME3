package common.render.lpv;

import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import lombok.Getter;

/**
 *
 * @author VTPlusAKnauer
 */
public class ReflectiveShadowMap {

	@Getter private final FrameBuffer frameBuffer;
	@Getter private final Texture2D colorMap;
	@Getter private final Texture2D normalMap;
	@Getter private final Texture2D positionMap;
	@Getter private final Camera camera;

	public ReflectiveShadowMap(int width, int height) {
		colorMap = new Texture2D(width, height, Image.Format.RGB8);
		colorMap.setMagFilter(Texture.MagFilter.Nearest);
		colorMap.setMinFilter(Texture.MinFilter.NearestNoMipMaps);

		normalMap = new Texture2D(width, height, Image.Format.RGB16F);
		normalMap.setMagFilter(Texture.MagFilter.Nearest);
		normalMap.setMinFilter(Texture.MinFilter.NearestNoMipMaps);

		positionMap = new Texture2D(width, height, Image.Format.RGB16F);
		positionMap.setMagFilter(Texture.MagFilter.Nearest);
		positionMap.setMinFilter(Texture.MinFilter.NearestNoMipMaps);

		Texture2D depthTexture = new Texture2D(width, height, Image.Format.Depth);
		depthTexture.setShadowCompareMode(Texture.ShadowCompareMode.LessOrEqual);
		depthTexture.setMagFilter(Texture.MagFilter.Nearest);
		depthTexture.setMinFilter(Texture.MinFilter.NearestNoMipMaps);

		frameBuffer = new FrameBuffer(width, height, 1);
		frameBuffer.setDepthTexture(depthTexture);
		frameBuffer.addColorTexture(colorMap);
		frameBuffer.addColorTexture(normalMap);
		frameBuffer.addColorTexture(positionMap);
		frameBuffer.setMultiTarget(true);

		camera = new Camera(width, height);
		camera.setParallelProjection(true);
	}

	public void setSunDirection(Vector3f direction) {
		camera.getRotation().lookAt(direction, camera.getUp());
		camera.setLocation(direction.mult(-50));
		camera.setFrustum(20.1f, 80.0f, -25, 25, 10.0f, -10f);
		camera.update();
		camera.updateViewProjection();
	}
	
	public int getWidth() {
		return frameBuffer.getWidth();
	}
	
	public int getHeight() {
		return frameBuffer.getHeight();
	}
}
