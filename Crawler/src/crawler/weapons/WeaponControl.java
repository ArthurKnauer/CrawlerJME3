package crawler.weapons;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;
import crawler.audio.AudioNodeEx;
import crawler.effects.MuzzleFlashControl;
import crawler.weapons.projectiles.ProjectileBuilder;

/**
 *
 * @author VTPlusAKnauer
 */
public class WeaponControl extends AbstractControl {

	private final WeaponType type;
	private final MuzzleFlashControl muzzleFlashControl;
	private final AudioNodeEx fireSound;
	
	private final float delayBetweenShots = 0.1f;
	private float passedTimeSinceLastShot = delayBetweenShots;

	public WeaponControl(WeaponType type, MuzzleFlashControl muzzleFlashControl, AudioNodeEx fireSound) {
		this.muzzleFlashControl = muzzleFlashControl;
		this.fireSound = fireSound;
		this.type = type;
	}
	
	
	@Override
	protected void controlUpdate(float tpf) {
		passedTimeSinceLastShot += tpf;
	}

	@Override
	protected void controlRender(RenderManager rm, ViewPort vp) {
	}
	

	public void lookAt(Vector3f position, Vector3f upVector) {
		spatial.lookAt(position, upVector);
	}

	public boolean fire() {
		if (passedTimeSinceLastShot < delayBetweenShots)
			return false;
		
		muzzleFlashControl.flash();
		
		float speed = 715;
		Vector3f location = muzzleFlashControl.getSpatial().getWorldTranslation();
		Quaternion rotation = spatial.getWorldRotation();
		Vector3f velocity = rotation.mult(Vector3f.UNIT_Z).multLocal(speed);
		ProjectileBuilder.build(location, rotation, velocity);		
		
		fireSound.stop();
		fireSound.play();
		
		passedTimeSinceLastShot = 0;
		return true;
	}
}
