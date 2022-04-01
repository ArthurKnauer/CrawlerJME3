package common.render.lpv;

import com.jme3.asset.AssetManager;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.texture.Texture3D;
import common.debug.DebugPictureList;
import common.lua.LuaCompiler;
import common.render.SimpleProcessor;
//import crawler.render.decals.Decal;
//import crawler.render.decals.DecalRenderer;
import common.render.lpv.debug.LPVProbe;
import java.util.LinkedList;
import lombok.Getter;
import lombok.Setter;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

public final class LPVProcessor extends SimpleProcessor {

	private static final int SHADOW_MAP_WIDTH = 4096;
	private static final int SHADOW_MAP_HEIGHT = 2048;
	private static final int RSM_MAP_WIDTH = 256;
	private static final int RSM_MAP_HEIGHT = 128;
	private static final int LPV_SIZE_X = 64;
	private static final int LPV_SIZE_Y = 8;
	private static final int LPV_SIZE_Z = 64;

	private final FrustumRays frustumRays;

	private LPVShape lpvShape;
	private LPVFrameBuffer lpvFrameBuffer;
	private LPVRenderKit taskInfo;

	private final LPVFillGeometry volumeFillGeom;

	private Node flashLightNode;
	private final ShadowMapSunLight sunLight;
	private final ShadowMapFlashLight flashLight;

	private LPVTaskList taskList;

	private LPVClearer lpvClearer;
	private ReflectiveShadowMap rsm;
	@Getter private RSMInjector rsmInjector;
	private RSMRenderer rsmRenderer;
	@Getter private GeometryInjector geometryInjector;
	@Getter private LightPointInjector lightPointInjector;
	@Getter private LightPropagator lightPropagator;

	private final DeferredShader sceneShader;
	//private final DecalRenderer decalRenderer;

	// will be setMinPosAndScale by lpv.lua script
	@Setter private float fluxScale;

	//public LinkedList<Decal> decalList = new LinkedList<>();
	private LPVProbe lpvProbe;

	@Getter private final Texture2D hdrResultTexture;
	@Getter private final FrameBuffer hdrResultFrameBuffer;

	private final Texture2D depthMap;
	private boolean enabled = true;

	public LPVProcessor(AssetManager assetManager, int screenWidth, int screenHeight, boolean enabled) {
		this.enabled = enabled;

		frustumRays = new FrustumRays();
		lpvShape = new LPVShape(LPV_SIZE_X, LPV_SIZE_Y, LPV_SIZE_Z);

		lpvFrameBuffer = new LPVFrameBuffer(lpvShape);

		// create triangles for volumeFillGeom, filling the whole lpv (used for clearing and propagation)
		volumeFillGeom = new LPVFillGeometry(LPV_SIZE_Z);

		if (enabled) {

			lpvProbe = new LPVProbe(assetManager);

			lpvClearer = new LPVClearer(assetManager);
			rsm = new ReflectiveShadowMap(RSM_MAP_WIDTH, RSM_MAP_HEIGHT);
			rsmInjector = new RSMInjector(assetManager, rsm);

			geometryInjector = new GeometryInjector(assetManager);
			rsmRenderer = new RSMRenderer(rsm);

			lightPointInjector = new LightPointInjector(assetManager);
			lightPropagator = new LightPropagator(assetManager, lpvFrameBuffer);

			taskList = LPVTaskList.builder()
					.rsmRenderer(rsmRenderer)
					.lpvClearer(lpvClearer)
					.geometryInjector(geometryInjector)
					.rsmInjector(rsmInjector)
					.lightPointInjector(lightPointInjector)
					.lightPropagator(lightPropagator).build();
		}

		sunLight = new ShadowMapSunLight(SHADOW_MAP_WIDTH, SHADOW_MAP_HEIGHT);
		flashLight = new ShadowMapFlashLight(assetManager, SHADOW_MAP_WIDTH, SHADOW_MAP_HEIGHT);

		sceneShader = new DeferredShader(assetManager, screenWidth, screenHeight, lpvFrameBuffer, sunLight, flashLight);

		//decalRenderer = new DecalRenderer(screenWidth, screenHeight, sceneShader);
		reset();

		hdrResultTexture = new Texture2D(screenWidth, screenHeight, Image.Format.RGB16F);
		hdrResultFrameBuffer = new FrameBuffer(screenWidth, screenHeight, 1);
		hdrResultFrameBuffer.setColorTexture(hdrResultTexture);

		depthMap = new Texture2D(screenWidth, screenHeight, Image.Format.Depth32F);
		depthMap.setMagFilter(Texture.MagFilter.Nearest);
		depthMap.setMinFilter(Texture.MinFilter.NearestNoMipMaps);

		hdrResultFrameBuffer.setDepthTexture(depthMap);
	}

	public void addDebugPictures(DebugPictureList debugPictures) {
		debugPictures.add("color", sceneShader.getDiffuseMap());
		debugPictures.add("normal", sceneShader.getNormalMap());
		debugPictures.add("depth", sceneShader.getDepthMap());

		if (enabled) {
			debugPictures.add("rsmCol", rsm.getColorMap());
			debugPictures.add("rsmNrm", rsm.getNormalMap());
			debugPictures.add("rsmPos", rsm.getPositionMap());
		}

		debugPictures.add("sunSM", sunLight.getShadowMap());
		debugPictures.add("result", hdrResultTexture);
		debugPictures.add("resultD", depthMap);

		debugPictures.add("GV", lpvFrameBuffer.getGvMap());
		debugPictures.add("LPV_R", lpvFrameBuffer.getLPVMap(0, 0));
		debugPictures.add("LPV_G", lpvFrameBuffer.getLPVMap(0, 1));
		debugPictures.add("LPV_B", lpvFrameBuffer.getLPVMap(0, 2));
	}

	public void setLPVShape(Vector3f minPos, Vector3f scale) {
		lpvShape.setMinPosAndScale(minPos, scale);

		sceneShader.setLPVShape(lpvShape);

		if (enabled) {
			rsmInjector.setLPVShape(lpvShape);
			geometryInjector.setLPVShape(lpvShape);
			lightPointInjector.setLPVShape(lpvShape);
			lightPropagator.setLPVShape(lpvShape);
			lpvProbe.setLPVSetup(lpvShape);
		}

	}

	@Override
	public void initialize(RenderManager renderManager, ViewPort viewPort) {
		super.initialize(renderManager, viewPort);

		if (enabled) {
			taskInfo = LPVRenderKit.builder()
					.renderManager(renderManager)
					.viewPort(viewPort)
					.lpvFrameBuffer(lpvFrameBuffer)
					.volumeFillGeom(volumeFillGeom)
					.frustumRays(frustumRays)
					.sunLight(sunLight).build();
		}
	}

	public void addLightPoints(Mesh points) {
		if (enabled) {
			lightPointInjector.addPoints(points);
		}
	}

	public void addOccluder(Geometry geom) {
		if (enabled) {
			geometryInjector.addOccluder(geom);
		}
	}

	public void setFlashLight(Node node) {
		flashLightNode = node;

		if (enabled) {
			node.attachChild(lpvProbe);
			lpvProbe.setFluxScale(fluxScale);
			lpvProbe.setLPV(lpvFrameBuffer);
		}
	}

	/**
	 * Sets the light direction to use to compute shadows
	 *
	 * @param sunDirection
	 */
	public void setSunDirection(Vector3f sunDirection) {
		Vector3f lightDirection = sunDirection.negate();
		sunLight.setDirection(lightDirection);
		if (enabled) {
			rsm.setSunDirection(lightDirection);
		}
	}

	// the render queue is assembled but not yet rendered, this is where most of the lpv magic happens
	@Override
	public void postQueue(RenderQueue renderQueue) {
		frustumRays.build(viewPort.getCamera());

		renderShadowMaps();

		if (enabled)
			taskList.doNext(taskInfo);

		renderScenePreDeferred(viewPort);

		//decalRenderer.renderDecals(taskInfo, decalList);
		shadeSceneDeferred(viewPort.getCamera(), frustumRays);

		renderer.setFrameBuffer(null);
		renderManager.setForcedMaterial(null);
		renderManager.setForcedTechnique(null);
		renderManager.setCamera(viewPort.getCamera(), false);
	}

	private void callSettingsScript() {
		if (enabled)
			LuaCompiler.compileFunction("lua/lpv.lua", "settings")
					.ifPresent(settings -> settings.call(CoerceJavaToLua.coerce(this)));
	}

	private void renderShadowMaps() {
		// render big shadow map (depth buffer)
		renderManager.setForcedMaterial(null);
		renderManager.setForcedTechnique("ShadowMap");

		sunLight.renderShadowMap(renderManager, viewPort);

		flashLight.positionAtNode(flashLightNode);
		flashLight.renderShadowMap(renderManager, viewPort);
	}

	private void renderScenePreDeferred(ViewPort viewPort) {
		// setMinPosAndScale settings for scene rendering render to 3 textures (color, depth and normal)
		renderManager.setForcedMaterial(null);
		renderManager.setForcedTechnique("PreDeferred");

		renderManager.setCamera(viewPort.getCamera(), false);
		renderer.setFrameBuffer(sceneShader.getFrameBuffer());
		renderer.clearBuffers(true, true, false);

		renderManager.setHandleTranslucentBucket(true);

		viewPort.getQueue().renderQueue(RenderQueue.Bucket.Opaque, renderManager, viewPort.getCamera(), true);
	}

	private void shadeSceneDeferred(Camera viewCam, FrustumRays frustumRays) {
		// render deferred shaded scene rect, renderManager will take care of the rest (sky, transparents, gui)
		renderer.setFrameBuffer(hdrResultFrameBuffer);
		renderManager.getRenderer().clearBuffers(true, true, true);

		renderManager.setCamera(viewCam, false);
		renderManager.setForcedMaterial(null);
		renderManager.setForcedTechnique(null);
		renderer.setDepthRange(0, 1);

		sceneShader.setFluxScale(fluxScale);
		sceneShader.setFrustumRays(frustumRays);
		sceneShader.setSunLight(sunLight);
		sceneShader.setFlashLight(flashLight);
		sceneShader.setCamera(viewCam);

		sceneShader.render(renderManager);
	}

	@Override
	public void postFrame(FrameBuffer out) {
	}

	public void reset() {
		if (enabled) {
			callSettingsScript(); // setMinPosAndScale settings from lua
			lightPropagator.reset();
			taskList.reset();
		}
	}

	@Override
	public void preFrame(float tpf) {
	}

	@Override
	public void reshape(ViewPort vp, int w, int h) {
		//throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void cleanup() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void toggleShowLPVProbe() {
		lpvProbe.toggle();
	}

}
