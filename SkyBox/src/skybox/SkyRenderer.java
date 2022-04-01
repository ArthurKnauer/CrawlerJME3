package skybox;

import com.jme3.asset.AssetManager;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.texture.FrameBuffer;
import common.debug.DebugPictureList;
import common.render.SimpleProcessor;
import java.util.Calendar;
import lombok.Builder;
import lombok.Getter;
import skybox.atmosphere.AtmosphereRenderer;
import skybox.spatials.Spatials;
import skybox.starmath.EarthSunSystem;

/**
 *
 * @author VTPlusAKnauer
 */
public final class SkyRenderer extends SimpleProcessor {

	private static final float UPDATES_PER_VIEWANGLE_PER_PIXEL = 8;

	private final Configurator configurator;

	private final FrameBuffer hdrResultFrameBuffer;

	private final AtmosphereRenderer atmosphereRenderer;
	private final Spatials spatials;

	private final EarthSunSystem earthSunSystem;

	private final Calendar localTime;

	@Getter private Vector3f sunDirection;

	private float secondsSinceLastUpdate = 0;
	private float minSunAngleDifferenceForUpdate = 0;

	private boolean atmosphereNeedsUpdate = true;

	@Builder
	public SkyRenderer(AssetManager assetManager,
					   FrameBuffer hdrResultFrameBuffer,
					   String settingsScriptPath,
					   Calendar localTime,
					   int screenWidth, int screenHeight) {

		this.hdrResultFrameBuffer = hdrResultFrameBuffer;
		this.localTime = localTime;

		earthSunSystem = new EarthSunSystem();
		earthSunSystem.setObserverLatitude(FastMath.QUARTER_PI);

		atmosphereRenderer = new AtmosphereRenderer(assetManager);
		spatials = new Spatials(assetManager, atmosphereRenderer);

		sunDirection = new Vector3f();

		configurator = Configurator.builder()
				.scriptPath(settingsScriptPath)
				.calendar(localTime)
				.atmosphereShader(atmosphereRenderer.getShader()).build();
		configurator.compileAndRunScript();

		earthSunSystem.setLocalTime(localTime);
		updateSunDirection();
	}

	@Override
	public void initialize(RenderManager renderManager, ViewPort viewPort) {
		super.initialize(renderManager, viewPort);
		
		computeAnglePerPixel(viewPort.getCamera());
	}

	public void addDebugPictures(DebugPictureList debugPictures) {
		debugPictures.add("skyUp", atmosphereRenderer.getTextureUp());
		debugPictures.add("skySds", atmosphereRenderer.getTextureSides());
	}

	@Override
	public void reshape(ViewPort viewPort, int width, int height) {
		computeAnglePerPixel(viewPort.getCamera());
	}

	@Override
	public void preFrame(float tpf) {
		if (configurator.hasScriptChanged(tpf)) {
			configurator.compileAndRunScript();
			atmosphereNeedsUpdate = true;
		}

		secondsSinceLastUpdate += tpf;
		if (secondsSinceLastUpdate > 5) {
			float roundDown = FastMath.floor(secondsSinceLastUpdate);
			localTime.add(Calendar.SECOND, (int) roundDown);
			secondsSinceLastUpdate -= roundDown;

			updateSunDirection();
		}
	}

	@Override
	public void postQueue(RenderQueue rq) {
		if (atmosphereNeedsUpdate) {
			atmosphereRenderer.render(renderManager);
			atmosphereNeedsUpdate = false;
		}

		renderManager.getRenderer().setFrameBuffer(hdrResultFrameBuffer);
		renderManager.setCamera(viewPort.getCamera(), false);
		renderManager.getRenderer().setDepthRange(1, 1);

		spatials.render(renderManager);

		renderManager.getRenderer().setFrameBuffer(null);
		renderManager.getRenderer().setDepthRange(0, 1);
	}

	@Override
	public void postFrame(FrameBuffer out) {
	}

	@Override
	public void cleanup() {
	}

	private void updateSunDirection() {
		Vector3f newSunDirection = earthSunSystem.getSunDirection();
		if (newSunDirection.angleBetween(sunDirection) > minSunAngleDifferenceForUpdate) {
			sunDirection = newSunDirection;

			spatials.setSunDirection(sunDirection);
			atmosphereRenderer.getShader().setSunDirection(sunDirection);
			atmosphereNeedsUpdate = true;

			spatials.setStarsRotation(earthSunSystem.getStarsRotation());

			Vector3f moonDirection = earthSunSystem.getMoonDirection();
			spatials.setMoonDirection(moonDirection);
		}
	}

	private void computeAnglePerPixel(Camera camera) {
		float viewAnglePerPixel = camera.getFOVVertical() / camera.getHeight();
		minSunAngleDifferenceForUpdate = viewAnglePerPixel / UPDATES_PER_VIEWANGLE_PER_PIXEL;
	}
}
