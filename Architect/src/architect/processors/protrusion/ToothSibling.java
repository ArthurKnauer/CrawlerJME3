/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architect.processors.protrusion;

import architect.math.segments.Side;
import architect.room.RoomRect;

/**
 *
 * @author VTPlusAKnauer
 */
class ToothSibling {

	enum Type {

		Aligned,
		NonExistantEdgeOfProtrusion,
		FurtherOutwardNoProtrusion,
		FurtherInwardEdgeOfProtrusion
	};

	public final RoomRect rr;
	public final RoomRect sibling;
	public final Type type;

	private ToothSibling(RoomRect rr, RoomRect sibling, Type type) {
		this.rr = rr;
		this.sibling = sibling;
		this.type = type;

		if (sibling == null && type != Type.NonExistantEdgeOfProtrusion)
			throw new RuntimeException("Sibling rr may be null only with type NonExistant");
	}

	static ToothSibling next(RoomRect rr, Side alignmentSide, Side walkDir) {
		for (RoomRect neighbor : rr.sideNeighborsStartingFrom(walkDir, alignmentSide)) {
			if (neighbor.isAssignedLike(rr)) {
				if (rr.sidePosEqual(alignmentSide, neighbor)) {
					return new ToothSibling(rr, neighbor, Type.Aligned);
				}
				else if (neighbor.furtherThan(rr, alignmentSide))
					return new ToothSibling(rr, neighbor, Type.FurtherOutwardNoProtrusion);
				else
					return new ToothSibling(rr, neighbor, Type.FurtherInwardEdgeOfProtrusion);
			}
			else if (neighbor.furtherThan(rr, alignmentSide.opposite()))
				return new ToothSibling(rr, null, Type.NonExistantEdgeOfProtrusion);
		}

		return new ToothSibling(rr, null, Type.NonExistantEdgeOfProtrusion);
	}
}
