package crawler.characters;

import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.control.AbstractControl;
import crawler.characters.ragdoll.RagdollControl;

/**
 *
 * @author VTPlusAKnauer
 */


public class HealthControl extends AbstractControl {
	
	private float health = 100;
	
	public void takeDamage(Vector3f hitLocation, Vector3f hitNormal) {
		float oldHealth = health;
		health -= 35f;

//		blood.setLocalTranslation(hitLocation);
//		blood.getParticleInfluencer().setInitialVelocity(hitNormal.mult(-2));
//		blood.setNumParticles(5 + (int) (Math.random() * 5));
//		blood.emitAllParticles();
		if (oldHealth > 0 && health <= 0) {
			RagdollControl ragdollControl = ((Node)spatial).getChild("model").getControl(RagdollControl.class);
			ragdollControl.setEnabled(true);

//			deathSound.stop();
//			deathSound.setPitch((float) (Math.random() * 0.45f + 0.85f));
//			deathSound.play();
//			System.out.println(deathSound.getWorldTranslation());

			RigidBodyControl boneControl = ragdollControl.getFirstRigidBodyControl();
			Vector3f randomVector = new Vector3f(FastMath.nextRandomFloat() - 0.5f,
												 FastMath.nextRandomFloat(),
												 FastMath.nextRandomFloat() - 0.5f).normalizeLocal().multLocal(200);
			boneControl.applyImpulse(randomVector, Vector3f.ZERO);

		//	goDir = GoDir.STAND;
			//ragdollControl.getBoneRigidBodyControl("Pelvis").applyCentralForce(hitNormal.mult(-20));
			//ragdollControl.getBoneRigidBodyControl("Pelvis").applyImpulse(hitNormal.mult(-20), Vector3f.ZERO);	
			CustomCharacterControl charControl = spatial.getControl(CustomCharacterControl.class);
			charControl.setEnabled(false);
		}
	}
	
	public void kill() {
		
	}
	
	public void revive() {
		
	}

	@Override
	protected void controlUpdate(float tpf) {
	
	}

	@Override
	protected void controlRender(RenderManager rm, ViewPort vp) {
	
	}
	
}
