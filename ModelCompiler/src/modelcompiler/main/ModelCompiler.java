package modelcompiler.main;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.LoopMode;
import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioNode;
import com.jme3.bounding.BoundingBox;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import common.attributes.Shelves;
import common.cameras.OrbitalCamera;
import common.gizmos.Gizmos;
import common.hdr.HDRProcessor;
import common.logging.Logging;
import common.material.DefaultMaterial;
import common.material.MaterialLoader;
import common.material.RandomMaterialSelector;
import common.render.lpv.LPVProcessor;
import common.utils.ScrollableMessageDialog;
import de.lessvoid.nifty.Nifty;
import java.awt.Toolkit;
import java.awt.datatransfer.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Optional;
import java.util.Random;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import lombok.extern.java.Log;
import modelcompiler.gui.MainScreen;
import modelcompiler.gui.MainScreenListener;
import modelcompiler.io.MaterialExternLoader;
import modelcompiler.io.ModelExporter;
import modelcompiler.io.ModelImporter;
import modelcompiler.modifiers.centerfixer.CenterFixer;
import modelcompiler.modifiers.shelffinder.ShelfFinder;
import modelcompiler.properties.ModelCompilerProperties;
import org.lwjgl.opengl.Display;
import skybox.SkyRenderer;

@Log
public class ModelCompiler extends SimpleApplication implements MainScreenListener {

	public static void main(String[] args) {
		Logging.setGlobalLevel(Level.SEVERE);

		ModelCompiler app = new ModelCompiler(args);
		AppSettings settings = new AppSettings(true);

		app.setupSettings(settings);
		app.setShowSettings(false);
		app.setSettings(settings);
		app.start();

		Display.setResizable(true);
	}

	private String fileToImportOnLaunch;

	private Spatial model;
	private OrbitalCamera orbitalCam;
	private MainScreen mainScreen;
	private Vector3f extents = new Vector3f(0, 0, 0);

	private final String GUI_DEFINITION_FILE = "Interface/ModelCompiler.xml";

	private LPVProcessor lpvProcessor;

	private NiftyJmeDisplay niftyDisplay;
	private AnimChannel animChannel;
	private AnimControl animControl;

	private final Calendar localTime = new GregorianCalendar(2016, Calendar.AUGUST, 8, 15, 12, 0);
	private SkyRenderer skyRenderer;
	private HDRProcessor hdrProcessor;

	private Node clusterNode;
	private Node shelvesNode;

	public ModelCompiler(String[] args) {
		if (args.length > 0)
			fileToImportOnLaunch = args[0];
	}

	private void setupSettings(AppSettings settings) {
		settings.setResolution(800, 600);
		settings.setFrameRate(30);
		settings.setTitle("ModelCompiler");

		try {
			BufferedImage[] list = {ImageIO.read(new File("modelCompiler.png")), ImageIO.read(new File("modelCompiler.png"))};
			settings.setIcons(list);
		} catch (IOException ex) {
			log.log(Level.WARNING, null, ex);
		}
	}

	@Override
	public void simpleInitApp() {
		setDisplayFps(false);
		setDisplayStatView(false);

		setupLookAndFeel();

		flyCam.setEnabled(false);
		orbitalCam = new OrbitalCamera(cam, rootNode, inputManager, settings);
		
		MaterialLoader.init(assetManager);

		Random random = new Random();
		Globals.setRootNode(rootNode);
		Globals.setAssetManager(assetManager);
		Globals.setRandom(random);
		Globals.setMaterialSelector(new RandomMaterialSelector(assetManager, random));

		DefaultMaterial.setAssetManager(assetManager);

		clusterNode = new Node();
		rootNode.attachChild(clusterNode);
		stateManager.attach(new TriangleClusterToggler(clusterNode));

		shelvesNode = new Node();
		rootNode.attachChild(shelvesNode);

		setupProcessors();

		loadGUI();
		updateGUIData();
		setupInput();
		setupLight();
		setupGizmos();

		AudioNode launchSound = new AudioNode(assetManager, "Sounds/start.wav", false);
		launchSound.setName("launchSound");
		rootNode.attachChild(launchSound);
	}

	private void setupProcessors() {
		lpvProcessor = new LPVProcessor(assetManager, settings.getWidth(), settings.getHeight(), true);
		lpvProcessor.setLPVShape(new Vector3f(-4, -2, -4), new Vector3f(8, 4, 8));

		skyRenderer = SkyRenderer.builder()
				.assetManager(assetManager)
				.localTime(localTime)
				.hdrResultFrameBuffer(lpvProcessor.getHdrResultFrameBuffer())
				.screenWidth(viewPort.getCamera().getWidth())
				.screenHeight(viewPort.getCamera().getHeight())
				.settingsScriptPath("assets/Lua/sky.lua")
				.build();

		hdrProcessor = new HDRProcessor(assetManager);
		hdrProcessor.setHDRSceneTexture(lpvProcessor.getHdrResultTexture());

		viewPort.addProcessor(lpvProcessor);
		viewPort.addProcessor(skyRenderer);
		viewPort.addProcessor(hdrProcessor);

		lpvProcessor.setSunDirection(skyRenderer.getSunDirection());
	}

	private void setupGizmos() {
		Gizmos.init(assetManager);
		rootNode.attachChild(Gizmos.createGrid(15, 0.5f, ColorRGBA.DarkGray));
		rootNode.attachChild(Gizmos.createAxis(2, 1));

		Material whiteMat = DefaultMaterial.withColor(new Vector3f(1, 1, 1));
		rootNode.attachChild(createFrontHintText(whiteMat));
	}

	private Node createFrontHintText(Material mat) {
		Spatial frontText = assetManager.loadModel("Models/front.j3o");
		frontText.setMaterial(mat);
		frontText.setLocalTranslation(0, 0, 3);
		frontText.setLocalScale(0.5f);
		Node node = new Node();
		node.attachChild(frontText);
		return node;
	}

	/**
	 * Sets up windows look and feel for swing dialogs (e.g. file chooser)
	 */
	private void setupLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
			log.log(Level.SEVERE, null, ex);
		}
	}

	private void setupLight() {
		Node flashlight = new Node("flashlight");
		rootNode.attachChild(flashlight);
		lpvProcessor.setFlashLight(flashlight);
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
			log.log(Level.SEVERE, null, ex);
			ScrollableMessageDialog.showException("Failed to load " + GUI_DEFINITION_FILE, ex);
		}
	}

	private void setupInput() {
		inputManager.addMapping("reloadXMLGUI", new KeyTrigger(KeyInput.KEY_F2));
		inputManager.addListener((ActionListener) (String name, boolean isPressed, float tpf) -> {
			if (isPressed) {
				switch (name) {
					case "reloadXMLGUI":
						reloadXMLGUI();
						break;
				}
			}
		}, new String[]{"reloadXMLGUI"});
	}

	@Override
	public void afterFirstUpdate() {
		inputManager.setCursorVisible(true); // display mouse cursor

		AudioNode audioNode = (AudioNode) rootNode.getChild("launchSound");
		audioNode.play();

		if (fileToImportOnLaunch != null) {
			ModelImporter.modelFromFile(new File(fileToImportOnLaunch)).ifPresent(this::replaceModel);
		}
	}

	@Override
	public void simpleRender(RenderManager rm) {
		Spatial flashlight = rootNode.getChild("flashlight");
		if (flashlight != null && cam != null) {
			flashlight.setLocalTranslation(cam.getLocation());
			flashlight.setLocalRotation(cam.getRotation());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void updateGUIData() {
		if (mainScreen.isInitialized()) {
			if (animControl != null)
				mainScreen.setAnimationSet(animControl.getAnimationNames());
			else
				mainScreen.setAnimationSet(Collections.EMPTY_SET);
		}
	}

	@Override
	public void simpleUpdate(float tpf) {
		super.simpleUpdate(tpf);
	}

	@Override
	public void importModel() {
		ModelImporter.importSpatial().ifPresent(this::replaceModel);
	}

	@Override
	public void openModel() {
		ModelImporter.openSpatial().ifPresent(this::replaceModel);
	}

	@Override
	public void loadMaterial() {
		Optional<Material> material = MaterialExternLoader.load();
		if (model != null && material.isPresent())
			model.setMaterial(material.get());
	}

	public void replaceModel(Spatial newModel) {
		if (model != null)
			rootNode.detachChild(model);

		model = newModel;

		((Node) model).getChildren().forEach(child -> Globals.getMaterialSelector().applyByMeshName(child));

		animControl = model.getControl(AnimControl.class);
		if (animControl != null)
			animChannel = animControl.createChannel();

		rootNode.attachChild(model);
		
		clusterNode.detachAllChildren();
		shelvesNode.detachAllChildren();

		orbitalCam.frame(model);
		// change window title
		//String filePath = model.getName().getAbsolutePath();
		Display.setTitle("ModelCompiler - " + model.getName());

		updateGUIData();
	}

	@Override
	public void playAnimation(String name) {
		if (animChannel != null) {
			animChannel.setAnim(name);
			animChannel.setLoopMode(LoopMode.Loop);
		}
	}

	@Override
	public void saveModel() {
		ModelExporter.export(model, assetManager);
	}

	@Override
	public void centerExtents() {
		extents = CenterFixer.fix(assetManager, model); // make bounding box offset to center	zero
	}

	@Override
	public void copyExtents() {
		String myString = extents.toString();
		StringSelection stringSelection = new StringSelection(myString);
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, null);
	}

	@Override
	public void findShelves() {
		clusterNode.detachAllChildren();
		shelvesNode.detachAllChildren();
		
		ShelfFinder.find(assetManager, model, clusterNode);

		Shelves shelves = model.getAttribute(Shelves.class);
		if (shelves != null) {
			for (BoundingBox box : shelves.getShelves()) {
				addShelfBox("shelf", box);
			}
		}
	}

	@Override
	public void setShowShelves(boolean show) {
		Spatial.CullHint cull = show ? Spatial.CullHint.Inherit : Spatial.CullHint.Always;
		clusterNode.setCullHint(cull);
		shelvesNode.setCullHint(cull);
	}

	private void addShelfBox(String name, BoundingBox box) {
		Geometry geom = new Geometry(name, new Box(box.getXExtent(), box.getYExtent(), box.getZExtent()));
		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		mat.getAdditionalRenderState().setWireframe(true);
		mat.setColor("Color", ColorRGBA.randomColor());
		geom.setMaterial(mat);
		geom.setLocalTranslation(box.getCenter());
		geom.setQueueBucket(RenderQueue.Bucket.Transparent);
		shelvesNode.attachChild(geom);
	}
}
