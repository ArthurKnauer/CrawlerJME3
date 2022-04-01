package crawler.characters.player;

import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;
import crawler.characters.MoveDirection;

/**
 *
 * @author VTPlusAKnauer
 */
public class PlayerInputControl extends AbstractControl implements ActionListener, AnalogListener {

	private final float mouseSensitivity = 0.75f;

	boolean moveForward, moveRight, moveLeft, moveBack;
	boolean fire, aim, use;

	private final PlayerBipedControl playerBiped;

	public PlayerInputControl(PlayerBipedControl biped) {
		this.playerBiped = biped;
	}

	@Override
	protected void controlUpdate(float tpf) {
		MoveDirection moveDirection = MoveDirection.STOP;

		if (moveForward)
			moveDirection = moveDirection.add(MoveDirection.FORWARD);
		if (moveRight)
			moveDirection = moveDirection.add(MoveDirection.RIGHT);
		if (moveLeft)
			moveDirection = moveDirection.add(MoveDirection.LEFT);
		if (moveBack)
			moveDirection = moveDirection.add(MoveDirection.BACK);

		playerBiped.move(moveDirection);

		if (fire)
			playerBiped.fire();
		if (aim)
			playerBiped.aim();
		if (use)
			playerBiped.use();

		//	System.out.println(biped.closestGoDir(Vector3f.UNIT_Z));
	}

	@Override
	protected void controlRender(RenderManager rm, ViewPort vp) {
	}

	@Override
	public void onAnalog(String name, float value, float tpf) {
		if (!isEnabled())
			return;

		switch (name) {
			case "right_turn":
				playerBiped.turn(value * mouseSensitivity, 0);
				break;
			case "left_turn":
				playerBiped.turn(-value * mouseSensitivity, 0);
				break;
			case "look_up":
				playerBiped.turn(0, value * mouseSensitivity);
				break;
			case "look_down":
				playerBiped.turn(0, -value * mouseSensitivity);
				break;
		}
	}

	@Override
	public void onAction(String name, boolean keyPressed, float tpf) {
		if (!isEnabled())
			return;

		switch (name) {
			case "forward":
				moveForward = keyPressed;
				break;
			case "right":
				moveRight = keyPressed;
				break;
			case "left":
				moveLeft = keyPressed;
				break;
			case "backward":
				moveBack = keyPressed;
				break;
			case "aim":
				playerBiped.aim();
				break;
			case "fire":
				fire = keyPressed;
				break;
			case "use":
				use = keyPressed;
				break;

			case "next_weapon":
				if (keyPressed)
					playerBiped.nextWeapon();
				break;

			case "prev_weapon":
				if (keyPressed)
					playerBiped.previousWeapon();
				break;
		}
	}


}
