package crawler.characters.player;

import com.jme3.animation.AnimControl;
import com.jme3.bullet.collision.PhysicsRayTestResult;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.CameraControl;
import crawler.attributes.UsableAttribute;
import crawler.characters.BipedControl;
import crawler.characters.BipedProperties;
import crawler.characters.CustomCharacterControl;
import crawler.main.Globals;
import crawler.weapons.WeaponControl;
import java.util.List;

/**
 *
 * @author VTPlusAKnauer
 */
public class PlayerBipedControl extends BipedControl {

	private final Node eyeNode;
	private final Node weaponNode;

	protected final float maxYAngle = 1.7f;
	protected final float minYAngle = -1.7f;
	protected float yAngle = FastMath.PI;

	public PlayerBipedControl(CustomCharacterControl charControl,
							  AnimControl animControl,
							  BipedProperties properties,
							  Node eyeNode,
							  Node weaponNode) {
		super(charControl, animControl, properties);

		animControl.setEnabled(false);

		this.eyeNode = eyeNode;
		this.weaponNode = weaponNode;
	}

	@Override
	public void reset() {
		super.reset();
		yAngle = 0;
	}

	public void turn(float dx, float dy) {
		xAngle += dx;
		yAngle += dy;

		limitXAngleTo2PiRange();
		limitYAngleToMaxMin();
	}

	private void limitYAngleToMaxMin() {
		if (yAngle > maxYAngle)
			yAngle = maxYAngle;
		else if (yAngle < minYAngle)
			yAngle = minYAngle;
	}

	@Override
	protected void rotateView() {

		super.rotateView();

		Quaternion verticalRot = new Quaternion();
		verticalRot.fromAngleNormalAxis(yAngle, new Vector3f(1, 0, 0));
		eyeNode.setLocalRotation(verticalRot);
	}

	public void fire() {
		weaponNode.getChild(0).getControl(WeaponControl.class).fire();
	}

	public void aim() {

	}

	public void use() {

		Vector3f aimLineStart = eyeNode.getWorldTranslation();
		Vector3f aimLineEnd = eyeNode.getWorldRotation().mult(Vector3f.UNIT_Z).scaleAdd(3, aimLineStart);

		List<PhysicsRayTestResult> hitResults = Globals.getPhysicsSpace().rayTest(aimLineStart, aimLineEnd);

		if (hitResults.size() > 0) {
			// find hit closest to the ray start (first hit)
			float closestHitFraction = 1.0f;
			PhysicsRayTestResult closestHit = null;
			for (PhysicsRayTestResult hit : hitResults) {
				if (hit.getHitFraction() < closestHitFraction) {
					closestHit = hit;
					closestHitFraction = hit.getHitFraction();
				}
			}

			if (closestHit != null) {
				Object hitObject = closestHit.getCollisionObject().getUserObject();

				if (hitObject != null && hitObject instanceof Spatial) {
					Spatial hitSpatial = (Spatial) hitObject;
					UsableAttribute usable = hitSpatial.getAttribute(UsableAttribute.class);
					if (usable != null) {
						usable.use(spatial);
					}
				}
//				
//				PhysicsCollisionObject cobj;
//				cobj = closestHit.getCollisionObject();
//				if (cobj instanceof RigidBodyControl) {
//					RigidBodyControl rbc = (RigidBodyControl) cobj;
//					Vector3f rayDir = aimLineEnd.subtract(aimLineStart);
//					Vector3f hitPos = rayDir.mult(closestHit.getHitFraction()).add(aimLineStart);
//					DebugGeometry.addWireBox(hitPos, 0.02f, ColorRGBA.Blue);
//					//addHitPositionToHistory(hitPos);
//
//					Vector3f hitNormal = closestHit.getHitNormalLocal().normalize();
//					if (rbc.getMass() > 0) { // dynamic object -> apply impulse					
//						rbc.setCcdMotionThreshold(0.1f);
//						rbc.setCcdSweptSphereRadius(30);
//						rbc.applyImpulse(rayDir.normalizeLocal().multLocal(15), hitPos.subtract(rbc.getPhysicsLocation()));
//					}
//					else { // solid object -> add decal, debris and smoke
//						//		decalList.add(new Decal(hitPos, hitNormal, (float) Math.random() * 0.01f + 0.02f));
//
////							debris.setLocalTranslation(hitPos);
////							debris.getParticleInfluencer().setInitialVelocity(hitNormal.mult(4));
////							debris.setNumParticles(2 + (int) (Math.random() * 2));
////							debris.emitAllParticles();
////
////							smoke.setLocalTranslation(hitPos);
////							smoke.getParticleInfluencer().setInitialVelocity(hitNormal.mult(0.5f));
////							smoke.setNumParticles(1);
////							smoke.emitAllParticles();
//					}
//
//					// check if rigid body belongs to a biped -> add damage
//					Object object = rbc.getUserObject();
//					System.out.println("hit user object " + object);
////					if (object != null && object instanceof Biped) {
////						Biped biped = (Biped) object;
////						biped.takeDamage(hitPos, rayDir.normalizeLocal());
////					}
//				}
				}
			}
		}

	

	public void previousWeapon() {

	}

	public void nextWeapon() {

	}

	public void enableEyeCameraControl(boolean enable) {
		eyeNode.getControl(CameraControl.class).setEnabled(enable);
	}
}
