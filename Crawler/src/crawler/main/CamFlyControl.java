/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package crawler.main;

import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;

/**
 *
 * @author VTPlusAKnauer
 */
class CamFlyControl extends AbstractControl implements ActionListener, AnalogListener {

	private boolean goForward, goRight, goLeft, goBackwards, superSpeed;

	CamFlyControl() {

	}

	@Override
	protected void controlUpdate(float tpf) {
		Quaternion camRot = spatial.getLocalRotation();
		Vector3f camForward = new Vector3f(0, 0, 1);
		Vector3f camRight = new Vector3f(-1, 0, 0);
		camRot.multLocal(camForward);
		camRot.multLocal(camRight);
		Vector3f camLocation = spatial.getLocalTranslation();

		final float camMoveSpeed = superSpeed ? 50.0f : 10.0f;

		if (goForward)
			camLocation.addLocal(camForward.mult(camMoveSpeed * tpf));
		if (goRight)
			camLocation.addLocal(camRight.mult(camMoveSpeed * tpf));
		if (goLeft)
			camLocation.addLocal(camRight.mult(-camMoveSpeed * tpf));
		if (goBackwards)
			camLocation.addLocal(camForward.mult(-camMoveSpeed * tpf));

		if (camLocation.y < -149)
			camLocation.y = -149;

		spatial.setLocalTranslation(camLocation);
	}

	@Override
	protected void controlRender(RenderManager rm, ViewPort vp) {
	}

	@Override
	public void onAction(String name, boolean isPressed, float tpf) {
		if (!isEnabled())
			return;

		switch (name) {
			case "forward": goForward = isPressed;
				break;
			case "right": goRight = isPressed;
				break;
			case "left": goLeft = isPressed;
				break;
			case "backward": goBackwards = isPressed;
				break;
			case "run": superSpeed = isPressed;
				break;
		}
	}

	@Override
	public void onAnalog(String name, float value, float tpf) {
		if (!isEnabled())
			return;
		switch (name) {
			case "right_turn":
				turn(value, 0);
				break;
			case "left_turn":
				turn(-value, 0);
				break;
			case "look_up":
				turn(0, value);
				break;
			case "look_down":
				turn(0, -value);
				break;
		}
	}

	private void turn(float dx, float dy) {
		float xAngle = dx;
		float yAngle = dy;

		Quaternion camRot = spatial.getLocalRotation();
		Vector3f camForward = new Vector3f(0, 0, 1);
		Vector3f camRight = new Vector3f(1, 0, 0);
		camRot.multLocal(camForward);
		camRot.multLocal(camRight);

		Quaternion pitch = new Quaternion(), yaw = new Quaternion();
		pitch.fromAngleNormalAxis(yAngle, camRight);
		yaw.fromAngleNormalAxis(xAngle, Vector3f.UNIT_Y);

		yaw.multLocal(pitch).multLocal(camRot);

		spatial.setLocalRotation(yaw);
	}

}
