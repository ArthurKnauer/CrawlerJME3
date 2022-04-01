/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common.render;

import com.jme3.post.SceneProcessor;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.Renderer;
import com.jme3.renderer.ViewPort;

/**
 *
 * @author VTPlusAKnauer
 */
public abstract class SimpleProcessor implements SceneProcessor {
	
	protected RenderManager renderManager;
	protected Renderer renderer;
	protected ViewPort viewPort;

	@Override
	public void initialize(RenderManager renderManager, ViewPort viewPort) {
		this.renderManager = renderManager;
		this.renderer = renderManager.getRenderer();
		this.viewPort = viewPort;
	}

	@Override
	public final boolean isInitialized() {
		return (renderManager != null) && (renderer != null) && (viewPort != null);
	}	
}
