/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architect.rectpoly;

import architect.math.segments.LineSegment;
import architect.math.Rectangle;
import architect.math.Vector2D;
import architect.processors.helpers.BiggestRectFinder;
import java.util.*;
import java.util.stream.Stream;

public abstract class RectilinearPolyWithNeighbors<P extends RectilinearPolyWithNeighbors, R extends RectAssignedToPoly<P, R>, E extends LineSegment>
		extends RectilinearPoly<P, R, E> {

	protected final HashSet<P> neighbors = new HashSet<>();

	protected Rectangle biggestRect;
	private boolean biggestRectOutOfDate = true;

	public RectilinearPolyWithNeighbors(Set<R> subRects, List<E> edgeLoop) {
		super(subRects, edgeLoop);
	}

	final public Set<P> neighbors() {
		return Collections.unmodifiableSet(neighbors);
	}

	private void updateNeighbors() {
		neighbors.stream().forEach(neighbor -> neighbor.neighbors.remove(this));
		neighbors.clear();

		subRects.stream().map(R::neighbors).flatMap(Set::stream)
				.filter(rect -> !rect.isAssignedTo((P) this)).map(R::assignedTo)
				.flatMap(opt -> opt.map(Stream::of).orElseGet(Stream::empty))
				.forEach(room -> {
					neighbors.add(room);
					room.neighbors.add(this);
				});
	}

	final public Rectangle biggestRect() {
		if (biggestRectOutOfDate)
			biggestRect = BiggestRectFinder.find(subRects, rr -> rr.isAssignedTo((P) this), rr -> true);
		biggestRectOutOfDate = false;
		return biggestRect;
	}

	final public void addAllSubRects(Collection<R> toAdd) {
		toAdd.stream().forEach(rect -> addSubRect(rect));
	}

	final public void removeAllSubRects(Collection<R> toRemove) {
		toRemove.stream().forEach(rect -> removeSubRect(rect));
	}

	public boolean hasAssigned(R subRect) {
		return subRect.isAssignedTo((P) this);
	}

	public void addSubRect(R subRect) {
		subRect.assignedTo().ifPresent(poly -> poly.removeSubRect(subRect));
		subRect.assignTo((P) this);

		subRects.add(subRect);

		area += subRect.area();
		if (subRects.size() == 1)
			boundingRect = subRect;
		else
			boundingRect = boundingRect.scaledToContain(subRect);

		updateNeighbors();
		biggestRectOutOfDate = true;
	}

	public void removeSubRect(R subRect) {
		if (!subRects.remove(subRect))
			throw new RuntimeException("RoomRect " + subRect + " not in room " + this);

		subRect.assignTo(null);

		area -= subRect.area();
		updateBoundingRect();
		updateNeighbors();

		biggestRectOutOfDate = true;
	}

	final public Vector2D perimeter() {
		float perimeterX = 0, perimeterY = 0;

		for (R subRect : subRects) {
			perimeterX += subRect.width * 2;
			perimeterY += subRect.height * 2;
			for (R neighbor : subRect.neighbors()) {
				if (neighbor.isAssignedTo((P) this)) {
					perimeterX -= subRect.xOverlap(neighbor);
					perimeterY -= subRect.yOverlap(neighbor);
				}
			}
		}

		return new Vector2D(perimeterX, perimeterY);
	}

	final public Vector2D perimeterWithNeighbors() {
		float perimeterX = 0, perimeterY = 0;

		for (R subRect : subRects) {
			for (R neighbor : subRect.neighbors()) {
				if (!neighbor.isAssignedTo((P) this)) {
					perimeterX += subRect.xOverlap(neighbor);
					perimeterY += subRect.yOverlap(neighbor);
				}
			}
		}

		return new Vector2D(perimeterX, perimeterY);
	}

	final public float commonPerimeter(P poly) {
		if (!neighbors.contains(poly))
			return 0;
		return commonXPerimeter(poly) + commonYPerimeter(poly);
	}

	final public float commonXPerimeter(P poly) {
		if (!neighbors.contains(poly))
			return 0;
		return (float) poly.subRects.stream()
				.mapToDouble(rrN -> commonXPerimeter((Rectangle) rrN))
				.sum();
	}

	final public float commonYPerimeter(P poly) {
		if (!neighbors.contains(poly))
			return 0;
		return (float) poly.subRects.stream()
				.mapToDouble(rrN -> commonYPerimeter((Rectangle) rrN))
				.sum();
	}

}
