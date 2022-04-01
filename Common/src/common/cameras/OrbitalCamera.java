package common.cameras;

import com.jme3.bounding.BoundingBox;
import com.jme3.bounding.BoundingSphere;
import com.jme3.input.ChaseCamera;
import com.jme3.input.InputManager;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.FastMath;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;

/**
 *
 * @author VTPlusAKnauer
 */
public final class OrbitalCamera extends ChaseCamera {

	protected final static String ChaseCamTogglePan = "ChaseCamTogglePan";
	private boolean canPan;
	private final Node orbitTarget = new Node();
	private final float panSpeed = 2;
	private long panToggleButtonLastClickTimeMS = 0;
	private final long panToggleMaxDoubleClickTimeMS = 500;

	public OrbitalCamera(Camera cam, Node target, InputManager inputManager, AppSettings settings) {
		super(cam, target, inputManager);

		target.attachChild(orbitTarget);
		this.target.removeControl(this);
		this.target = orbitTarget;
		this.target.addControl(this);

		setRotationSpeed(10);
		setMinVerticalRotation(-FastMath.PI / 2);
		setInvertVerticalAxis(true);
		setSmoothMotion(false);
		setDefaultDistance(5);
		setMinDistance(0.1f);
		cam.setFrustumPerspective(60, (float) settings.getWidth() / settings.getHeight(), 0.01f, 50);

		inputManager.addMapping(ChaseCamTogglePan, new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
		inputManager.addListener(this, ChaseCamTogglePan);
	}

	public void frame(Spatial model) {
		float maxSideLength = 1;
		if (model.getWorldBound() instanceof BoundingBox) {
			BoundingBox bbox = (BoundingBox) model.getWorldBound();
			maxSideLength = Math.max(bbox.getXExtent(), Math.max(bbox.getYExtent(), bbox.getZExtent()));
			orbitTarget.setLocalTranslation(0, bbox.getYExtent(), 0);
		}
		else if (model.getWorldBound() instanceof BoundingSphere) {
			maxSideLength = ((BoundingSphere) model.getWorldBound()).getRadius();
			orbitTarget.setLocalTranslation(0, maxSideLength, 0);
		}

		setDefaultDistance(maxSideLength * 3);		
	}

	@Override
	protected void zoomCamera(float value) {
		zoomSpeed = targetDistance * 0.1f;
		super.zoomCamera(value); //To change body of generated methods, choose Tools | Templates.
	}

	private void checkPanDoubleClick() {
		long currentTime = System.currentTimeMillis();
		if (System.currentTimeMillis() - panToggleButtonLastClickTimeMS < panToggleMaxDoubleClickTimeMS)
			resetPan();
		panToggleButtonLastClickTimeMS = currentTime;
	}

	private void resetPan() {
		orbitTarget.setLocalTranslation(0, 0, 0);
	}

	@Override
	public void onAction(String name, boolean keyPressed, float tpf) {
		super.onAction(name, keyPressed, tpf);

		if (name.equals(ChaseCamTogglePan) && enabled) {
			if (keyPressed) {
				checkPanDoubleClick();
				canPan = true;
				canRotate = false;
				if (hideCursorOnRotate) {
					inputManager.setCursorVisible(false);
				}
			}
			else {

				canPan = false;
				if (hideCursorOnRotate) {
					inputManager.setCursorVisible(true);
				}
			}
		}
	}

	@Override
	public void onAnalog(String name, float value, float tpf) {
		super.onAnalog(name, value, tpf);

		if (name.equals(ChaseCamUp)) {
			panCamera(value);
		}
		else if (name.equals(ChaseCamDown)) {
			panCamera(-value);
		}
	}

	private void panCamera(float value) {
		if (canPan && enabled) {
			orbitTarget.move(0, value * panSpeed * distance, 0);
			panToggleButtonLastClickTimeMS = 0;
		}
	}
}
