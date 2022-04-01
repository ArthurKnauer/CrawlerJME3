package cityplanner.math;

import com.jme3.math.Vector2f;

/**
 *
 * @author VTPlusAKnauer
 */
public class BoundingRect {

	private final Vector2f center;
	private final Vector2f extents;

	public BoundingRect(Vector2f center, Vector2f extents) {
		this.center = center;
		this.extents = extents;
	}

	public Vector2f getCenter() {
		return center;
	}

	public Vector2f getExtents() {
		return extents;
	}

	public boolean isAbove(float y) {
		return center.y - extents.y > y;
	}

	public boolean isBelow(float y) {
		return center.y + extents.y < y;
	}

	public boolean isLeft(float x) {
		return center.x + extents.x < x;
	}

	public boolean isRight(float x) {
		return center.x - extents.x > x;
	}

	public BoundingRect increased(float increase) {
		return new BoundingRect(center.clone(), extents.add(increase, increase));
	}

	public BoundingRect merge(BoundingRect other) {
		float maxX = Math.max(center.x + extents.x, other.center.x + other.extents.x);
		float maxY = Math.max(center.y + extents.y, other.center.y + other.extents.y);
		float minX = Math.min(center.x - extents.x, other.center.x - other.extents.x);
		float minY = Math.min(center.y - extents.y, other.center.y - other.extents.y);

		return new BoundingRect(new Vector2f((minX + maxX) * 0.5f, (minY + maxY) * 0.5f),
								new Vector2f((maxX - minX) * 0.5f, (maxY - minY) * 0.5f));
	}

	public boolean overlap(BoundingRect other) {
		float maxX = center.x + extents.x + other.extents.x;
		float maxY = center.y + extents.y + other.extents.y;
		float minX = center.x - extents.x - other.extents.x;
		float minY = center.y - extents.y - other.extents.y;

		return other.center.x > minX && other.center.x < maxX
			   && other.center.y > minY && other.center.y < maxY;
	}

}
