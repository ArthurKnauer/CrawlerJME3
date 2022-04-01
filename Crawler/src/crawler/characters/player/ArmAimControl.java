/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package crawler.characters.player;

import com.jme3.animation.Bone;
import com.jme3.animation.Skeleton;
import com.jme3.animation.SkeletonControl;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import java.io.IOException;

/**
 *
 * @author VTPlusAKnauer
 */
public class ArmAimControl implements Control {

	private Spatial spatial;
	private Skeleton skeleton;
	private SkeletonControl skeletonControl;

	Bone leftArm, rightArm, neck, head, eyes, upperChest, lowerChest;
	float angle;
	boolean aiming = false;

	@Override
	public Control cloneForSpatial(Spatial spatial) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void setSpatial(Spatial spatial) {
		this.spatial = spatial;
		skeletonControl = spatial.getControl(SkeletonControl.class);
		skeleton = skeletonControl.getSkeleton();

		leftArm = skeleton.getBone("Upperarm.L");
		rightArm = skeleton.getBone("Upperarm.R");
		neck = skeleton.getBone("Neck");
		head = skeleton.getBone("Head");
		eyes = skeleton.getBone("Eyes");
		upperChest = skeleton.getBone("Upperchest");
		lowerChest = skeleton.getBone("Lowerchest");
	}

	public void setAngle(float angle, boolean aiming) {
		this.angle = angle;
		this.aiming = aiming;
	}

	@Override
	public void update(float tpf) {
		if (aiming) {
			Quaternion rot = new Quaternion();
			rot.fromAngleAxis(angle, new Vector3f(1, 0, 0).normalizeLocal());
			leftArm.additionalRot.set(rot);
			rightArm.additionalRot.set(rot);
		}
		else {
			leftArm.additionalRot.set(Quaternion.IDENTITY);
			rightArm.additionalRot.set(Quaternion.IDENTITY);
		}

		Quaternion rot = new Quaternion();
		rot.fromAngleAxis(FastMath.HALF_PI, new Vector3f(1, 0, 0).normalizeLocal());
		eyes.additionalRot.set(rot);

		float angleThird = angle / 3.0f;
		rot.fromAngleAxis(angleThird, new Vector3f(1, 0, 0).normalizeLocal());
		head.additionalRot.set(rot);
		neck.additionalRot.set(rot);

		rot.fromAngleAxis(angleThird * 0.5f, new Vector3f(1, 0, 0).normalizeLocal());
		upperChest.additionalRot.set(rot);
		lowerChest.additionalRot.set(rot);
		eyes.additionalRot.multLocal(rot);
	}

	@Override
	public void render(RenderManager rm, ViewPort vp) {
		//	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void write(JmeExporter ex) throws IOException {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void read(JmeImporter im) throws IOException {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

}
