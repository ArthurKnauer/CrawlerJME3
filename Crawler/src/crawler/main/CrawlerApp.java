package crawler.main;

import com.jme3.bullet.BulletAppState;
import com.jme3.input.controls.InputListener;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.CameraNode;
import common.debug.DebugPicturesProcessor;
import common.hdr.HDRProcessor;
import common.material.MaterialLoader;
import common.material.RandomMaterialSelector;
import common.render.lpv.LPVProcessor;
import crawler.main.states.GameGUI;
import crawler.main.states.GameInput;
import crawler.main.states.InfoTracing;
import crawler.main.states.MainScreen;
import static crawler.properties.CrawlerProperties.PROPERTIES;
import crawler.render.city.CityRenderer;
import crawler.weapons.WeaponTypeSet;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Random;
import java.util.logging.Level;
import lombok.extern.java.Log;
import skybox.SkyRenderer;

@Log
public class CrawlerApp extends CrawlerAppBase {

	private final static float MIN_OPENGL_SHADING_VERSION = 3.3f;
	
	private static final boolean USE_LPV = PROPERTIES.getBoolean("CrawlerApp.useLPV");

	private static int currentSeed = 2;

	private BulletAppState bulletAppState;
	private CamFlyControl camFlyControl;

	private boolean freeCamera;
	private boolean fpsLimited = true;

	private GameState gameState;
	private MainScreen mainScreenState;
	private GameInput gameInputState;
	private GameGUI gameGUIState;
	//private ChopperState chopperState;
	private InfoTracing infoTracing;

	private final float slowDownSpeed = 5f;
	private final float speedUpSpeed = 5f;

	private LPVProcessor lpvProcessor;
	private SkyRenderer skyRenderer;
	private CityRenderer cityRenderer;
	private HDRProcessor hdrProcessor;
	private DebugPicturesProcessor debugPictureProcessor;

	private final Calendar localTime = new GregorianCalendar(2016, Calendar.AUGUST, 8, 0, 0, 0);

	@Override
	public void simpleInitApp() {
		checkOpenGLShadingVersion();
		
		MaterialLoader.init(assetManager);

		//audioRenderer.setEnvironment(new Environment(0.50f, 1.0f, 1.0f, 1.00f, 0.28f, 1.0f, 0.870f, 0.0020f, 0.810f, 0.0080f));
		// enable bullet physics
		bulletAppState = new BulletAppState();
		bulletAppState.setThreadingType(BulletAppState.ThreadingType.PARALLEL);
		stateManager.attach(bulletAppState);

		Random random =	new Random();
		Globals.setRandom(random);
		Globals.setRootNode(rootNode);
		Globals.setBulletAppState(bulletAppState);
		Globals.setAssetManager(assetManager);
		Globals.setPhysicsSpace(bulletAppState.getPhysicsSpace());

		lpvProcessor = new LPVProcessor(assetManager, settings.getWidth(), settings.getHeight(), USE_LPV);

		skyRenderer = SkyRenderer.builder()
				.assetManager(assetManager)
				.localTime(localTime)
				.hdrResultFrameBuffer(lpvProcessor.getHdrResultFrameBuffer())
				.screenWidth(viewPort.getCamera().getWidth())
				.screenHeight(viewPort.getCamera().getHeight())
				.settingsScriptPath("assets/Lua/sky.lua")
				.build();

		cityRenderer = new CityRenderer(lpvProcessor.getHdrResultFrameBuffer());

		hdrProcessor = new HDRProcessor(assetManager);
		hdrProcessor.setHDRSceneTexture(lpvProcessor.getHdrResultTexture());

		viewPort.addProcessor(lpvProcessor);
		viewPort.addProcessor(skyRenderer);
		viewPort.addProcessor(cityRenderer);
		viewPort.addProcessor(hdrProcessor);

		debugPictureProcessor = new DebugPicturesProcessor(assetManager, guiNode, guiFont);
		viewPort.addProcessor(debugPictureProcessor);

		lpvProcessor.addDebugPictures(debugPictureProcessor.getDebugPictures());
		skyRenderer.addDebugPictures(debugPictureProcessor.getDebugPictures());
		hdrProcessor.addDebugPictures(debugPictureProcessor.getDebugPictures());

		lpvProcessor.setSunDirection(skyRenderer.getSunDirection());

		Globals.setLpvProcessor(lpvProcessor);
		Globals.setWeaponTypeSet(WeaponTypeSet.createFromScript());
		Globals.setMaterialSelector(new RandomMaterialSelector(assetManager, random));

		setupCamera();

		gameState = new GameState(skyRenderer);
		stateManager.attach(gameState);

		gameInputState = new GameInput(this);
		stateManager.attach(gameInputState);

		gameGUIState = new GameGUI(this);
		stateManager.attach(gameGUIState);

		mainScreenState = new MainScreen(this);
		stateManager.attach(mainScreenState);

//		chopperState = new ChopperState();
//		stateManager.attach(chopperState);

		infoTracing = new InfoTracing();
	//	stateManager.attach(infoTracing);

		//toggleMenu();
		inputManager.setCursorVisible(true);
	}

	void checkOpenGLShadingVersion() {
//		float openglShadingVersion = Float.parseFloat(GL11.glGetString(GL20.GL_SHADING_LANGUAGE_VERSION));
//		if (openglShadingVersion < MIN_OPENGL_SHADING_VERSION) {
//			log.log(Level.SEVERE, "!!!!!!GLSL version lower than {0}: {1}",
//					new Object[]{MIN_OPENGL_SHADING_VERSION, openglShadingVersion});
//		}
//		else {
//			log.log(Level.INFO, "GLSL version: {0}", openglShadingVersion);
//		}
	}

	void setupCamera() {
		cam.setFrustumPerspective(60f, (float) cam.getWidth() / cam.getHeight(), 0.1f, 5000f);

		CameraNode cameraNode = new CameraNode("flyCam", cam);
		rootNode.attachChild(cameraNode);
		camFlyControl = new CamFlyControl();
		cameraNode.addControl(camFlyControl);
		camFlyControl.setEnabled(false);
	}

	public void resume() {
		enbableMenu(false);
	}

	public void newGame() {
		stateManager.detach(bulletAppState);
		bulletAppState = new BulletAppState();
		bulletAppState.setThreadingType(BulletAppState.ThreadingType.PARALLEL);
		stateManager.attach(bulletAppState);
		Globals.setBulletAppState(bulletAppState);

		gameState.restart();
		enbableMenu(false);
	}

	public void quit() {
		stop();
	}

	public void toggleHelp() {
		gameGUIState.toggleHelp();
	}

	public void toggleMenu() {
		boolean enable = !mainScreenState.isEnabled();
		enbableMenu(enable);
	}

	private void enbableMenu(boolean enable) {
		inputManager.setCursorVisible(enable);

		mainScreenState.setEnabled(enable);
		gameInputState.setEnabled(!enable);
	}

	public void toggleFurnitureDebugDisplay() {
		//interiorDesigner.toggleFurnitureDebugDisplay();
	}

	public void toggleZombie() {
		gameState.toggleZombie();
	}

	public void togglePhysicsDebug() {
		bulletAppState.setDebugEnabled(!bulletAppState.isDebugEnabled());
	}

	public void setDefaultGameSpeed() {
		speed = 1.0f;
	}

	public void decreaseGameSpeed() {
		speed -= 0.1f;
		if (speed < 0.1f) {
			speed = 0.1f;
		}
	}

	public void increaseGameSpeed() {
		speed += 0.1f;
	}

	public void toggleLimitFPS() {
		fpsLimited = !fpsLimited;
	}

	public void toggleMouseCursorLock() {
		boolean visible = !inputManager.isCursorVisible();
		inputManager.setCursorVisible(visible);
		gameInputState.enablePlayerInput(!visible);
	}

	public void toggleShowDebugTextures() {
		boolean enable = !debugPictureProcessor.isEnabled();
		debugPictureProcessor.setEnabled(enable);
	}

	public void toggleShowLPVProbe() {
		lpvProcessor.toggleShowLPVProbe();
	}

	public void toggleCameraLock() {
		freeCamera = !freeCamera;

	//	chopperState.setEnabled(freeCamera);

		camFlyControl.setEnabled(freeCamera);
		gameInputState.enablePlayerInput(!freeCamera);
	}

	public void refurnishAllRooms() {
		currentSeed++;
		Globals.getRandom().setSeed(currentSeed);
		log.log(Level.INFO, "currentSeed: {0}", currentSeed);
		Globals.getRandom().nextInt();

		lpvProcessor.reset();
		gameState.refurnishAllRooms();
	}

	@Override
	public void simpleUpdate(float tpf) {
		rootNode.updateGeometricState();
		menuWorldSlowDownEffect();

		listener.setLocation(cam.getLocation());
		listener.setRotation(cam.getRotation());

		// frame limiter
		if (fpsLimited) {
			long delta = (long) (getTimer().getTimePerFrame() * 1000L);
			if (delta < 40) { // 25 FPS
				try {
					Thread.sleep(40 - delta);
				} catch (InterruptedException ex) {
				}
			}
		}

		// update audio listener position
		listener.setLocation(cam.getLocation());
	}

	@Override
	public void simpleRender(RenderManager rm) {
	}

	private void menuWorldSlowDownEffect() {
		float tpf = getTimer().getTimePerFrame();

		if (mainScreenState.isEnabled()) {
			if (speed > 0.1f) {
				speed -= slowDownSpeed * tpf;
				if (speed < 0.1f) {
					speed = 0.1f;
				}
			}
		}
		else if (speed < 1) {
			speed += speedUpSpeed * tpf;
			if (speed > 1)
				speed = 1;
		}
	}

	@Override
	public void destroy() {
		try {
			super.destroy();
		} catch (Exception ex) {
			log.log(Level.SEVERE, null, ex);
		}
	}

	public InputListener getCamFlyControlInputListener() {
		return camFlyControl;
	}

	public boolean isFPSLimited() {
		return fpsLimited;
	}

	public float getSpeed() {
		return speed;
	}

	public GameState getGameState() {
		return gameState;
	}
}
