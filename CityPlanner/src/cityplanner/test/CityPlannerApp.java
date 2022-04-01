package cityplanner.test;

import cityplanner.CityScape;
import cityplanner.test.gui.MainScreen;
import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Plane;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.shadow.BasicShadowRenderer;
import com.jme3.system.AppSettings;
import common.cameras.OrbitalCamera;
import common.gizmos.Gizmos;
import common.utils.ScrollableMessageDialog;
import de.lessvoid.nifty.Nifty;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import jme3utilities.sky.SkyControl;

public class CityPlannerApp extends SimpleApplication {

	private final static Logger LOGGER = Logger.getLogger(CityPlannerApp.class.getName());
	private OrbitalCamera orbitalCam;

	private MainScreen mainScreen;
	private NiftyJmeDisplay niftyDisplay;
	private SkyControl skyControl;

	private CityScape cityScape;
	private int seed = 70;
	private final int mapSize = 10000;

	private final String GUI_DEFINITION_FILE = "Interface/CityPlanner.xml";

	public static void main(String[] args) {
		Logger.getLogger("com.jme3").setLevel(Level.SEVERE);

		CityPlannerApp app = new CityPlannerApp();
		AppSettings settings = new AppSettings(true);
		setupSettings(settings);

		app.setShowSettings(false);
		app.setSettings(settings);
		app.start();
	}

	private static void setupSettings(AppSettings settings) {
		settings.setResolution(1408, 800);
		settings.setFrameRate(30);
		settings.setTitle("CityPlanner");

		try {
			BufferedImage[] list = {ImageIO.read(new File("cityPlanner.png")), ImageIO.read(new File("cityPlanner.png"))};
			settings.setIcons(list);
		} catch (IOException ex) {
			LOGGER.log(Level.WARNING, null, ex);
		}
	}
	private BasicShadowRenderer bsr;
	private Object frustum;

	@Override
	public void simpleInitApp() {
		setDisplayFps(false);
		setDisplayStatView(false);

		setupLookAndFeel();
		setupCamera();

		loadGUI();
		updateGUIData();
		setupInput();
		setupSky();
		setupLight();
		setupGizmos();

		cityScape = new CityScape("cityScape");
		rootNode.attachChild(cityScape);

		planRoads();
	}

	public CityScape getCityScape() {
		return cityScape;
	}

	private void setupCamera() {
		flyCam.setEnabled(false);
		orbitalCam = new OrbitalCamera(cam, rootNode, inputManager, settings);
		orbitalCam.setMaxDistance(mapSize * 1.5f);
		orbitalCam.setDefaultDistance(mapSize / 2);
		orbitalCam.setMinDistance(mapSize / 50);

		cam.setFrustumPerspective(45f, (float) cam.getWidth() / cam.getHeight(), mapSize / 100, mapSize * 2.2f);
	}

	private void setupGizmos() {
		Gizmos.init(assetManager);

		Node grid = new Node("grid");
		grid.attachChild(Gizmos.createGrid(111, 100, new ColorRGBA(0.05f, 0.05f, 0.05f, 1)));
		grid.attachChild(Gizmos.createGrid(11, 1000, new ColorRGBA(0.2f, 0.2f, 0.2f, 1)));
		rootNode.attachChild(grid);

		rootNode.attachChild(Gizmos.createAxis(2, 100));
	}

	/**
	 * Sets up windows look and feel for swing dialogs (e.g. file chooser)
	 */
	private void setupLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
			LOGGER.log(Level.SEVERE, null, ex);
		}
	}

	private void setupSky() {
		skyControl = new SkyControl(assetManager, 0.9f, true, true);
		rootNode.addControl(skyControl);

		skyControl.getSunAndStars().setHour(14);
		skyControl.getSunAndStars().setObserverLatitude(1.5f);
		skyControl.getSunAndStars().setSolarLongitude(6, 10);
		skyControl.setCloudiness(1);
		skyControl.setEnabled(true);
	}

	private void setupLight() {
		AmbientLight ambi = new AmbientLight();
		ambi.setColor(new ColorRGBA(1, 1, 1f, 1));
		rootNode.addLight(ambi);

		DirectionalLight sun = new DirectionalLight();
		sun.setColor(new ColorRGBA(1, 1, 0.8f, 1));
		sun.setDirection(skyControl.getSunAndStars().getSunDirection().negate());
		rootNode.addLight(sun);
	}

	private void loadGUI() {
		niftyDisplay = new NiftyJmeDisplay(assetManager, inputManager, audioRenderer, guiViewPort);
		guiViewPort.addProcessor(niftyDisplay);
		mainScreen = new MainScreen(this);
		reloadXMLGUI();
	}

	private void reloadXMLGUI() {
		try {
			Nifty nifty = niftyDisplay.getNifty();
			nifty.unregisterScreenController(mainScreen);
			//nifty.validateXml(GUI_DEFINITION_FILE);
			nifty.fromXml(GUI_DEFINITION_FILE, "start", mainScreen);
			updateGUIData();
		} catch (Exception ex) {
			LOGGER.log(Level.SEVERE, null, ex);
			ScrollableMessageDialog.showException("Failed to load " + GUI_DEFINITION_FILE, ex);
		}
	}

	private void setupInput() {
		inputManager.setCursorVisible(true); // display mouse cursor		
		inputManager.addMapping("reloadXMLGUI", new KeyTrigger(KeyInput.KEY_F2));
		inputManager.addMapping("click", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
		inputManager.addListener((ActionListener) (String name, boolean isPressed, float tpf) -> {
			if (isPressed) {
				switch (name) {
					case "reloadXMLGUI":
						reloadXMLGUI();
						break;
					case "click":
						onMouseClick();
						break;
				}
			}
		}, new String[]{"reloadXMLGUI", "click"});

	}

	@Override
	public void simpleUpdate(float tpf) {
//		Camera shadowCam = bsr.getShadowCamera();
//		Vector3f[] points;
//		ShadowUtil.updateFrustumPoints2(shadowCam, points);
//
//		frustum.update(points);
	}

	@Override
	public void simpleRender(RenderManager rm) {
	}

	private void onMouseClick() {
		Vector2f click2d = inputManager.getCursorPosition();
		Vector3f click3d = cam.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 0f).clone();
		Vector3f dir = cam.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 1f).subtractLocal(click3d).normalizeLocal();
		Ray ray = new Ray(click3d, dir);

		Vector3f intersection = new Vector3f();
		ray.intersectsWherePlane(new Plane(Vector3f.UNIT_Y.clone(), 0), intersection);
		Vector2f intersection2D = new Vector2f(intersection.x, intersection.z);

		System.out.println(intersection2D);
	}

	public void planRoads() {
		cityScape.build(seed++, assetManager);
	}

	private void updateGUIData() {
		if (mainScreen.isBound()) {
		}
	}

	public void mainscreenBindEvent() {
		updateGUIData();
	}

	public void toggleGrid() {
		toggleSpatial("grid");
	}

	public void toggleSky() {
		skyControl.setEnabled(!skyControl.isEnabled());
	}

	private void toggleSpatial(String name) {
		Spatial spatial = rootNode.getChild(name);
		if (spatial != null) {
			spatial.setCullHint(spatial.getCullHint() == Spatial.CullHint.Always
								? Spatial.CullHint.Inherit : Spatial.CullHint.Always);
		}
	}
}
