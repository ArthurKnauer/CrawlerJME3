package skybox.test;

import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.renderer.RenderManager;
import com.jme3.system.AppSettings;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image;
import com.jme3.texture.Texture2D;
import common.cameras.OrbitalCamera;
import common.gizmos.Gizmos;
import common.logging.Logging;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import lombok.extern.java.Log;
import org.lwjgl.opengl.Display;
import skybox.SkyRenderer;
import common.hdr.HDRProcessor;

@Log
class SkyBoxTest extends SimpleApplication {

	public static void main(String[] args) {
		Logging.setGlobalLevel(Level.SEVERE);

		SkyBoxTest app = new SkyBoxTest();
		AppSettings settings = new AppSettings(true);

		app.setupSettings(settings);
		app.setShowSettings(false);
		app.setSettings(settings);
		app.start();

		Display.setResizable(true);
	}

	private OrbitalCamera orbitalCam;
	private SkyRenderer skyRenderer;
	private HDRProcessor hdrProcessor;

	private Texture2D hdrResultTexture;
	private FrameBuffer hdrResultFrameBuffer;

	private final StringBuilder stringBuilder = new StringBuilder(64);
	private Gui gui;
	private final Calendar localTime = new GregorianCalendar(2016, Calendar.AUGUST, 8, 0, 0, 0);

	private void setupSettings(AppSettings settings) {
		settings.setResolution(1600, 800);
		settings.setFrameRate(30);
		settings.setTitle("SkyBoxTest");
	}

	@Override
	public void simpleInitApp() {
		setDisplayFps(false);
		setDisplayStatView(false);

		setupLookAndFeel();
		flyCam.setEnabled(false);
		orbitalCam = new OrbitalCamera(cam, rootNode, inputManager, settings);

		cam.setFrustumPerspective(35f, (float) cam.getWidth() / cam.getHeight(), 0.1f, 10f);

		setupInput();
		setupGizmos();
		setupProcessors();

		updateSettings();

		gui = new Gui(SimpleApplication.guiFont);
		stateManager.attach(gui);
	}

	private void setupGizmos() {
		Gizmos.init(assetManager);
	}

	private void setupProcessors() {		
		createFrameBufferAndTexture(cam.getWidth(), cam.getHeight());
		
		skyRenderer = SkyRenderer.builder()
				.assetManager(assetManager)
				.localTime(localTime)
				.hdrResultFrameBuffer(hdrResultFrameBuffer)
				.screenWidth(viewPort.getCamera().getWidth())
				.screenHeight(viewPort.getCamera().getHeight())
				.settingsScriptPath("assets/Lua/settings.lua")
				.build();

		viewPort.addProcessor(skyRenderer);

		hdrProcessor = new HDRProcessor(assetManager);
		hdrProcessor.setHDRSceneTexture(hdrResultTexture);
		viewPort.addProcessor(hdrProcessor);
	}

	private void createFrameBufferAndTexture(int width, int height) {
		hdrResultTexture = new Texture2D(width, height, Image.Format.RGB16F);

		if (hdrResultFrameBuffer != null) {
			renderer.deleteFrameBuffer(hdrResultFrameBuffer);
		}

		hdrResultFrameBuffer = new FrameBuffer(width, height, 1);
		hdrResultFrameBuffer.setColorTexture(hdrResultTexture);
	}

	public void updateSettings() {
	}

	private void setupLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
			log.log(Level.SEVERE, "setupLookAndFeel failed: {0}", ex);
		}
	}

	private void setupInput() {
		inputManager.addMapping("reloadXMLGUI", new KeyTrigger(KeyInput.KEY_F2));
		inputManager.addListener((ActionListener) (String name, boolean isPressed, float tpf) -> {
			if (isPressed) {
				switch (name) {
					case "reloadXMLGUI":
						break;
				}
			}
		}, new String[]{"reloadXMLGUI"});
	}

	@Override
	public void afterFirstUpdate() {
		inputManager.setCursorVisible(true); // display mouse cursor
	}

	@Override
	public void simpleRender(RenderManager rm) {
		renderer.setFrameBuffer(hdrResultFrameBuffer);
		renderer.clearBuffers(true, true, true);
	}

	@Override
	public void reshape(int width, int height) {
		super.reshape(width, height);

		createFrameBufferAndTexture(width, height);
		hdrProcessor.setHDRSceneTexture(hdrResultTexture);
	}

	@Override
	public void simpleUpdate(float tpf) {
		super.simpleUpdate(tpf);

		updateGui();
	}

	private void updateGui() {
		stringBuilder.setLength(0);
		stringBuilder.append(localTime.getTime());
		gui.setHudText(stringBuilder);
	}
}
