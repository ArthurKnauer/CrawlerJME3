package common.render.lpv.debug;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;
import common.render.lpv.LPVFrameBuffer;
import common.render.lpv.LPVShape;

/**
 *
 * @author VTPlusAKnauer
 */
public class LPVProbe extends Node {

	private enum RenderState {

		None, ProbeLight, ProbeGeometry
	}

	private RenderState renderState = RenderState.None;
	private final Geometry sampleBall; // used to sample geometry or lpv volume 
	private final Material shader;

	public LPVProbe(AssetManager assetManager) {
		sampleBall = new Geometry("sampleBall", new Sphere(32, 32, 0.25f));
		sampleBall.setCullHint(Spatial.CullHint.Always);

		shader = new Material(assetManager, "MatDefs/GVProbed/GVProbed.j3md");
		sampleBall.setMaterial(shader);

		attachChild(sampleBall);
		
		setQueueBucket(RenderQueue.Bucket.Transparent);

		setLocalTranslation(new Vector3f(0, 0, 2));
	}

	public void toggle() {
		int nextStateOrdinal = (renderState.ordinal() + 1) % RenderState.values().length;
		renderState = RenderState.values()[nextStateOrdinal];

		shader.setBoolean("ProbeLPV", renderState == RenderState.ProbeLight);

		sampleBall.setCullHint(renderState == RenderState.None ? Spatial.CullHint.Always : Spatial.CullHint.Never);
	}

	public void setLPV(LPVFrameBuffer lpvFrameBuffer) {
		shader.setTexture("GV", lpvFrameBuffer.getGvMap());
		
//		shader.setTexture("LPVRed", lpvFrameBuffer);
//		shader.setTexture("LPVGreen", lpvFrameBuffer.getGvMap());
//		shader.setTexture("LPVBlue", lpvFrameBuffer.getGvMap());
	}

	public void setLPVSetup(LPVShape lpvSetup) {
		shader.setVector3("GVMinPos", lpvSetup.getGvMinPos());
		shader.setVector3("LPVMinPos", lpvSetup.getMinPos());
		shader.setVector3("GVScale", lpvSetup.getScale());
	}

	public void setFluxScale(float fluxScale) {
		shader.setFloat("LPVFluxScale", fluxScale);
	}

	public void updatePosition(Camera viewCam) {

	}
}
