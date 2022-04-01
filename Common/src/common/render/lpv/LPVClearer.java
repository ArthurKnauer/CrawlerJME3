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
public class LPVClearer implements LPVRenderTask {

	private final Material clearShader;

	public LPVClearer(AssetManager assetManager) {
		clearShader = new Material(assetManager, "MatDefs/LPV/Clear.j3md");
	}

	@Override
	public void setLPVShape(LPVShape lpvShape) {
	}

	@Override
	public Result doIt(LPVRenderKit kit) {
		kit.setForcedTechnique(null);
		
		kit.setFrameBufferGV();	
		kit.renderVolumeFill(clearShader);
		
		kit.setFrameBufferFirstLPV();
		kit.renderVolumeFill(clearShader);
		
		kit.setFrameBufferSecondLPV();
		kit.renderVolumeFill(clearShader);
		
		return Result.DONE;
	}

}
