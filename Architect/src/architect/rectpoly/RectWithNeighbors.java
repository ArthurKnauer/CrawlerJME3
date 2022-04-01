/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architect.rectpoly;

import architect.math.Rectangle;
import architect.math.segments.Side;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public abstract class RectWithNeighbors<R extends RectWithNeighbors> extends Rectangle {

	private static class RectangleMinXComparator implements Comparator<Rectangle> {

		@Override
		public int compare(Rectangle rr1, Rectangle rr2) {
			if (rr1.minX > rr2.minX)
				return +1;
			else if (rr1.minX < rr2.minX)
				return -1;
			else
				return 0;
		}
	}

	private static class RectangleMinYComparator implements Comparator<Rectangle> {

		@Override
		public int compare(Rectangle rr1, Rectangle rr2) {
			if (rr1.minY > rr2.minY)
				return +1;
			else if (rr1.minY < rr2.minY)
				return -1;
			else
				return 0;
		}
	}

	protected final HashSet<R> neighbors = new HashSet<>();
	protected final EnumMap<Side, TreeSet<R>> sideNeighbors = new EnumMap<>(Side.class);

	public RectWithNeighbors(float minX, float minY, float width, float height) {
		super(minX, minY, width, height);

		sideNeighbors.put(Side.Left, new TreeSet<>(new RectangleMinYComparator()));
		sideNeighbors.put(Side.Right, new TreeSet<>(new RectangleMinYComparator()));
		sideNeighbors.put(Side.Top, new TreeSet<>(new RectangleMinXComparator()));
		sideNeighbors.put(Side.Bottom, new TreeSet<>(new RectangleMinXComparator()));
	}

	public Set<R> neighbors() {
		return Collections.unmodifiableSet(neighbors);
	}

	public Stream<R> neighborsMatching(Predicate<R> predicate) {
		return neighbors.stream().filter(predicate);
	}

	public boolean isNeighborWith(R rwn) {
		return neighbors.contains(rwn);
	}

	public NavigableSet<R> sideNeighbors(Side side) {
		return sideNeighbors.get(side);
	}

	public R sideNeighborAtEgde(Side neighborSide, Side startSide) {
		NavigableSet<R> sideNeighborsFromStartSide = sideNeighborsStartingFrom(neighborSide, startSide);
		if (sideNeighborsFromStartSide.isEmpty())
			return null;
		else
			return sideNeighborsFromStartSide.first();
	}

	public NavigableSet<R> sideNeighborsStartingFrom(Side neighborSide, Side startSide) {
		if (!neighborSide.isOrthogonalTo(startSide))
			throw new RuntimeException("neighborSide and startSide must be orthogonal, yet are "
									   + neighborSide + " and " + startSide);
		if (startSide.isLeftOrBottom)
			return sideNeighbors.get(neighborSide);
		else
			return sideNeighbors.get(neighborSide).descendingSet();
	}

	public boolean canBeNeighbors(Rectangle rwn, float minOverlapForNeighbor) {
		return isTouchingSide(rwn) && (xOverlap(rwn) > minOverlapForNeighbor || yOverlap(rwn) > minOverlapForNeighbor);
	}

	public boolean addNeighbor(R rwn) {
		if (this == rwn)
			return false;

		if (isTouchingSide(rwn)) {
			Side side = sideTouchingNeighbor(rwn);
			neighbors.add(rwn);
			sideNeighbors.get(side).add(rwn);
			return true;
		}
		else
			return false; // can't be a neighbor, not touching with overlap
	}

	public boolean removeNeighbor(R neighbor) {
		if (neighbors.remove(neighbor)) { // true if had it as neighbor
			if (isTouchingSide(neighbor)) {
				Side side = sideTouchingNeighbor(neighbor);
				sideNeighbors.get(side).remove(neighbor);
			}
			return true;
		}
		else
			return false;
	}

}
