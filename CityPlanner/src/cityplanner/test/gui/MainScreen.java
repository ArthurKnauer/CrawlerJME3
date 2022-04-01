package cityplanner.test.gui;

import cityplanner.test.CityPlannerApp;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;

/**
 *
 */
public class MainScreen extends AbstractAppState implements ScreenController {

	private final CityPlannerApp app;
	private boolean bound = false;

	public MainScreen(CityPlannerApp app) {
		this.app = app;
	}

	// Nifty GUI ScreenControl methods
	/**
	 *
	 * @param nifty
	 * @param screen
	 */
	@Override
	public void bind(Nifty nifty, Screen screen) {
		bound = true;
		app.mainscreenBindEvent();
	}

	public boolean isBound() {
		return bound;
	}

	@Override
	public void initialize(AppStateManager stateManager, Application app) {
	}

	@Override
	public void update(float tpf) {
	}

	@Override
	public void onStartScreen() {
	}

	@Override
	public void onEndScreen() {
	}

	public void onPlanRoads() {
		app.planRoads();
	}

	public void onToggleGrid() {
		app.toggleGrid();
	}

	public void onTogglePopulationMap() {
		app.getCityScape().togglePopulationMap();
	}

	public void onToggleSky() {
		app.toggleSky();
	}

	public void onToggleWorldPlane() {
		app.getCityScape().toggleWorldPlane();
	}

	public void onToggleRoads() {
		app.getCityScape().toggleRoads();
	}

	public void onToggleBuildings() {
		app.getCityScape().toggleBuildings();
	}

	public void onToggleTrees() {
		app.getCityScape().toggleTrees();
	}

	public void onToggleDebugInfo() {
		app.getCityScape().toggleDebugInfo();
	}
}
