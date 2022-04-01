/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package crawler.render.city;

import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.texture.FrameBuffer;
import common.render.SimpleProcessor;

/**
 *
 * @author VTPlusAKnauer
 */
public class CityRenderer extends SimpleProcessor {

	private final FrameBuffer hdrResultFrameBuffer;

	public CityRenderer(FrameBuffer hdrResultFrameBuffer) {
		this.hdrResultFrameBuffer = hdrResultFrameBuffer;
	}

	@Override
	public void reshape(ViewPort vp, int w, int h) {
	}

	@Override
	public void preFrame(float tpf) {
	}

	@Override
	public void postQueue(RenderQueue rq) {
		renderManager.getRenderer().setFrameBuffer(hdrResultFrameBuffer);
		renderManager.setCamera(viewPort.getCamera(), false);

		viewPort.getQueue().renderQueue(RenderQueue.Bucket.Sky3D, renderManager, viewPort.getCamera(), true);
		viewPort.getQueue().renderQueue(RenderQueue.Bucket.Transparent, renderManager, viewPort.getCamera(), true);

		renderManager.getRenderer().setFrameBuffer(null);
	}

	@Override
	public void postFrame(FrameBuffer out) {
	}

	@Override
	public void cleanup() {
	}
}
