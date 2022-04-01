/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package architect.utils;

import static architect.Constants.EPSILON;
import static java.lang.Math.abs;
import java.util.Objects;

/**
 *
 * @author VTPlusAKnauer
 * @param <T> Class to hold float for
 */
public class FloatComparable<T> implements Comparable<FloatComparable<T>> {

	public final T obj;
	private float value;

	public FloatComparable(T obj, float value) {
		this.obj = obj;
		this.value = value;
	}

	public void setValue(float value) {
		this.value = value;
	}

	public float value() {
		return value;
	}

	@Override
	public int compareTo(FloatComparable<T> obj) {
		if (obj.value() < value)
			return +1;
		else if (obj.value() > value)
			return -1;
		else
			return 0;
	}

	@Override
	public int hashCode() {
		return obj.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final FloatComparable<?> other = (FloatComparable<?>) obj;
		if (!Objects.equals(this.obj, other.obj))
			return false;
		if (abs(this.value - other.value) > EPSILON)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "(" + value + ": " + obj + ")";
	}

}
