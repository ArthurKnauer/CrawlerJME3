package crawler.effects;

import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;

/**
 *
 * @author VTPlusAKnauer
 */
public class MuzzleFlashControl extends AbstractControl {

	private final ColorRGBA currentColor; // decays over time
	private final ColorRGBA color;
	private final ColorRGBA colorVaried; // = color with random offset

	private float lifeTime = 0;
	private final float decaySpeed = 20f;
	private boolean visible;
	private final Vector3f rotationAxis;

	MuzzleFlashControl(ColorRGBA muzzeFlashMaterialColor, ColorRGBA color) {
		this.color = color;
		currentColor = muzzeFlashMaterialColor;
		currentColor.set(0, 0, 0, 0);
		colorVaried = new ColorRGBA(1, 1, 1, 1);
		rotationAxis = new Vector3f(0, 0, 1);

	}

	public void flash() {
		lifeTime = 1;

		colorRandom();
		rotateRandom();
		scaleRandom();
		show();
	}

	@Override
	public void setSpatial(Spatial spatial) {
		super.setSpatial(spatial);
		hide();
	}

	private void colorRandom() {
		float randomR = FastMath.rand.nextFloat() * 0.2f + 0.8f * color.r;
		float randomG = FastMath.rand.nextFloat() * 0.2f + 0.8f * color.g;
		float randomB = FastMath.rand.nextFloat() * 0.2f + 0.8f * color.b;
		float randomA = FastMath.rand.nextFloat() * 0.2f + 0.8f * color.a;

		colorVaried.set(randomR, randomG, randomB, randomA);
		currentColor.set(colorVaried);
	}

	private void scaleRandom() {
		float randomX = FastMath.rand.nextFloat() * 0.6f + 0.7f;
		float randomY = FastMath.rand.nextFloat() * 0.6f + 0.7f;
		float randomZ = FastMath.rand.nextFloat() * 0.6f + 0.7f;
		spatial.getLocalScale().set(randomX, randomY, randomZ);
	}

	private void rotateRandom() {
		float randomAngle = FastMath.rand.nextFloat() * FastMath.TWO_PI;
		spatial.getLocalRotation().fromAngleNormalAxis(randomAngle, rotationAxis);
	}

	@Override
	protected void controlUpdate(float tpf) {
		if (visible) {
			decayStep(tpf);
		}
	}

	@Override
	protected void controlRender(RenderManager rm, ViewPort vp) {
	}

	private void decayStep(float tpf) {
		currentColor.set(colorVaried.r * lifeTime,
						 colorVaried.g * lifeTime,
						 colorVaried.b * lifeTime,
						 colorVaried.a * lifeTime);

		if (lifeTime > 0) {
			lifeTime -= tpf * decaySpeed;
		}
		else {
			hide();
		}
	}

	private void hide() {
		visible = false;
		spatial.setCullHint(Spatial.CullHint.Always);
	}

	private void show() {
		visible = true;
		spatial.setCullHint(Spatial.CullHint.Inherit);
	}
}
