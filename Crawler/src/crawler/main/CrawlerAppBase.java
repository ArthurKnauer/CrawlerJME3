package crawler.main;

import com.jme3.app.Application;
import com.jme3.app.DebugKeysAppState;
import com.jme3.app.state.AppState;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeSystem;
import lombok.Getter;
import lombok.Setter;


public abstract class CrawlerAppBase extends Application {

    protected static final String INPUT_MAPPING_CAMERA_POS = DebugKeysAppState.INPUT_MAPPING_CAMERA_POS;
    protected static final String INPUT_MAPPING_MEMORY = DebugKeysAppState.INPUT_MAPPING_MEMORY;
    protected static final String INPUT_MAPPING_HIDE_STATS = "SIMPLEAPP_HideStats";
	public static BitmapFont guiFont;
                                                                         
    @Getter protected Node rootNode = new Node("Root Node");
    @Getter protected Node guiNode = new Node("Gui Node");
    protected BitmapText fpsText;
    @Getter @Setter protected boolean showSettings = true;

    public CrawlerAppBase() {
        this(new DebugKeysAppState() );
    }

    public CrawlerAppBase( AppState... initialStates ) {
        super();
        
        if (initialStates != null) {
            for (AppState a : initialStates) {
                if (a != null) {
                    stateManager.attach(a);
                }
            }
        }
    }

    @Override
    public void start() {
        // set some default settings in-case
        // settings dialog is not shown
        boolean loadSettings = false;
        if (settings == null) {
            setSettings(new AppSettings(true));
            loadSettings = true;
        }

        // show settings dialog
        if (showSettings) {
            if (!JmeSystem.showSettingsDialog(settings, loadSettings)) {
                return;
            }
        }
        //re-setting settings they can have been merged from the registry.
        setSettings(settings);
        super.start();
    }

    /**
     *  Creates the font that will be set to the guiFont field
     *  and subsequently set as the font for the stats text.
	 * @return 
     */
    protected BitmapFont loadGuiFont() {
        return assetManager.loadFont("Interface/Fonts/Default.fnt");
    }

    @Override
    public void initialize() {
        super.initialize();

        // Several things rely on having this
        guiFont = loadGuiFont();
		Globals.setDefaultFont(guiFont);

        guiNode.setQueueBucket(Bucket.Gui);
        guiNode.setCullHint(CullHint.Never);
        viewPort.attachScene(rootNode);
        guiViewPort.attachScene(guiNode);

        // call user code
        simpleInitApp();
    }

    @Override
    public void update() {
        super.update(); // makes sure to execute AppTasks
        if (paused) {
            return;
        }

        float tpf = timer.getTimePerFrame() * speed;
		if (tpf > 0.2f) {
			System.err.println("Too much time per frame: " + tpf + "!!!");
			return;
		}

        // update states
        stateManager.update(tpf);

        // simple update and root node
        simpleUpdate(tpf);
 
        rootNode.updateLogicalState(tpf);
        guiNode.updateLogicalState(tpf);
        
        rootNode.updateGeometricState();
        guiNode.updateGeometricState();

        // render states
        stateManager.render(renderManager);
        renderManager.render(tpf, context.isRenderable());
        simpleRender(renderManager);
        stateManager.postRender();        
    }

    public abstract void simpleInitApp();

    public abstract void simpleUpdate(float tpf);

    public abstract void simpleRender(RenderManager rm);
}
