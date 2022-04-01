/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common.render.lpv;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import java.util.ArrayList;
import org.lwjgl.opengl.GL11;

/**
 *
 * @author VTPlusAKnauer
 */
class LightPointInjector implements LPVRenderTask {

	private final ArrayList<Geometry> lightPointMeshes = new ArrayList<>(); // all virtual light points (windows, etc..)
	private final Material shader;

	LightPointInjector(AssetManager assetManager) {
		shader = new Material(assetManager, "MatDefs/LPV/InjectPoints.j3md");
	}

	void addPoints(Mesh lightPoints) {
		lightPointMeshes.add(new Geometry("lightPoints", lightPoints));
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
		shader.setFloat("LPVTextureDepthHalved", lpvShape.getTextureSizeZ() * 0.5f);
		shader.setVector3("LPVCellSize", lpvShape.getCellSize());
	}

	@Override
	public Result doIt(LPVRenderKit kit) {
		GL11.glPointSize(1);
		kit.setFrameBufferFirstLPV();

		for (Geometry windowVPLs : lightPointMeshes) {
			shader.setMatrix4("WorldMatrix", windowVPLs.getWorldMatrix());
			kit.render(shader, windowVPLs);
		}
		
		return Result.DONE;
	}

}
