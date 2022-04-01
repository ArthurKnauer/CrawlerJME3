package crawler.main.states;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import crawler.characters.player.PlayerBipedControl;
import crawler.characters.player.PlayerInputControl;
import crawler.main.CrawlerApp;
import crawler.main.Globals;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author VTPlusAKnauer
 */
public class GameInput extends AbstractAppState implements ActionListener {

	private final CrawlerApp crawlerApp;
	private final Map<String, Runnable> commands;

	public GameInput(CrawlerApp crawlerApp) {
		this.crawlerApp = crawlerApp;
		this.commands = new HashMap<>();
	}

	@Override
	public void initialize(AppStateManager stateManager, Application app) {
		setupKeys(app.getInputManager());
		setupCommands();
	}

	private void setupKeys(InputManager inputManager)  {
		inputManager.addMapping("help", new KeyTrigger(KeyInput.KEY_F1));
		inputManager.addMapping("menu", new KeyTrigger(KeyInput.KEY_ESCAPE));
		inputManager.addMapping("show_furniture_agents", new KeyTrigger(KeyInput.KEY_F2));
		inputManager.addMapping("furnish", new KeyTrigger(KeyInput.KEY_F));

		inputManager.addMapping("zombie", new KeyTrigger(KeyInput.KEY_Z));
		inputManager.addMapping("phys_debug", new KeyTrigger(KeyInput.KEY_G));
		inputManager.addMapping("frame_limit", new KeyTrigger(KeyInput.KEY_L));
		inputManager.addMapping("mouse_lock", new KeyTrigger(KeyInput.KEY_M));
		inputManager.addMapping("toggle_debug_pics", new KeyTrigger(KeyInput.KEY_V));
		inputManager.addMapping("lpv_show_probe", new KeyTrigger(KeyInput.KEY_P));
		inputManager.addMapping("show_navmesh", new KeyTrigger(KeyInput.KEY_N));
		inputManager.addMapping("speed_1", new KeyTrigger(KeyInput.KEY_NUMPAD1));
		inputManager.addMapping("speed_plus", new KeyTrigger(KeyInput.KEY_ADD));
		inputManager.addMapping("speed_minus", new KeyTrigger(KeyInput.KEY_SUBTRACT));
		inputManager.addMapping("camera_lock", new KeyTrigger(KeyInput.KEY_C));

		inputManager.addMapping("forward", new KeyTrigger(KeyInput.KEY_W));
		inputManager.addMapping("left", new KeyTrigger(KeyInput.KEY_A));
		inputManager.addMapping("backward", new KeyTrigger(KeyInput.KEY_S));
		inputManager.addMapping("right", new KeyTrigger(KeyInput.KEY_D));
		inputManager.addMapping("use", new KeyTrigger(KeyInput.KEY_E));
		inputManager.addMapping("aim", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
		inputManager.addMapping("fire", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
		inputManager.addMapping("run", new KeyTrigger(KeyInput.KEY_LSHIFT));

		inputManager.addMapping("right_turn", new MouseAxisTrigger(MouseInput.AXIS_X, true));
		inputManager.addMapping("left_turn", new MouseAxisTrigger(MouseInput.AXIS_X, false));
		inputManager.addMapping("look_up", new MouseAxisTrigger(MouseInput.AXIS_Y, true));
		inputManager.addMapping("look_down", new MouseAxisTrigger(MouseInput.AXIS_Y, false));

		inputManager.addMapping("next_weapon", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));
		inputManager.addMapping("prev_weapon", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));

		inputManager.addListener(this, new String[]{"help", "menu", "show_furniture_agents", "zombie",
													"phys_debug", "frame_limit", "mouse_lock",
													"toggle_debug_pics", "show_navmesh", "furnish", "speed_1",
													"speed_plus", "speed_minus", "camera_lock", "lpv_show_probe"});

		PlayerInputControl playerInputControl = Globals.getPlayer().getControl(PlayerInputControl.class);
		inputManager.addListener(playerInputControl, new String[]{"forward", "left", "backward", "right", "aim", "fire", "use",
																  "right_turn", "left_turn", "look_up", "look_down", "next_weapon", "prev_weapon"});

		inputManager.addListener(crawlerApp.getCamFlyControlInputListener(), new String[]{"run", "forward", "left", "backward", "right",
																						  "right_turn", "left_turn", "look_up", "look_down"});
	}

	@Override
	public void onAction(String name, boolean isPressed, float tpf) {
		if (!name.equals("menu") && !isEnabled())
			return;

		if (!isPressed) {
			Runnable command = commands.get(name);
			if (command != null) {
				command.run();
			}
			else {
				throw new IllegalArgumentException("Unmapped command " + name);
			}
		}
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		enablePlayerInput(enabled);
	}

	public void enablePlayerInput(boolean enable) {
		Globals.getPlayer().getControl(PlayerInputControl.class).setEnabled(enable);
		Globals.getPlayer().getControl(PlayerBipedControl.class).enableEyeCameraControl(enable);
	}

	private void setupCommands() {
		commands.put("help", () -> {
				 crawlerApp.toggleHelp();
			 });

		commands.put("menu", () -> {
				 crawlerApp.toggleMenu();
			 });

		commands.put("show_furniture_agents", () -> {
				 crawlerApp.toggleFurnitureDebugDisplay();
			 });

		commands.put("zombie", () -> {
				 crawlerApp.toggleZombie();
			 });

		commands.put("phys_debug", () -> {
				 crawlerApp.togglePhysicsDebug();
			 });

		commands.put("speed_1", () -> {
				 crawlerApp.setDefaultGameSpeed();
			 });

		commands.put("speed_minus", () -> {
				 crawlerApp.decreaseGameSpeed();
			 });

		commands.put("speed_plus", () -> {
				 crawlerApp.increaseGameSpeed();
			 });

		commands.put("camera_lock", () -> {
				 crawlerApp.toggleCameraLock();
			 });

		commands.put("frame_limit", () -> {
				 crawlerApp.toggleLimitFPS();
			 });

		commands.put("mouse_lock", () -> {
				 crawlerApp.toggleMouseCursorLock();
			 });

		commands.put("toggle_debug_pics", () -> {
				 crawlerApp.toggleShowDebugTextures();
			 });

		commands.put("show_navmesh", () -> {
				 crawlerApp.getGameState().toggleShowNavMesh();
			 });

		commands.put("lpv_show_probe", () -> {
				 crawlerApp.toggleShowLPVProbe();
			 });

		commands.put("furnish", () -> {
				 crawlerApp.refurnishAllRooms();
			 });
	}
}
