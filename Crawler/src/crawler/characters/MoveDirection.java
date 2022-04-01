package crawler.characters;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;

/**
 *
 * @author VTPlusAKnauer
 */
public enum MoveDirection {

	STOP(0, 0),
	FORWARD(0, -1),
	FORWARD_RIGHT(1, -1),
	RIGHT(1, 0),
	BACK_RIGHT(1, 1),
	BACK(0, 1),
	BACK_LEFT(-1, 1),
	LEFT(-1, 0),
	FORWARD_LEFT(-1, -1);

	/**
	 * used for adding (interpolating) different directions
	 */
	private static final MoveDirection directionalArray[][] = {{FORWARD_LEFT, FORWARD, FORWARD_RIGHT},
															   {LEFT, STOP, RIGHT},
															   {BACK_LEFT, BACK, BACK_RIGHT}
	};

	private final int arrayX, arrayY;

	private MoveDirection(int arrayX, int arrayY) {
		this.arrayX = arrayX;
		this.arrayY = arrayY;
	}

	/**
	 * Adds two directions and tries to give a reasonable result
	 *
	 * @param other
	 * @return interpolated direction, e.g. FORWARD + LEFT = FORWAR_LEFT, LEFT + RIGHT = STOP
	 */
	public MoveDirection add(MoveDirection other) {
		int sumArrayX = arrayX + other.arrayX;
		int sumArrayY = arrayY + other.arrayY;

		if (sumArrayX < -1)
			sumArrayX = -1;
		else if (sumArrayX > 1)
			sumArrayX = 1;

		if (sumArrayY < -1)
			sumArrayY = -1;
		else if (sumArrayY > 1)
			sumArrayY = 1;

		return directionalArray[1 + sumArrayY][1 + sumArrayX];
	}

	// angle intervals, circle divided into 8 segments
	private static final float maxForwardAngle = FastMath.PI / 8.0f;
	private static final float maxForwardLeftRightAngle = FastMath.PI * 3.0f / 8.0f;
	private static final float maxLeftRightAngle = FastMath.PI * 5.0f / 8.0f;
	private static final float maxBackLeftRightAngle = FastMath.PI * 7.0f / 8.0f;

	/**
	 * Returns relative MoveDirection from the angle between forward and direction.
	 * <img src="../../../doc/images/MoveDirection_angles.png" />
	 *
	 * @param forward the forward facing vector
	 * @param direction the direction to move
	 * @return the relative direction as enum
	 */
	public static MoveDirection relativeMoveDirection(Vector3f forward, Vector3f direction) {
		float angle = forward.signedAngleOnYPlane(direction);

		if (angle > 0)
			if (angle < maxForwardAngle)
				return FORWARD;
			else if (angle < maxForwardLeftRightAngle)
				return FORWARD_RIGHT;
			else if (angle < maxLeftRightAngle)
				return RIGHT;
			else if (angle < maxBackLeftRightAngle)
				return BACK_RIGHT;
			else
				return MoveDirection.BACK;
		else // angle < 0
		if (angle > -maxForwardAngle)
			return FORWARD;
		else if (angle > -maxForwardLeftRightAngle)
			return FORWARD_LEFT;
		else if (angle > -maxLeftRightAngle)
			return LEFT;
		else if (angle > -maxBackLeftRightAngle)
			return BACK_LEFT;
		else
			return BACK;
	}
}
