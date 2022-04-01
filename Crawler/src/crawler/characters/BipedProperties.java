package crawler.characters;

/**
 *
 * @author VTPlusAKnauer
 */
public class BipedProperties {

	private final float maxMoveSpeed;
	private final float movementAcceleration;
	private final float maxRotateSpeed;
	private final float rotateAcceleration;

	public static class Builder {

		private float maxMoveSpeed = 4;
		private float movementAcceleration = 20;
		private float maxRotateSpeed = 3;
		private float rotateAcceleration = 10;

		public Builder setMaxMoveSpeed(float maxMoveSpeed) {
			this.maxMoveSpeed = maxMoveSpeed;
			return this;
		}

		public Builder setMovementAcceleration(float movementAcceleration) {
			this.movementAcceleration = movementAcceleration;
			return this;
		}

		public Builder setMaxRotateSpeed(float maxRotateSpeed) {
			this.maxRotateSpeed = maxRotateSpeed;
			return this;
		}

		public Builder setRotateAcceleration(float rotateAcceleration) {
			this.rotateAcceleration = rotateAcceleration;
			return this;
		}

		public BipedProperties build() {
			return new BipedProperties(maxMoveSpeed,
									   movementAcceleration,
									   maxRotateSpeed,
									   rotateAcceleration);
		}

	}

	private BipedProperties(float maxMoveSpeed,
							float movementAcceleration,
							float maxRotateSpeed,
							float rotateAcceleration) {
		this.maxMoveSpeed = maxMoveSpeed;
		this.movementAcceleration = movementAcceleration;
		this.maxRotateSpeed = maxRotateSpeed;
		this.rotateAcceleration = rotateAcceleration;
	}

	public float getMaxMoveSpeed() {
		return maxMoveSpeed;
	}

	public float getMovementAcceleration() {
		return movementAcceleration;
	}

	public float getMaxRotateSpeed() {
		return maxRotateSpeed;
	}

	public float getRotateAcceleration() {
		return rotateAcceleration;
	}
}
