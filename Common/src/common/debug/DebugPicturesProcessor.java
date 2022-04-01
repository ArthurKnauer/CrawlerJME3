/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common.debug;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.post.SceneProcessor;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.texture.FrameBuffer;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author VTPlusAKnauer
 */
public class DebugPicturesProcessor implements SceneProcessor {

	private ViewPort viewPort;
	private RenderManager renderManager;

	@Getter private final DebugPictureList debugPictures;
	@Getter @Setter private boolean enabled = true;

	public DebugPicturesProcessor(AssetManager assetManager, Node guiNode, BitmapFont font) {
		debugPictures = new DebugPictureList(assetManager, guiNode, font);
	}

	@Override
	public void initialize(RenderManager renderManager, ViewPort viewPort) {
		this.renderManager = renderManager;
		this.viewPort = viewPort;
	}

	@Override
	public void reshape(ViewPort vp, int w, int h) {
	}

	@Override
	public boolean isInitialized() {
		return viewPort != null;
	}

	@Override
	public void preFrame(float tpf) {		
		debugPictures.updateLogicalState(tpf);
	}

	@Override
	public void postQueue(RenderQueue rq) {
	}

	@Override
	public void postFrame(FrameBuffer out) {
		if (enabled) {
			debugPictures.display(viewPort, renderManager);
		}
	}

	@Override
	public void cleanup() {
	}

}
