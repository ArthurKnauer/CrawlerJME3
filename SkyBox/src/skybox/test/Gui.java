package skybox.test;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;

/**
 *
 * @author VTPlusAKnauer
 */
class Gui extends AbstractAppState {

	private BitmapText hudText, hudTextBG;
	private final BitmapFont guiFont;
	private SimpleApplication app;

	Gui(BitmapFont guiFont) {
		this.guiFont = guiFont;
	}

	@Override
	public void initialize(AppStateManager stateManager, Application app) {
		this.app = (SimpleApplication) app;

		setupGUI();
	}

	void setupGUI() {
		hudText = new BitmapText(guiFont, false);
		hudText.setSize(guiFont.getCharSet().getRenderedSize());
		hudText.setColor(ColorRGBA.Yellow);
		hudText.setLocalScale(1);

		hudTextBG = new BitmapText(guiFont, false);
		hudTextBG.setSize(guiFont.getCharSet().getRenderedSize());
		hudTextBG.setColor(ColorRGBA.Black);
		hudTextBG.setLocalScale(1);

		app.getGuiNode().attachChild(hudTextBG);
		app.getGuiNode().attachChild(hudText);

		updateHudTextPosition();
	}

	@Override
	public void update(float tpf) {
		updateHudTextPosition(); // update position if window rescaled
	}

	private void updateHudTextPosition() {
		hudText.setLocalTranslation(0, app.getCamera().getHeight(), 0); // position
		hudTextBG.setLocalTranslation(hudText.getLocalTranslation().add(1, -1, 0)); // position
	}

	public void setHudText(CharSequence text) {
		hudText.setText(text);
		hudTextBG.setText(text);
	}
}
