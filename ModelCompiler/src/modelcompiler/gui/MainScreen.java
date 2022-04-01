package modelcompiler.gui;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.CheckBox;
import de.lessvoid.nifty.controls.CheckBoxStateChangedEvent;
import de.lessvoid.nifty.controls.DropDown;
import de.lessvoid.nifty.controls.DropDownSelectionChangedEvent;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import java.util.Set;
import java.util.logging.Level;
import lombok.extern.java.Log;

/**
 *
 */
@Log
public class MainScreen extends AbstractAppState implements ScreenController {

	private final MainScreenListener listener;

	private DropDown<String> animationsDropDown;
	private Element animationPanel;

	public MainScreen(MainScreenListener listener) {
		this.listener = listener;
	}

	// Nifty GUI ScreenControl methods
	/**
	 *
	 * @param nifty
	 * @param screen
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void bind(Nifty nifty, Screen screen) {
		animationsDropDown = screen.findNiftyControl("animations", DropDown.class);
		animationPanel = screen.findElementByName("layer").findElementByName("animationPanel");
		
		CheckBox showShelves = screen.findNiftyControl("showShelves", CheckBox.class);
		showShelves.setChecked(true);
	}

	@Override
	public void initialize(AppStateManager stateManager, Application app) {
		super.initialize(stateManager, app);
		listener.updateGUIData();
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

	public void onImportModel() {
		try {
			listener.importModel();
		} catch (Exception ex) {
			log.log(Level.SEVERE, "importModel excpetion", ex);
		}
	}

	public void onOpenModel() {
		try {
			listener.openModel();
		} catch (Exception ex) {
			log.log(Level.SEVERE, "openModel excpetion", ex);
		}
	}

	public void onLoadMaterial() {
		try {
			listener.loadMaterial();
		} catch (Exception ex) {
			log.log(Level.SEVERE, "loadMaterial excpetion", ex);
		}
	}

	public void onSaveModel() {
		try {
			listener.saveModel();
		} catch (Exception ex) {
			log.log(Level.SEVERE, "saveModel excpetion", ex);
		}
	}

	public void onCenterExtents() {
		try {
			listener.centerExtents();
		} catch (Exception ex) {
			log.log(Level.SEVERE, "centerExtents excpetion", ex);
		}
	}

	public void onCopyExtents() {
		try {
			listener.copyExtents();
		} catch (Exception ex) {
			log.log(Level.SEVERE, "copyExtents excpetion", ex);
		}
	}

	public void onFindShelves() {
		try {
			listener.findShelves();
		} catch (Exception ex) {
			log.log(Level.SEVERE, "findShelves excpetion", ex);
		}
	}
	
	@NiftyEventSubscriber(id = "showShelves")
	public void onShowShelves(String id, CheckBoxStateChangedEvent event) {
		listener.setShowShelves(event.getCheckBox().isChecked());
	}

	public void setAnimationSet(Set<String> animationNames) {
		animationPanel.setVisible(!animationNames.isEmpty());
		animationsDropDown.clear();
		for (String name : animationNames) {
			animationsDropDown.addItem(name);
		}
	}

	@NiftyEventSubscriber(id = "animations")
	public void onAnimationSelection(String id, DropDownSelectionChangedEvent<String> event) {
		String name = animationsDropDown.getSelection();
		if (name != null)
			listener.playAnimation(animationsDropDown.getSelection());
	}
}
