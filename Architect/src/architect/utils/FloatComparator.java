/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package architect.utils;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.function.ToDoubleFunction;
import java.util.stream.Stream;

public class FloatComparator<T> implements Comparator<FloatComparable<T>>, Serializable {

	private static final long serialVersionUID = 3774416224075749063L;

	@Override
	public int compare(FloatComparable<T> a, FloatComparable<T> b) {
		if (a.value() > b.value())
			return -1;
		else
			return +1;
	}

	public static <T> PriorityQueue<FloatComparable<T>> createPriorityQueue(Collection<T> collection,
																			ToDoubleFunction<T> toDouble) {
		if (collection.isEmpty())
			return new PriorityQueue<>(new FloatComparator<T>());

		PriorityQueue<FloatComparable<T>> queue = new PriorityQueue<>(collection.size(), new FloatComparator<T>());
		for (T obj : collection) {
			queue.add(new FloatComparable<>(obj, (float) toDouble.applyAsDouble(obj)));
		}
		return queue;
	}

	public static <T> PriorityQueue<FloatComparable<T>> createPriorityQueue(Stream<T> stream,
																			ToDoubleFunction<T> toDouble) {
		PriorityQueue<FloatComparable<T>> queue = new PriorityQueue<>(new FloatComparator<T>());
		stream.forEach(obj -> {
			queue.add(new FloatComparable<>(obj, (float) toDouble.applyAsDouble(obj)));
		});
		return queue;
	}

	public static <T> Optional<T> findMaxGreaterThan(Collection<T> collection, ToDoubleFunction<T> toDouble, double minValue) {
		double maxValue = minValue;
		T objWithMaxValue = null;
		for (T obj : collection) {
			double value = toDouble.applyAsDouble(obj);
			if (value > maxValue) {
				maxValue = value;
				objWithMaxValue = obj;
			}
		}

		return Optional.ofNullable(objWithMaxValue);
	}

	public static <T> Optional<T> findMinLesserThan(Collection<T> collection, ToDoubleFunction<T> toDouble, double maxValue) {
		double minValue = maxValue;
		T objWithMinValue = null;
		for (T obj : collection) {
			double value = toDouble.applyAsDouble(obj);
			if (value < minValue) {
				minValue = value;
				objWithMinValue = obj;
			}
		}

		return Optional.ofNullable(objWithMinValue);
	}
}
