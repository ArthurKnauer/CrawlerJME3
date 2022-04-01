package convdecomp.geometry;

public class Point {

	public float x, y;

	public Point(float px, float py) {
		x = px;
		y = py;
	}

	public Point(Point p) {
		this(p.x, p.y);
	}

	public Point(HomoPoint p) {
		this(p.x / p.w, p.y / p.w);
	}

	public float dot(Point p) {
		return x * p.x + y * p.y;
	}

	public float perpdot(Point p, Point q) {
		return x * (q.y - p.y) - y * (q.x - p.x);
	}
	
	public HomoPoint meet(Point p) {
		return new HomoPoint(x * p.y - y * p.x, y - p.y, p.x - x);
	}

	public boolean less(Point q) { /* true if p.x < q.x, break ties on y */
		float diff = x - q.x;
	
		return (diff < 0) || ((diff == 0) && (y < q.y));
	}

	private static float det3(Point p, Point q, Point r) {
		return (q.x - p.x) * (r.y - p.y) - (q.y - p.y) * (r.x - p.x);
	}

	public static boolean right(Point p, Point q, Point r) {
		return det3(p, q, r) < 0.0f;
	}

	public static boolean left(Point p, Point q, Point r) {
		return det3(p, q, r) > 0.0f;
	}
}