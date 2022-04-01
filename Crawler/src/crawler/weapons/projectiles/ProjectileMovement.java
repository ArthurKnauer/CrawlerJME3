package crawler.weapons.projectiles;

import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import crawler.main.Globals;
import crawler.physics.Trace;
import crawler.physics.impacts.Impact;
import crawler.physics.impacts.Impacts;

/**
 *
 * @author VTPlusAKnauer
 */
public class ProjectileMovement extends AbstractControl {

	private final Vector3f velocity;
	private Vector3f nextLocation;

	public ProjectileMovement(Vector3f velocity) {
		this.velocity = velocity;
	}

	@Override
	public void setSpatial(Spatial spatial) {
		super.setSpatial(spatial);
		if (spatial != null) {
			if (spatial.getParent() != Globals.getRootNode()) {
				throw new IllegalArgumentException("expecting rootNode to be parent of projectile");
			}

			nextLocation = new Vector3f(spatial.getLocalTranslation());
		}
	}

	@Override
	protected void controlUpdate(float tpf) {
		if (spatial != null) {
			nextLocation.scaleAdd(tpf, velocity, nextLocation);	

			Trace.traceRay(spatial.getLocalTranslation(), nextLocation).ifPresent(hit -> {
				Vector3f hitPos = hit.getHitLocation();
				Impact impact = Impact.builder().location(hitPos).impulse(velocity.mult(0.001f)).build();
				Impacts.apply(hit.getCollisionObject(), impact);
				//Globals.getLpvProcessor().decalList.add(new Decal(hitPos, hit.getHitNormalLocal(), 1.1f));

				die();
			});
			
			spatial.setLocalTranslation(nextLocation);
		}
	}

	@Override
	protected void controlRender(RenderManager rm, ViewPort vp) {
	}

	private void die() {
		spatial.removeFromParent();
	}
}
