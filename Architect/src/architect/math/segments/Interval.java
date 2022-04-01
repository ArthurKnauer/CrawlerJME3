package architect.math.segments;

import static architect.Constants.*;
import static java.lang.Math.max;
import static java.lang.Math.min;

public class Interval {

	public float min, max;

	public Interval(float min, float max) {
		this.min = min;
		this.max = max;
	}

	public Interval(Interval interval) {
		this.min = interval.min;
		this.max = interval.max;
	}

	public final float length() {
		return max - min;
	}

	public final float mid() {
		return (max + min) * 0.5f;
	}

	public boolean isValid() {
		return min < max;
	}

	@Override
	public String toString() {
		return "[" + min + ", " + max + "]";
	}

	public final boolean isSubsetOf(float min, float max) {
		return (this.min > min - EPSILON) && (this.max < max + EPSILON);
	}

	public final Interval overlap(float min, float max) {
		return new Interval(max(this.min, min), min(this.max, max));
	}

	public final Interval overlap(Interval interval) {
		return overlap(interval.min, interval.max);
	}

	public final float overlapLength(Interval interval) {
		return min(max, interval.max) - max(min, interval.min);
	}

	public final boolean overlaps(float min, float max) {
		if (this.min > max - EPSILON || this.max < min + EPSILON)
			return false;
		else
			return true;
	}

	public final boolean overlaps(Interval interval) {
		return Interval.this.overlaps(interval.min, interval.max);
	}

	public final float distToNoOverlap(Interval avoid, boolean moveToMin) {
		if (!overlaps(avoid))
			return 0;

		if (moveToMin)
			return EPSILON + (max - avoid.min);
		else
			return EPSILON + (avoid.max - min);
	}
}
