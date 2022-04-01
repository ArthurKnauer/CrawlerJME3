/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common.render.lpv;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import org.lwjgl.opengl.GL11;

/**
 *
 * @author VTPlusAKnauer
 */
public class RSMInjector implements LPVRenderTask {

	private final RSMInjectPoints injectPoints;
	private final Material shader;

	public RSMInjector(AssetManager assetManager, ReflectiveShadowMap rsm) {
		injectPoints = new RSMInjectPoints(rsm);

		shader = new Material(assetManager, "MatDefs/LPV/InjectRSM.j3md");

		shader.setTexture("PositionMap", rsm.getPositionMap());
		shader.setTexture("NormalMap", rsm.getNormalMap());
		shader.setTexture("ColorMap", rsm.getColorMap());

		float areaFactor = (rsm.getWidth() * rsm.getHeight()) / (128.0f * 128.0f);
		shader.setFloat("RSMArea", areaFactor);
	}

	public void setFluxScale(float fluxScale) {
		shader.setFloat("FluxScale", fluxScale);
	}

	public void setUseCosLobe(boolean useCosLobe) {
		shader.setBoolean("UseCosLobe", useCosLobe);
	}

	@Override
	public void setLPVShape(LPVShape lpvShape) {
		shader.setVector3("LPVMinPos", lpvShape.getMinPos());
		shader.setVector3("LPVScale", lpvShape.getScale());
		shader.setVector3("LPVCellSize", lpvShape.getCellSize());
		shader.setFloat("LPVTextureDepthHalved", lpvShape.getTextureSizeZ() * 0.5f);
	}

	@Override
	public Result doIt(LPVRenderKit kit) {
		GL11.glPointSize(1); // rsm geometry is a mesh of points
		
		kit.setFrameBufferFirstLPV();
		kit.render(shader, injectPoints);	
		
		return Result.DONE;
	}
}
