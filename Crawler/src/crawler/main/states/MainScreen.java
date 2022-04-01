package crawler.main.states;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.niftygui.NiftyJmeDisplay;
import crawler.main.CrawlerApp;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.Label;
import de.lessvoid.nifty.controls.TextField;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;

/**
 *
 */
public class MainScreen extends AbstractAppState implements ScreenController {

	private Nifty nifty;
	private final CrawlerApp crawlerApp;
	private Screen screen;
	private TextField enterField;
	private Label chatField;
	private Label status;
	private TextField ipField;

	public MainScreen(CrawlerApp crawlerApp) {
		this.crawlerApp = crawlerApp;
		NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(crawlerApp.getAssetManager(),
														   crawlerApp.getInputManager(),
														   crawlerApp.getAudioRenderer(),
														   crawlerApp.getGuiViewPort());
		nifty = niftyDisplay.getNifty();
		nifty.fromXml("Interface/MainScreen.xml", "start", this);
		crawlerApp.getGuiViewPort().addProcessor(niftyDisplay);
	}
	
	@Override
	public void bind(Nifty nifty, Screen screen) {
		this.nifty = nifty;
		this.screen = screen;

//		chatField = screen.findNiftyControl("chat_text", Label.class);
//		enterField = screen.findNiftyControl("text_input", TextField.class);
//		status = screen.findNiftyControl("status", Label.class);
//		ipField = screen.findNiftyControl("ip_input", TextField.class);

	}
	
	public void onResume() {
		crawlerApp.resume();
	}
	
	public void onNewGame() {
		crawlerApp.newGame();
	}
	
	public void onQuit() {
		crawlerApp.quit();
	}


	@Override
	public void initialize(AppStateManager stateManager, Application app) {
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);

		if (enabled)
			nifty.gotoScreen("start");
		else
			nifty.gotoScreen("empty");
	}

	
	@Override
	public void onStartScreen() {
	}

	@Override
	public void onEndScreen() {
	}

	@Override
	public void update(float tpf) {
	}
}
