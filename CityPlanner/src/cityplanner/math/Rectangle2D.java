package cityplanner.math;

import cityplanner.quadtree.QuadtreeItem;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;

/**
 *
 * @author VTPlusAKnauer
 */
public class Rectangle2D implements QuadtreeItem {

	private final Vector2f center;
	private final Vector2f lengthDirection;
	private final Vector2f widthDirection;
	private final Vector2f extent1, extent2;
	private float length;
	private float width;
	private float radius;
	private boolean updateExtents = false;
	private boolean updateBoundingRect = true;
	private BoundingRect boundingRect;

	public Rectangle2D(Vector2f center, Vector2f lengthDirection, float length, float width) {
		this.center = new Vector2f(center);
		this.lengthDirection = new Vector2f(lengthDirection);
		this.widthDirection = new Vector2f(lengthDirection.y, -lengthDirection.x);
		this.length = length;
		this.width = width;

		this.radius = FastMath.sqrt(length * length + width * width);

		this.extent1 = new Vector2f(lengthDirection.x * length + widthDirection.x * width,
									lengthDirection.y * length + widthDirection.y * width);

		this.extent2 = new Vector2f(lengthDirection.x * length - widthDirection.x * width,
									lengthDirection.y * length - widthDirection.y * width);
	}

	private void updateExtents() {
		if (updateExtents) {
			this.radius = FastMath.sqrt(length * length + width * width);

			this.extent1.set(lengthDirection.x * length + widthDirection.x * width,
							 lengthDirection.y * length + widthDirection.y * width);

			this.extent2.set(lengthDirection.x * length - widthDirection.x * width,
							 lengthDirection.y * length - widthDirection.y * width);
			updateExtents = false;
		}
	}

	@Override
	public BoundingRect getBoundingRect() {
		if (updateBoundingRect) {
			float xExtent = Math.max(FastMath.abs(extent1.dot(Vector2f.UNIT_X)),
									 FastMath.abs(extent2.dot(Vector2f.UNIT_X)));

			float yExtent = Math.max(FastMath.abs(extent1.dot(Vector2f.UNIT_Y)),
									 FastMath.abs(extent2.dot(Vector2f.UNIT_Y)));
			
			boundingRect = new BoundingRect(center.clone(), new Vector2f(xExtent, yExtent));
		}
		updateBoundingRect = false;
		
		return boundingRect;
	}

	/**
	 * Performs a fast axis-aligned square check and then a simple separating axis test.
	 *
	 * @param location
	 * @return true if location is inside this rectangle
	 */
	public boolean contains(Vector2f location) {
		updateExtents();

		Vector2f centerToPoint = location.subtract(center);

		if (Math.abs(centerToPoint.x) > radius
			|| Math.abs(centerToPoint.y) > radius) // fast axis-aligned bounding square vs bounding square check
			return false;

		float c = FastMath.abs(centerToPoint.dot(lengthDirection));
		if (length < c)
			return false;

		c = FastMath.abs(centerToPoint.dot(widthDirection));
		if (width < c)
			return false;

		return true;
	}

	/**
	 * Separating axis test between two Rectangles
	 *
	 * @param other
	 * @return true if overlap found
	 */
	public boolean overlaps(Rectangle2D other) {
		updateExtents();
		other.updateExtents();

		Vector2f centerToCenter = other.center.subtract(center);

		if (Math.abs(centerToCenter.x) > radius + other.radius
			|| Math.abs(centerToCenter.y) > radius + other.radius) // fast axis-aligned bounding square vs bounding square check
			return false;

		float a = length;
		float b = Math.max(FastMath.abs(other.extent1.dot(lengthDirection)),
						   FastMath.abs(other.extent2.dot(lengthDirection)));
		float c = FastMath.abs(centerToCenter.dot(lengthDirection));
		if (a + b < c)
			return false;

		a = width;
		b = Math.max(FastMath.abs(other.extent1.dot(widthDirection)),
					 FastMath.abs(other.extent2.dot(widthDirection)));
		c = FastMath.abs(centerToCenter.dot(widthDirection));
		if (a + b < c)
			return false;

		a = Math.max(FastMath.abs(extent1.dot(other.lengthDirection)),
					 FastMath.abs(extent2.dot(other.lengthDirection)));
		b = other.length;
		c = FastMath.abs(centerToCenter.dot(other.lengthDirection));
		if (a + b < c)
			return false;

		a = Math.max(FastMath.abs(extent1.dot(other.widthDirection)),
					 FastMath.abs(extent2.dot(other.widthDirection)));
		b = other.width;
		c = FastMath.abs(centerToCenter.dot(other.widthDirection));
		if (a + b < c)
			return false;

		return true;
	}

	public Vector2f[] buildCorners() {
		Vector2f[] corners = new Vector2f[4];

		corners[0] = center.add(extent1);
		corners[1] = center.add(extent2);
		corners[2] = center.subtract(extent1);
		corners[3] = center.subtract(extent2);

		return corners;
	}

	public Vector2f getCenter() {
		return center;
	}

	public Vector2f getLengthDirection() {
		return lengthDirection;
	}

	public Vector2f getWidthDirection() {
		return widthDirection;
	}

	public float getLength() {
		return length;
	}

	public float getWidth() {
		return width;
	}

	public void setCenter(Vector2f center) {
		this.center.set(center);
		updateBoundingRect = true;
	}

	public void setLenghtDirection(Vector2f lengthDirection) {
		this.lengthDirection.set(lengthDirection);
		this.widthDirection.set(lengthDirection.y, -lengthDirection.x);
		updateExtents = true;
		updateBoundingRect = true;
	}

	public void setLength(float length) {
		this.length = length;
		updateExtents = true;
		updateBoundingRect = true;
	}

	public void setWidth(float width) {
		this.width = width;
		updateExtents = true;
		updateBoundingRect = true;
	}
}
