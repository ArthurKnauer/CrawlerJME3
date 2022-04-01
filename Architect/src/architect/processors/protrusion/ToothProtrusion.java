/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architect.processors.protrusion;

import architect.math.Rectangle;
import architect.math.segments.Side;
import static architect.processors.protrusion.ToothSibling.Type.*;
import architect.room.RoomRect;
import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 *
 * @author VTPlusAKnauer
 */
class ToothProtrusion extends Protrusion {

	public final ToothSibling rightOrTopEdge;
	public final ToothSibling leftOrBottomEdge;
	public final Side side;

	ToothProtrusion(RoomRect rr, ToothSibling rightOrTopEdge, ToothSibling leftOrBottomEdge, Side side) {
		super(rr, isolatingRect(rightOrTopEdge, leftOrBottomEdge, side));

		this.rightOrTopEdge = rightOrTopEdge;
		this.leftOrBottomEdge = leftOrBottomEdge;
		this.side = side;
	}

	@Override
	float size() {
		return isolatingRect.sideLength(side);
	}

	@Override
	boolean isSingularRR() {
		return rightOrTopEdge.rr == leftOrBottomEdge.rr;
	}

	static ToothProtrusion findSmallest(RoomRect rr, float maxProtrusionSize) {
		for (Side side : Side.values()) {
			//	for (WallSide wall : fp.floorPlanPoly.edgesTouching(rr.sideLineSegment(side))) {
			if (/*(wall.type == WallType.OUTER_WINDOWLESS || !room.stats().needsWindow)
					 &&*/rr.sideLength(side) < maxProtrusionSize/*
					 && !rr.hasSideNeighborAssignedTo(side, rr.assignedTo)*/) {
				ToothProtrusion protrusion = findProtrusion(rr, side);
				if (protrusion != null && protrusion.size() < maxProtrusionSize) {
					return protrusion;
				}
			}
			//	}
		}

		return null;
	}

	private static ToothProtrusion findProtrusion(RoomRect rr, Side side) {
		if (!rr.isAssigned())
			throw new RuntimeException("An unassigned RR cannot be a protrusion");

		Side orthogonalUpOrRight = side.isLeftOrRight ? Side.Top : Side.Right;
		Side orthogonalDownOrLeft = orthogonalUpOrRight.opposite();

		ToothSibling rightOrTopEdge = findProtrusionEdge(rr, side, orthogonalUpOrRight);
		if (rightOrTopEdge == null || rightOrTopEdge.type == FurtherOutwardNoProtrusion)
			return null;

		ToothSibling leftOrBottomEdge = findProtrusionEdge(rr, side, orthogonalDownOrLeft);
		if (leftOrBottomEdge == null || leftOrBottomEdge.type == FurtherOutwardNoProtrusion)
			return null;

		return new ToothProtrusion(rr, rightOrTopEdge, leftOrBottomEdge, side);
	}

	private static ToothSibling findProtrusionEdge(RoomRect rr, Side protrusionSide, Side walkDir) {
		if (!walkDir.isOrthogonalTo(protrusionSide))
			throw new RuntimeException("protrusionSide and walkDir must be orthogonal");

		while (true) {
			if (rr.hasSideNeighborAssignedTo(protrusionSide, rr.assignedTo().get()))
				return null;

			ToothSibling nextSibling = ToothSibling.next(rr, protrusionSide, walkDir);
			if (nextSibling.type != Aligned)
				return nextSibling;

			rr = nextSibling.sibling;
		}
	}

	private static Rectangle isolatingRect(ToothSibling siblingA, ToothSibling siblingB, Side protrusionSide) {
		Rectangle rectEnvelopingBoth = siblingA.rr.scaledToContain(siblingB.rr);

		float protrusionStart = protrusionStart(siblingA, siblingB, protrusionSide);
		return rectEnvelopingBoth.movedSideTo(protrusionSide.opposite(), protrusionStart);
	}

	private static float protrusionStart(ToothSibling siblingA, ToothSibling siblingB, Side protrusionSide) {
		float protrusionStartA = siblingA.rr.sidePos(protrusionSide.opposite());
		float protrusionStartB = siblingB.rr.sidePos(protrusionSide.opposite());

		if (siblingA.type == FurtherInwardEdgeOfProtrusion)
			protrusionStartA = siblingA.sibling.sidePos(protrusionSide);
		if (siblingB.type == FurtherInwardEdgeOfProtrusion)
			protrusionStartB = siblingB.sibling.sidePos(protrusionSide);

		return protrusionSide.isRightOrTop ? max(protrusionStartA, protrusionStartB)
			   : min(protrusionStartA, protrusionStartB);
	}

}
