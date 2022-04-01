package crawler.main.states;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.font.BitmapText;
import com.jme3.input.InputManager;
import com.jme3.input.KeyNames;
import com.jme3.input.Mapping;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import crawler.locale.Strings;
import crawler.main.CrawlerApp;
import static crawler.main.CrawlerAppBase.guiFont;
import crawler.main.Globals;
import crawler.navmesh.NavMesh;
import crawler.navmesh.navpoly.NavPoly;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @author VTPlusAKnauer
 */
public class GameGUI extends AbstractAppState {

	private final CrawlerApp crawlerApp;
	private BitmapText hudText, hudTextBG;
	private DecimalFormat df;
	private boolean showHelp;

	public GameGUI(CrawlerApp crawlerApp) {
		this.crawlerApp = crawlerApp;
	}

	@Override
	public void initialize(AppStateManager stateManager, Application app) {
		DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
		otherSymbols.setDecimalSeparator('.');
		otherSymbols.setGroupingSeparator(',');
		df = new DecimalFormat("###.##", otherSymbols);

		setupGUI();
	}

	void setupGUI() {
		// temporary hud text info
		hudText = new BitmapText(guiFont, false);
		hudText.setSize(guiFont.getCharSet().getRenderedSize());      // font size
		hudText.setColor(ColorRGBA.Yellow);                             // font color
		hudText.setLocalScale(1);
		hudText.setLocalTranslation(0, crawlerApp.getCamera().getHeight(), 0); // position

		hudTextBG = new BitmapText(guiFont, false);
		hudTextBG.setSize(guiFont.getCharSet().getRenderedSize());      // font size
		hudTextBG.setColor(ColorRGBA.Black);                             // font color
		hudTextBG.setLocalScale(1);
		hudTextBG.setLocalTranslation(hudText.getLocalTranslation().add(1, -1, 0)); // position

		crawlerApp.getGuiNode().attachChild(hudTextBG);
		crawlerApp.getGuiNode().attachChild(hudText);
	}

	@Override
	public void update(float tpf) {
		StringBuffer buffer = new StringBuffer();
		if (crawlerApp.isFPSLimited())
			buffer.append("frame limited (L), ");
		buffer.append("speed (+/-): ").append(df.format(crawlerApp.getSpeed())).append("\n");

		Vector3f playerPos = Globals.getPlayer().getWorldTranslation();
		Optional<NavMesh> mesh = Globals.getSuperNavMesh().findNavMesh(playerPos);
		Optional<NavPoly> poly = null;
		if (mesh.isPresent())
			poly = mesh.get().getPolygonByLocation(playerPos);

		buffer.append("pos: ").append(playerPos.format(df))
				.append(", navmesh: ").append(mesh)
				.append(", navpoly: ").append(poly);

//		if (Globals.getPlayer().getHitPositionHistory().size() > 1) {
//			Iterator<Vector3f> hitPosIterator = Globals.getPlayer().getHitPositionHistory().iterator();
//			Vector3f currentHitPos = hitPosIterator.next();
//			Vector3f lastHitPos = hitPosIterator.next();
//			float dist = currentHitPos.distance(lastHitPos);
//
//			buffer.append("currentHit: ").append(currentHitPos.format(df))
//					.append(", lastHit: ").append(lastHitPos.format(df))
//					.append(", dist: ").append(dist).append("\n");
//		}
		if (showHelp) {
			buffer.append("\n");
			buildInputBindingInfo(buffer, crawlerApp.getInputManager());
		}

		hudText.setText(buffer);
		hudTextBG.setText(hudText.getText());
	}

	public void toggleHelp() {
		showHelp = !showHelp;
	}

	private void buildInputBindingInfo(StringBuffer buffer, InputManager inputManager) {
		Map<String, Mapping> mappings = inputManager.getMappings();
		for (Mapping mapping : mappings.values()) {
			if (mapping.isMappedByKey()) {
				buffer.append(Strings.get("input." + mapping.getName())).append(": ")
						.append(KeyNames.getName(mapping.getTriggers().get(0)).orElse(""))
						.append("\n");
			}
		}

	}
}
