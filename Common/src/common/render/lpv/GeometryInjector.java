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
public class GeometryInjector implements LPVRenderTask {

	private final Material gvInjectShader;
	private final ArrayList<Geometry> staticOccluders = new ArrayList<>();

	public GeometryInjector(AssetManager assetManager) {
		gvInjectShader = new Material(assetManager, "MatDefs/LPV/GeometryInject.j3md");
	}

	public void addOccluder(Geometry occluder) {
		staticOccluders.add(occluder);
	}
	
	public void setBlockingScale(float blockingScale) {
		gvInjectShader.setFloat("SHScale", blockingScale);		
	}

	@Override
	public void setLPVShape(LPVShape lpvShape) {
		gvInjectShader.setVector3("GVMinPos", lpvShape.getGvMinPos());
		gvInjectShader.setVector3("GVScale", lpvShape.getScale());
		gvInjectShader.setVector3("GVCellSize", lpvShape.getCellSize());
		gvInjectShader.setFloat("GVTextureDepthHalved", lpvShape.getTextureSizeZ() * 0.5f);
	}

	@Override
	public Result doIt(LPVRenderKit kit) {
		GL11.glPointSize(1); //tessellator produces point clouds	
		
		kit.setFrameBufferGV();
		kit.setForcedTechnique(null);
		
		kit.setForcedMeshMode(Mesh.Mode.Patches);

		for (Geometry occluder : staticOccluders) {
			gvInjectShader.setMatrix4("WorldMatrix", occluder.getWorldMatrix());
			kit.render(gvInjectShader, occluder);
		}

		kit.setForcedMeshMode(null);
		return Result.DONE;
	}
}
