/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architect.processors.helpers;

import static architect.Constants.EPSILON;
import architect.math.Rectangle;
import architect.math.segments.Side;
import architect.rectpoly.RectAssignedToPoly;
import static java.lang.Math.*;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 *
 * @author VTPlusAKnauer
 */
public class BiggestRectFinder {

	private BiggestRectFinder() {
	}

	public static Rectangle find(Set<? extends RectAssignedToPoly> subRects,
								 Predicate<RectAssignedToPoly> assignedToPoly,
								 Predicate<RectAssignedToPoly> canBeRoot) {
		if (subRects.isEmpty())
			return null;

		Rectangle biggestRect = Rectangle.ZERO;
		for (RectAssignedToPoly rr : subRects) {

			if (canBeRoot.test(rr)) {

				// clone neighbor map -> it will be modified in the sweep algorithm
				EnumMap<Side, HashSet<RectAssignedToPoly>> sideNeighbors = new EnumMap<>(Side.class);
				for (Side side : Side.values()) {
					sideNeighbors.put(side, new HashSet<>(rr.sideNeighbors(side)));
				}

				Rectangle sweep = rr;
				sweep = biggestRectSweep(Side.Right, sweep, sideNeighbors, assignedToPoly);
				sweep = biggestRectSweep(Side.Left, sweep, sideNeighbors, assignedToPoly);
				sweep = biggestRectSweep(Side.Top, sweep, sideNeighbors, assignedToPoly);
				sweep = biggestRectSweep(Side.Bottom, sweep, sideNeighbors, assignedToPoly);

				if (biggestRect.shortestSideLength() < sweep.shortestSideLength())
					biggestRect = sweep;

				// restart algorithm -> fresh sidemap
				sideNeighbors = new EnumMap<>(Side.class);
				for (Side side : Side.values()) {
					sideNeighbors.put(side, new HashSet<>(rr.sideNeighbors(side)));
				}

				sweep = rr;
				sweep = biggestRectSweep(Side.Top, sweep, sideNeighbors, assignedToPoly);
				sweep = biggestRectSweep(Side.Bottom, sweep, sideNeighbors, assignedToPoly);
				sweep = biggestRectSweep(Side.Right, sweep, sideNeighbors, assignedToPoly);
				sweep = biggestRectSweep(Side.Left, sweep, sideNeighbors, assignedToPoly);

				if (biggestRect.shortestSideLength() < sweep.shortestSideLength())
					biggestRect = sweep;
			}
		}

		return biggestRect;
	}

	private static Rectangle biggestRectSweep(Side direction,
											  Rectangle sweep,
											  EnumMap<Side, HashSet<RectAssignedToPoly>> sideNeighbors,
											  Predicate<RectAssignedToPoly> assignedToPoly) {
		float border = sweep.sidePos(direction);
		float overlap = sweep.sideLength(direction);

		while (overlap > sweep.sideLength(direction) - EPSILON) {
			sweep = sweep.movedSideTo(direction, border);
			border = direction.isLeftOrBottom ? -Float.MAX_VALUE : Float.MAX_VALUE;
			overlap = 0;

			HashSet<RectAssignedToPoly> nextNeighbors = new HashSet<>(sideNeighbors.get(direction));
			sideNeighbors.get(direction).clear();

			for (RectAssignedToPoly rr : nextNeighbors) {
				if (assignedToPoly.test(rr)) {
					overlap += direction.isLeftOrRight ? sweep.yOverlap(rr) : sweep.xOverlap(rr);
					border = direction.isLeftOrBottom ? max(border, rr.sidePos(direction)) : min(border, rr.sidePos(direction));
				}
			}
			if (overlap > sweep.sideLength(direction) - EPSILON) { // keep neighbors that we have not passed completely
				for (RectAssignedToPoly rr : nextNeighbors) {
					if (assignedToPoly.test(rr)) {
						boolean noPass = direction.isLeftOrBottom ? rr.sidePos(direction) < border - EPSILON
										 : rr.sidePos(direction) > border + EPSILON;
						if (noPass)
							sideNeighbors.get(direction).add(rr);
						else { // passed -> add his next neighbors
							for (Object obj : rr.sideNeighbors(direction)) {
								RectAssignedToPoly nb = (RectAssignedToPoly) obj;
								float nbOverlap = direction.isLeftOrRight ? nb.yOverlap(sweep) : nb.xOverlap(sweep);
								if (assignedToPoly.test(nb) && nbOverlap > EPSILON)
									sideNeighbors.get(direction).add(nb);
							}

							if (direction.isLeftOrRight) {
								if (abs(sweep.maxY - rr.maxY) < EPSILON)
									sideNeighbors.get(Side.Top).addAll(rr.sideNeighbors(Side.Top));
								else if (sweep.maxY < rr.maxY)
									sideNeighbors.get(Side.Top).add(rr);
								if (abs(sweep.minY - rr.minY) < EPSILON)
									sideNeighbors.get(Side.Bottom).addAll(rr.sideNeighbors(Side.Bottom));
								else if (sweep.minY > rr.minY)
									sideNeighbors.get(Side.Bottom).add(rr);
							}
							else {
								if (abs(sweep.maxX - rr.maxX) < EPSILON)
									sideNeighbors.get(Side.Right).addAll(rr.sideNeighbors(Side.Right));
								else if (sweep.maxX < rr.maxX)
									sideNeighbors.get(Side.Right).add(rr);
								if (abs(sweep.minX - rr.minX) < EPSILON)
									sideNeighbors.get(Side.Left).addAll(rr.sideNeighbors(Side.Left));
								else if (sweep.minX > rr.minX)
									sideNeighbors.get(Side.Left).add(rr);
							}
						}
					}
				}
			}
		}

		return sweep;
	}
}
