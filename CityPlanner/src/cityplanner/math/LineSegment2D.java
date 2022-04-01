package cityplanner.math;

import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;

/**
 *
 * @author VTPlusAKnauer
 */
public class LineSegment2D {

	private final Vector2f start;
	private final Vector2f end;

	// lazy values, updated only before request
	private float length;
	private final Vector2f direction, right, middle;
	private boolean updateLength, updateDirection, updateRight, updateMiddle;

	public static class Intersection {

		public boolean found = false;
		public Vector2f point = new Vector2f();
	}

	public LineSegment2D(Vector2f start, Vector2f end) {
		this.start = new Vector2f(start);
		this.end = new Vector2f(end);

		direction = new Vector2f();
		right = new Vector2f();
		middle = new Vector2f();

		startOrEndChanged();
	}

	public void setStart(Vector2f start) {
		this.start.set(start);
		startOrEndChanged();
	}

	public void setEnd(Vector2f end) {
		this.end.set(end);
		startOrEndChanged();
	}

	private void startOrEndChanged() {
		updateLength = true;
		updateDirection = true;
		updateRight = true;
		updateMiddle = true;
	}

	public float getLength() {
		if (updateLength)
			length = end.distance(start);
		updateLength = false;
		return length;
	}

	public Vector2f getStart() {
		return start;
	}

	public Vector2f getEnd() {
		return end;
	}

	public Vector2f getRight() {
		if (updateRight) {
			getDirection(); // update direction
			right.set(direction.y, -direction.x);
			updateRight = false;
		}
		return right;
	}

	public Vector2f getDirection() {
		if (updateDirection) {
			direction.set(end).subtractLocal(start).normalizeLocal();
			updateDirection = false;
		}
		return direction;
	}

	public Vector2f getMiddle() {
		if (updateMiddle) {
			middle.set(start).addLocal(end).multLocal(0.5f);
			updateMiddle = false;
		}
		return middle;
	}

	/**
	 * Computes the nearest point on this segment to the given point.
	 *
	 * @see <a href="http://geomalgorithms.com/a02-_lines.html">geomalgorithms.com/a02-_lines.html</a>
	 * @param point
	 * @return a point on this segment which is closest to P
	 */
	public Vector2f closestPoint(Vector2f point) {
		return closestPoint(this, point);
	}

	/**
	 * Computes the nearest point on the segment to the given point.
	 *
	 * @see <a href="http://geomalgorithms.com/a02-_lines.html">geomalgorithms.com/a02-_lines.html</a>
	 * @param S segment
	 * @param P point
	 * @return a point on segment S which is closest to P
	 */
	public static Vector2f closestPoint(LineSegment2D S, Vector2f P) {
		Vector2f v = S.getEnd().subtract(S.getStart());
		Vector2f w = P.subtract(S.getStart());

		float c1 = w.dot(v);
		if (c1 <= 0)
			return S.getStart();

		float c2 = v.dot(v);
		if (c2 <= c1)
			return S.getEnd();

		float b = c1 / c2;
		Vector2f Pb = v.multLocal(b).addLocal(S.getStart());
		return Pb;
	}

	/**
	 * Computes the distance between two line segments.
	 *
	 * @see <a href="http://geomalgorithms.com/a07-_distance.html">geomalgorithms.com/a07-_distance.html</a>
	 * @param other segment
	 * @param intersection if found it will be set
	 * @return distance between the two closest points of both segments
	 */
	public float distance(LineSegment2D other, Intersection intersection) {
		return distance(this, other, intersection);
	}

	/**
	 * Computes the distance between two line segments.
	 *
	 * @see <a href="http://geomalgorithms.com/a07-_distance.html">geomalgorithms.com/a07-_distance.html</a>
	 * @param S1 first segment
	 * @param S2 second segment
	 * @param intersection if found it will be set
	 * @return distance between the two closest points of both segments
	 */
	public static float distance(LineSegment2D S1, LineSegment2D S2, Intersection intersection) {
		Vector2f u = S1.getEnd().subtract(S1.getStart());
		Vector2f v = S2.getEnd().subtract(S2.getStart());
		Vector2f w = S1.getStart().subtract(S2.getStart());
		float a = u.dot(u);		// always >= 0
		float b = u.dot(v);
		float c = v.dot(v);		// always >= 0
		float d = u.dot(w);
		float e = v.dot(w);
		float D = a * c - b * b;        // always >= 0
		float sc, sN, sD = D;       // sc = sN / sD, default sD = D >= 0
		float tc, tN, tD = D;       // tc = tN / tD, default tD = D >= 0

		// compute the line parameters of the two closest points
		if (D < FastMath.ZERO_TOLERANCE) { // the lines are almost parallel
			sN = 0.0f;         // force using point P0 on segment S1
			sD = 1.0f;         // to prevent possible division by 0.0 later
			tN = e;
			tD = c;
		}
		else {                 // get the closest points on the infinite lines
			sN = (b * e - c * d);
			tN = (a * e - b * d);
			if (sN < 0.0f) {        // sc < 0 => the s=0 edge is visible
				sN = 0.0f;
				tN = e;
				tD = c;
			}
			else if (sN > sD) {  // sc > 1  => the s=1 edge is visible
				sN = sD;
				tN = e + b;
				tD = c;
			}
		}

		if (tN < 0.0f) {            // tc < 0 => the t=0 edge is visible
			tN = 0.0f;
			// recompute sc for this edge
			if (-d < 0.0f)
				sN = 0.0f;
			else if (-d > a)
				sN = sD;
			else {
				sN = -d;
				sD = a;
			}
		}
		else if (tN > tD) {      // tc > 1  => the t=1 edge is visible
			tN = tD;
			// recompute sc for this edge
			if ((-d + b) < 0.0)
				sN = 0;
			else if ((-d + b) > a)
				sN = sD;
			else {
				sN = (-d + b);
				sD = a;
			}
		}

		// finally do the division to get sc and tc
		sc = (FastMath.abs(sN) < FastMath.ZERO_TOLERANCE ? 0.0f : sN / sD);
		tc = (FastMath.abs(tN) < FastMath.ZERO_TOLERANCE ? 0.0f : tN / tD);

		if (sc > 0 && sc < 1 && tc > 0 && tc < 1) { // intersection
			intersection.found = true;
			intersection.point.set(S1.getStart().x + sc * u.x, S1.getStart().y + sc * u.y);
		}
		else
			intersection.found = false;

		// get the difference of the two closest points
		Vector2f dP = new Vector2f(w.x + (sc * u.x) - (tc * v.x),
								   w.y + (sc * u.y) - (tc * v.y));

		return dP.length();   // return the closest distance
	}

	/**
	 * Returns the positive angle of the imagined intersection point if the segments were infinite lines.
	 *
	 * @param other segment
	 * @return angle in [0, PI/2]
	 */
	public float angle(LineSegment2D other) {
		float angle = Math.abs(getDirection().angleBetween(other.getDirection()));
		if (angle > FastMath.HALF_PI)
			angle = FastMath.PI - angle;

		return angle;
	}
}
