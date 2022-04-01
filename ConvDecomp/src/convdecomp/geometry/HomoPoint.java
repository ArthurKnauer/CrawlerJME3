package convdecomp.geometry;

// class representing 2d homogenous coordinates (x,y,w)
public final class HomoPoint {

	protected float w, x, y;

	public HomoPoint(float pw, float px, float py) {
		w = pw;
		x = px;
		y = py;
	}

	public HomoPoint(Point p) {
		this(1.0f, p.x, p.y);
	}

	public HomoPoint(int px, int py) {
		this(1.0f, px, -py);
	}

	public HomoPoint(java.awt.Point p) {
		this(p.x, p.y);
	}
	public final static HomoPoint INFINITY = new HomoPoint(1.0f, 0.0f, 0.0f);

	public HomoPoint assign(float pw, float px, float py) {
		w = pw;
		x = px;
		y = py;
		return this;
	}

	final protected int x() {
		return (int) (x / w);
	} // drawing needs these

	final protected int y() {
		return (int) (-y / w);
	}

	public HomoPoint add(HomoPoint p) {
		x += p.x;
		y += p.y;
		return this;
	}

	public HomoPoint sub(HomoPoint p) {
		x -= p.x;
		y -= p.y;
		return this;
	}

	public HomoPoint neg() {
		w = -w;
		x = -x;
		y = -y;
		return this;
	}

	public HomoPoint mul(float m) {
		w *= m;
		x *= m;
		y *= m;
		return this;
	}

	public HomoPoint div(float m) {
		w /= m;
		x /= m;
		y /= m;
		return this;
	}

	public HomoPoint normalize() {
		return div(len());
	}

	public float len2() {
		return x * x + y * y;
	}

	public float len() {
		return (float)Math.sqrt(this.len2());
	}

	final public HomoPoint perp() {
		float tmp = -y;
		y = x;
		x = tmp;
		return this;
	}

	public float dot(Point p) {
		return w + x * p.x + y * p.y;
	}

	public float dot(HomoPoint p) {
		return w * p.w + x * p.x + y * p.y;
	}

	public float perpdot(HomoPoint p) {
		return x * p.y - y * p.x;
	}

	public float dotperp(HomoPoint p) {
		return -x * p.y + y * p.x;
	}

	public boolean left(Point p) {
		return dot(p) > 0;
	}

	public boolean right(Point p) {
		return dot(p) < 0;
	}

	public static float det(HomoPoint p, HomoPoint q, HomoPoint r) {
		return p.w * q.perpdot(r) - q.w * p.perpdot(r) + r.w * p.perpdot(q);
	}

	public static boolean ccw(HomoPoint p, HomoPoint q, HomoPoint r) {
		return det(p, q, r) > 0;
	}

	public static boolean cw(HomoPoint p, HomoPoint q, HomoPoint r) {
		return det(p, q, r) < 0;
	}

	public java.awt.Point toScreen() {
		return new java.awt.Point((int) (x / w), (int) (-y / w));
	}

	public Point toPoint() {
		return new Point(x / w, y / w);
	}

	public HomoPoint meet(HomoPoint p) {
		return new HomoPoint(x * p.y - y * p.x, p.w * y - w * p.y, w * p.x - p.w * x);
	}

	public HomoPoint meet(Point p) {
		return new HomoPoint(x * p.y - y * p.x, y - w * p.y, w * p.x - x);
	}
}
