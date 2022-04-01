/*
 * To change this license header, choose License Headers in Project CrawlerProperties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package crawler.main.states;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import crawler.debug.GeometryBuilder;
import crawler.main.Globals;
import crawler.physics.Trace;
import static crawler.properties.CrawlerProperties.PROPERTIES;

/**
 *
 * @author VTPlusAKnauer
 */
public class InfoTracing extends AbstractAppState {

	private BitmapText infoText;
	
	private static final float TRACE_DIST = PROPERTIES.getFloat("InfoTracing.traceDist");
	private static final float TEXT_SCALE = PROPERTIES.getFloat("InfoTracing.textScale");

	@Override
	public void initialize(AppStateManager stateManager, Application app) {
		super.initialize(stateManager, app);

		infoText = GeometryBuilder.buildText("InfoTracing", Vector3f.ZERO, ColorRGBA.Green);

		infoText.setQueueBucket(RenderQueue.Bucket.Overlay);
		infoText.setCullHint(Spatial.CullHint.Never);

		Globals.getRootNode().attachChild(infoText);
	}

	@Override
	public void update(float tpf) {
		if (isEnabled()) {
			Node playerEye = Globals.getPlayer().getEyeNode();

			Trace.traceSpatialForward(playerEye, TRACE_DIST).ifPresent(hit -> {
				infoText.setLocalTranslation(hit.getHitLocation());
				infoText.setSize(hit.getHitFraction() * TEXT_SCALE);
				
				infoText.setText(hit.getCollisionObject().getUserObject().toString());
			});
		}
	}

}
