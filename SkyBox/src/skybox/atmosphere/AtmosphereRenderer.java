package skybox.atmosphere;

import com.jme3.asset.AssetManager;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.Renderer;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import lombok.Getter;
import lombok.extern.java.Log;

/**
 *
 * @author VTPlusAKnauer
 */
@Log
public class AtmosphereRenderer {

	private static final int TILE_SIZE = 512;
	private static final int TILE_SIZE_HALF = TILE_SIZE / 2;

	private final FrameBuffer frameBufferUp, frameBufferSides;
	private final Geometry renderRect;
	@Getter private final Texture2D textureUp;
	@Getter private final Texture2D textureSides;
	@Getter private final AtmosphereShader shader;
	private final Camera cameraUp, cameraSides;

	public AtmosphereRenderer(AssetManager assetManager) {
		textureUp = new Texture2D(TILE_SIZE, TILE_SIZE, Image.Format.RGB16F);
		textureUp.setMagFilter(Texture.MagFilter.Bilinear);
		textureUp.setMinFilter(Texture.MinFilter.BilinearNoMipMaps);
		textureUp.setWrap(Texture.WrapMode.EdgeClamp);

		frameBufferUp = new FrameBuffer(TILE_SIZE, TILE_SIZE, 1);
		frameBufferUp.setMultiTarget(false);
		frameBufferUp.setColorTexture(textureUp);

		textureSides = new Texture2D(TILE_SIZE * 4, TILE_SIZE_HALF, Image.Format.RGB16F);
		textureSides.setMagFilter(Texture.MagFilter.Bilinear);
		textureSides.setMinFilter(Texture.MinFilter.BilinearNoMipMaps);
		textureSides.setWrap(Texture.WrapMode.EdgeClamp);

		frameBufferSides = new FrameBuffer(TILE_SIZE * 4, TILE_SIZE_HALF, 1);
		frameBufferSides.setMultiTarget(false);
		frameBufferSides.setColorTexture(textureSides);

		cameraUp = new Camera(TILE_SIZE, TILE_SIZE);
		cameraUp.setParallelProjection(true);

		cameraSides = new Camera(TILE_SIZE * 4, TILE_SIZE_HALF);
		cameraSides.setParallelProjection(true);

		shader = new AtmosphereShader(assetManager);

		renderRect = new Geometry("renderRect", new Quad(1, 1));
	}

	public void render(RenderManager renderManager) {
		Renderer renderer = renderManager.getRenderer();
		renderer.setFrameBuffer(frameBufferUp);
		renderManager.setCamera(cameraUp, false);

		// top quad
		renderer.setViewPort(0, 0, TILE_SIZE, TILE_SIZE);
		shader.setForward(new Vector3f(0, 1, 0));
		shader.setRight(new Vector3f(0, 0, 1));
		shader.setUp(new Vector3f(-1, 0, 0));
		shader.render(renderRect, renderManager);

		renderer.setFrameBuffer(frameBufferSides);
		renderManager.setCamera(cameraSides, false);

		// front quad
		renderer.setViewPort(0, -TILE_SIZE_HALF, TILE_SIZE, TILE_SIZE);
		shader.setForward(new Vector3f(1, 0, 0));
		shader.setRight(new Vector3f(0, 0, 1));
		shader.setUp(new Vector3f(0, 1, 0));
		shader.render(renderRect, renderManager);

		// right quad
		renderer.setViewPort(TILE_SIZE, -TILE_SIZE_HALF, TILE_SIZE, TILE_SIZE);
		shader.setForward(new Vector3f(0, 0, 1));
		shader.setRight(new Vector3f(-1, 0, 0));
		shader.setUp(new Vector3f(0, 1, 0));
		shader.render(renderRect, renderManager);

		// back quad
		renderer.setViewPort(TILE_SIZE * 2, -TILE_SIZE_HALF, TILE_SIZE, TILE_SIZE);
		shader.setForward(new Vector3f(-1, 0, 0));
		shader.setRight(new Vector3f(0, 0, -1));
		shader.setUp(new Vector3f(0, 1, 0));
		shader.render(renderRect, renderManager);

		// left quad
		renderer.setViewPort(TILE_SIZE * 3, -TILE_SIZE_HALF, TILE_SIZE, TILE_SIZE);
		shader.setForward(new Vector3f(0, 0, -1));
		shader.setRight(new Vector3f(1, 0, 0));
		shader.setUp(new Vector3f(0, 1, 0));
		shader.render(renderRect, renderManager);
	}
}
