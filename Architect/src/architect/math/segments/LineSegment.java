package architect.math.segments;

import architect.math.Rectangle;
import architect.math.Vector2D;
import static architect.Constants.*;
import static architect.math.segments.Orientation.Horizontal;
import static architect.math.segments.Orientation.Vertical;
import static java.lang.Math.*;

public class LineSegment extends Interval {

	/**
	 * x-Value if vertical, y-Value if horizontal
	 */
	public float pos;

	public Orientation orientation;

	public LineSegment(float min, float max, float pos, Orientation orientation) {
		super(min, max);
		this.pos = pos;
		this.orientation = orientation;
	}

	public LineSegment(Interval interval, float pos, Orientation orientation) {
		super(interval);
		this.pos = pos;
		this.orientation = orientation;
	}

	public boolean isVertical() {
		return orientation == Vertical;
	}

	public boolean isHorizontal() {
		return orientation == Horizontal;
	}

	public Vector2D minPoint() {
		return pointOnLine(min);
	}

	public Vector2D maxPoint() {
		return pointOnLine(max);
	}

	public Vector2D center() {
		return pointOnLine(mid());
	}

	private Vector2D pointOnLine(float p) {
		if (orientation == Horizontal)
			return new Vector2D(p, pos);
		else
			return new Vector2D(pos, p);
	}

	public float touchOverlapLength(LineSegment line) {
		if (orientation == line.orientation && abs(pos - line.pos) < EPSILON)
			return max(0, min(max, line.max) - max(min, line.min));
		else
			return 0;
	}

	public LineSegment overlap(LineSegment line) {
		return new LineSegment(super.overlap(line), pos, orientation);
	}

	public LineSegment overlap(Rectangle rect) {
		if (orientation == Vertical)
			return new LineSegment(overlap(rect.minY, rect.maxY), pos, orientation);
		else
			return new LineSegment(overlap(rect.minX, rect.maxX), pos, orientation);
	}

	public Vector2D intersection(LineSegment line) {
		if (line.orientation == Vertical && orientation == Horizontal)
			return new Vector2D(line.pos, pos);
		else if (line.orientation == Horizontal && orientation == Vertical)
			return new Vector2D(pos, line.pos);
		else
			return null;
	}

	public Vector2D touchPoint(LineSegment line) {
		if (orientation != line.orientation)
			return intersection(line);
		else if (canJoin(line)) {
			return overlap(line).center();
		}
		else
			return null;
	}

	public boolean contains(Vector2D point) {
		if (orientation == Horizontal)
			return abs(pos - point.y) < EPSILON && min < point.x + EPSILON && max > point.x - EPSILON;
		else
			return abs(pos - point.x) < EPSILON && min < point.y + EPSILON && max > point.y - EPSILON;
	}

	public boolean isTouching(Rectangle rect) {
		if (orientation == Vertical
			&& overlaps(rect.minY, rect.maxY)
			&& rect.leftOrRightEqual(pos))
			return true;
		return (orientation == Horizontal
				&& overlaps(rect.minX, rect.maxX)
				&& rect.topOrBottomEqual(pos));
	}

	public boolean canJoin(LineSegment line) {
		return line.orientation == orientation && pos - line.pos < EPSILON;
	}

	public void scaleToHold(LineSegment line) {
		min = Math.min(min, line.min);
		max = Math.max(max, line.max);
	}
}
