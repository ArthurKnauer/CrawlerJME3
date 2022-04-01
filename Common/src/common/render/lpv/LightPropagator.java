/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common.render.lpv;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;

/**
 *
 * @author VTPlusAKnauer
 */
class LightPropagator implements LPVRenderTask {

	private final Material shader;
	private int currentStep;

	private static final int LIGHT_PROPAGATION_STEPS_DEFAULT = 32;
	private int totalSteps = LIGHT_PROPAGATION_STEPS_DEFAULT;

	LightPropagator(AssetManager assetManager, LPVFrameBuffer lpvFrameBuffer) {
		shader = new Material(assetManager, "MatDefs/LPV/Propagate.j3md");
		shader.setTexture("GV", lpvFrameBuffer.getGvMap());
	}
	
	public void setSteps(int totalSteps) {
		this.totalSteps = totalSteps;
	}

	public void reset() {
		this.currentStep = 0;
	}

	public void setFluxScale(float fluxScale) {
		shader.setFloat("FluxScale", fluxScale);
	}

	@Override
	public void setLPVShape(LPVShape lpvShape) {
		shader.setFloat("LPVTextureDepth", lpvShape.getTextureSizeZ());
		shader.setVector3("LPVCellSize", lpvShape.getCellSize());
	}

	@Override
	public Result doIt(LPVRenderKit kit) {
		kit.setLPVFilterNearest();
		kit.setForcedTechnique(null);

		if (currentStep == 0)
			shader.setBoolean("CheckOcclusion", false); // dont occlude on first step, avoid self occlusion
		else
			shader.setBoolean("CheckOcclusion", true);

		if (currentStep % 2 == 0) {
			kit.setFrameBufferSecondLPV();
			kit.setShaderTexturesFirstLPV(shader);
		}
		else {
			kit.setFrameBufferFirstLPV();
			kit.setShaderTexturesSecondLPV(shader);
		}
		kit.renderVolumeFill(shader);

		kit.setLPVFilterBilinear();
		currentStep++;

		if (currentStep < totalSteps)
			return Result.NEEDS_MORE_CALLS;
		else
			return Result.DONE;
	}
}
