/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common.render.lpv;


/**
 *
 * @author VTPlusAKnauer
 */
public class RSMRenderer implements LPVRenderTask {

	private final ReflectiveShadowMap rsm;

	public RSMRenderer(ReflectiveShadowMap rsm) {
		this.rsm = rsm;
	}

	@Override
	public void setLPVShape(LPVShape lpvShape) {
	}

	@Override
	public Result doIt(LPVRenderKit kit) {		
		kit.setCamera(rsm.getCamera(), false);
		kit.setForcedTechnique("RSM");

		kit.setFrameBuffer(rsm.getFrameBuffer());
		kit.clearBuffers(true, true, false);

		kit.renderShadowQueue();
		
		return Result.DONE;
	}
}
