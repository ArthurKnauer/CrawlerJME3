/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architect.rectpoly;

import static architect.Constants.EPSILON;
import architect.math.segments.LineSegment;
import architect.math.Rectangle;
import architect.math.Vector2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class RectilinearPoly<P extends RectilinearPoly, R extends Rectangle, E extends LineSegment> {

	protected final Set<R> subRects;
	protected final List<E> edgeLoop;

	private ArrayList<Vector2D> nodeLoop = null;

	protected float area;
	protected Rectangle boundingRect;

	public RectilinearPoly(Set<R> subRects, List<E> edgeLoop) {
		this.subRects = subRects;
		this.edgeLoop = edgeLoop;

		area = Rectangle.sumArea(subRects);
		updateBoundingRect();
	}

	final protected void updateBoundingRect() {
		boundingRect = Rectangle.findBoundingRect(subRects);
	}

	final public Rectangle boundingRect() {
		return boundingRect;
	}

	final public float area() {
		return area;
	}

	final public Set<R> subRects() {
		return Collections.unmodifiableSet(subRects);
	}

	final public List<E> edgeLoop() {
		return Collections.unmodifiableList(edgeLoop);
	}

	final public List<Vector2D> nodeLoop() {
		if (nodeLoop == null) {
			nodeLoop = new ArrayList<>(edgeLoop.size());
			for (int e = 0; e < edgeLoop.size(); e++) {
				E currentEdge = edgeLoop().get(e);
				E nextEdge = edgeLoop.get((e + 1) % edgeLoop.size());
				nodeLoop.add(currentEdge.touchPoint(nextEdge));
			}
		}
		return nodeLoop;
	}

	final public List<E> edgesTouching(LineSegment line) {
		return edgeLoop.stream()
				.filter(edge -> edge.touchOverlapLength(line) > EPSILON)
				.collect(Collectors.toList());
	}

	final public boolean isEdgeTouching(LineSegment line) {
		return edgeLoop.stream()
				.anyMatch(edge -> edge.touchOverlapLength(line) > EPSILON);
	}

	final public boolean isEdgeTouching(Vector2D point) {
		return edgeLoop.stream()
				.anyMatch(edge -> edge.contains(point));
	}

	final public boolean isEdgeTouching(Rectangle rect) {
		return edgeLoop.stream()
				.anyMatch(edge -> edge.isTouching(rect));
	}

	final public boolean isOverlapping(Rectangle rect) {
		return boundingRect.overlaps(rect)
			   && subRects.stream().anyMatch(subrect -> subrect.overlaps(rect));
	}

	final public float overlapArea(Rectangle rect) {
		if (boundingRect.overlaps(rect))
			return (float) subRects.stream()
					.mapToDouble(subrect -> subrect.overlapArea(rect))
					.sum();
		return 0;
	}

	final public float commonPerimeter(Rectangle rect) {
		return RectilinearPoly.this.commonXPerimeter(rect) + RectilinearPoly.this.commonYPerimeter(rect);
	}

	final public float commonXPerimeter(Rectangle rect) {
		return (float) subRects.stream()
				.mapToDouble(subrect -> subrect.commonXPerimeter(rect))
				.sum();
	}

	final public float commonYPerimeter(Rectangle rect) {
		return (float) subRects.stream()
				.mapToDouble(subrect -> subrect.commonYPerimeter(rect))
				.sum();
	}

	public R biggestSquareSubRect() {
		return subRects.stream()
				.sorted((ra, rb) -> ra.shortestSideLength() < rb.shortestSideLength() ? +1 : -1)
				.findFirst().orElse(null);
	}

	public Rectangle moveInside(Rectangle rect) {
		Rectangle result = rect;

		float overlapArea = overlapArea(rect);
		if (overlapArea < EPSILON)
			throw new IllegalArgumentException("Can't move " + rect + " inside " + this + " without no initial overlap");
		else if (overlapArea < result.area() - EPSILON) {
			R subRectWithMaxOvelap = subRects.stream()
					.sorted((ra, rb) -> ra.overlapArea(rect) > rb.overlapArea(rect) ? -1 : +1).findFirst()
					.orElseThrow(IllegalArgumentException::new);

			result = rect.movedInside(subRectWithMaxOvelap);
		}

		return result;
	}
}
