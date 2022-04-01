/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architect.math;

import static java.lang.Math.*;
import org.lwjgl.util.vector.Vector2f;

/**
 * @author VTPlusAKnauer
 */
public class Vector2D {

	public float x, y;

	public Vector2D() {
		super();
	}

	public Vector2D(float x, float y) {
		set(x, y);
	}

	public void set(float x, float y) {
		this.x = x;
		this.y = y;
	}

	
	public Vector2f toVector2f() {
		return new Vector2f(x, y);
	}
	
	public float length() {
		return (float) Math.sqrt(x * x + y * y);
	}

	public float lengthSquared() {
		return x * x + y * y;
	}

	public Vector2D translate(float x, float y) {
		this.x += x;
		this.y += y;
		return this;
	}

	public Vector2D negate() {
		x = -x;
		y = -y;
		return this;
	}

	public Vector2D negate(Vector2D dest) {
		if (dest == null)
			dest = new Vector2D();
		dest.x = -x;
		dest.y = -y;
		return dest;
	}

	public Vector2D normalise(Vector2D dest) {
		float l = length();

		if (dest == null)
			dest = new Vector2D(x / l, y / l);
		else
			dest.set(x / l, y / l);

		return dest;
	}

	public static float dot(Vector2D left, Vector2D right) {
		return left.x * right.x + left.y * right.y;
	}

	public static float angle(Vector2D a, Vector2D b) {
		float dls = dot(a, b) / (a.length() * b.length());
		if (dls < -1f)
			dls = -1f;
		else if (dls > 1.0f)
			dls = 1.0f;
		return (float) Math.acos(dls);
	}

	public static Vector2D add(Vector2D left, Vector2D right, Vector2D dest) {
		if (dest == null)
			return new Vector2D(left.x + right.x, left.y + right.y);
		else {
			dest.set(left.x + right.x, left.y + right.y);
			return dest;
		}
	}

	public static Vector2D sub(Vector2D left, Vector2D right, Vector2D dest) {
		if (dest == null)
			return new Vector2D(left.x - right.x, left.y - right.y);
		else {
			dest.set(left.x - right.x, left.y - right.y);
			return dest;
		}
	}

	public Vector2D scale(float scale) {

		x *= scale;
		y *= scale;

		return this;
	}

	public final float getX() {
		return x;
	}

	public final float getY() {
		return y;
	}

	public final void setX(float x) {
		this.x = x;
	}

	public final void setY(float y) {
		this.y = y;
	}

	public boolean isZero() {
		return x == 0 && y == 0;
	}

	@Override
	public String toString() {
		return "(" + x + ", " + y + ')';
	}

	public Vector2D normalised() {
		return scaled(1.0f / length());
	}

	public Vector2D scaled(float factor) {
		return new Vector2D(x * factor, y * factor);
	}

	public float distanceTo(Vector2D b) {
		return subtract(b).length();
	}

	public Vector2D add(Vector2D b) {
		return new Vector2D(x + b.x, y + b.y);
	}

	public Vector2D addLocal(Vector2D b) {
		return addLocal(b.x, b.y);
	}

	public Vector2D addLocal(float x, float y) {
		this.x += x;
		this.y += y;
		return this;
	}

	public Vector2D subtract(Vector2D b) {
		return new Vector2D(x - b.x, y - b.y);
	}

	public Vector2D subtractLocal(Vector2D b) {
		return subtractLocal(b.x, b.y);
	}

	public Vector2D subtractLocal(float x, float y) {
		this.x -= x;
		this.y -= y;
		return this;
	}

	public float dot(Vector2D b) {
		return x * b.x + y * b.y;
	}

	public float sumOfElements() {
		return x + y;
	}

	public float minElement() {
		return min(x, y);
	}

	public float maxElement() {
		return max(x, y);
	}
}
