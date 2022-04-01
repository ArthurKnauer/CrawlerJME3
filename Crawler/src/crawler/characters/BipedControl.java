package crawler.characters;

import com.jme3.animation.AnimControl;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;
import crawler.main.Globals;

/**
 *
 * @author VTPlusAKnauer
 */
public class BipedControl extends AbstractControl {

	protected final Vector3f move = new Vector3f(0, 0, 0);

	protected float xAngle = FastMath.PI;

	protected final CustomCharacterControl charControl;
	protected final AnimControl animControl;
	protected final BipedProperties properties;

	/**
	 * ***VERY IMPORTANT*+* | Defined in the same order as MoveDirection | ***VERY IMPORTANT***
	 */
	private final static String moveDirectionToWalkAnim[] = {"idle",
															 "walkForward",
															 "walkForwardRight",
															 "walkRight",
															 "walkBackRight",
															 "walkBack",
															 "walkBackLeft",
															 "walkLeft",
															 "walkForwardLeft"};

	public BipedControl(CustomCharacterControl charControl,
						AnimControl animControl,
						BipedProperties properties) {
		this.charControl = charControl;
		this.animControl = animControl;
		this.properties = properties;
	}

	public CustomCharacterControl getCharControl() {
		return charControl;
	}

	public void reset() {
		Globals.getPhysicsSpace().add(charControl);
		move.set(0, 0, 0);
		xAngle = FastMath.PI;
	}
	
	public void attack() {
		if (animControl.isEnabled()) {
			animControl.getChannel(0).setAnim("walkLeft");
		}
	}

	protected void limitXAngleTo2PiRange() {
		if (xAngle > FastMath.TWO_PI || xAngle < -FastMath.TWO_PI)
			xAngle %= FastMath.TWO_PI;
	}

	public void move(MoveDirection moveDirection) {
		move.set(0, 0, 0);

		if (animControl.isEnabled()) {
			String animName = moveDirectionToWalkAnim[moveDirection.ordinal()];
			animControl.getChannel(0).setAnim(animName);
		}

		if (moveDirection != MoveDirection.STOP) {
			Vector3f forwardDir = getForwardDirection();
			Vector3f rightDir = forwardDir.cross(Vector3f.UNIT_Y);

			switch (moveDirection) {
				case FORWARD:
					move.addLocal(forwardDir);
					break;
				case FORWARD_LEFT:
					move.addLocal(forwardDir);
					move.subtractLocal(rightDir);
					break;
				case FORWARD_RIGHT:
					move.addLocal(forwardDir);
					move.addLocal(rightDir);
					break;
				case BACK:
					move.subtractLocal(forwardDir);
					break;
				case BACK_LEFT:
					move.subtractLocal(rightDir);
					move.subtractLocal(forwardDir);
					break;
				case BACK_RIGHT:
					move.addLocal(rightDir);
					move.subtractLocal(forwardDir);
					break;
				case RIGHT:
					move.addLocal(rightDir);
					break;
				case LEFT:
					move.subtractLocal(rightDir);
					break;
				default:
					throw new IllegalArgumentException("Unexpected move direction " + moveDirection);
			}
		}
	}

	public float actualVelocity() {
		return charControl.getVelocity().length();
	}

	@Override
	protected void controlUpdate(float tpf) {
		rotateView();
		moveCharacter();
	}

	protected void rotateView() {
		Quaternion horizontalRot = new Quaternion();
		horizontalRot.fromAngleNormalAxis(xAngle, new Vector3f(0, 1, 0));
		Vector3f charDir = horizontalRot.mult(new Vector3f(0, 0, -1));
		charControl.setViewDirection(charDir);
	}

	private void moveCharacter() {
		if (move.lengthSquared() < 0.01f) {
			charControl.setWalkDirection(Vector3f.ZERO);
		}
		else {
			move.normalizeLocal().multLocal(properties.getMaxMoveSpeed());
			charControl.setWalkDirection(move);
		}
	}

	@Override
	protected void controlRender(RenderManager rm, ViewPort vp) {
	}

	public Vector3f getForwardDirection() {
		return charControl.getViewDirection();
	}
}
