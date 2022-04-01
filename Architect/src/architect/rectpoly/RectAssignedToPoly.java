/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architect.rectpoly;

import architect.math.segments.Side;
import java.util.Optional;

public class RectAssignedToPoly<P extends RectilinearPoly, R extends RectAssignedToPoly>
		extends RectWithNeighbors<R> {

	protected P assignedTo = null;

	public RectAssignedToPoly(float minX, float minY, float width, float height) {
		super(minX, minY, width, height);
	}

	public Optional<P> assignedTo() {
		return Optional.ofNullable(assignedTo);
	}

	public boolean isAssigned() {
		return assignedTo != null;
	}

	public void assignTo(P poly) {
		assignedTo = poly;
	}

	public boolean isAssignedTo(P poly) {
		return assignedTo == poly;
	}

	public boolean isAssignedLike(R rect) {
		return assignedTo == rect.assignedTo;
	}

	public boolean hasSideNeighborAssignedTo(Side side, P poly) {
		return sideNeighbors.get(side).stream()
				.filter(neighbor -> neighbor.assignedTo == poly).findAny().isPresent();
	}

}
