package crawler.physics.impacts;

import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;

/**
 *
 * @author VTPlusAKnauer
 */
public class Impacts {

	public static void apply(PhysicsCollisionObject pco, Impact impact) {
		if (pco instanceof RigidBodyControl)
			apply((RigidBodyControl) pco, impact);
	}

	public static void apply(RigidBodyControl rbc, Impact impact) {
		//addHitPositionToHistory(hitPos);
		//Vector3f hitNormal = closestHit.getHitNormalLocal().normalize();
		if (rbc.getMass() > 0) { // dynamic object -> apply impulse					
			rbc.setCcdMotionThreshold(0.1f);
			rbc.setCcdSweptSphereRadius(30);
			Vector3f relativeLocation = impact.getLocation().subtract(rbc.getPhysicsLocation());
			rbc.applyImpulse(impact.getImpulse(), relativeLocation);
		}
		else { // solid object -> add decal, debris and smoke
			//		decalList.add(new Decal(hitPos, hitNormal, (float) Math.random() * 0.01f + 0.02f));

//							debris.setLocalTranslation(hitPos);
//							debris.getParticleInfluencer().setInitialVelocity(hitNormal.mult(4));
//							debris.setNumParticles(2 + (int) (Math.random() * 2));
//							debris.emitAllParticles();
//
//							smoke.setLocalTranslation(hitPos);
//							smoke.getParticleInfluencer().setInitialVelocity(hitNormal.mult(0.5f));
//							smoke.setNumParticles(1);
//							smoke.emitAllParticles();
		}

		// check if rigid body belongs to a biped -> add damage
		Object object = rbc.getUserObject();
		System.out.println("hit user object " + object);
	}
}
