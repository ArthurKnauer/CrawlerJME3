/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architect.processors.protrusion;

import architect.floorplan.FloorPlan;
import architect.math.segments.Corner;
import architect.math.Rectangle;
import architect.math.segments.Side;
import architect.room.Room;
import architect.room.RoomRect;
import static java.lang.Math.min;
import java.util.Optional;

/**
 *
 * @author VTPlusAKnauer
 */
class ZigZagProtrusion extends Protrusion {

	final Room enveloperRoom;

	public ZigZagProtrusion(RoomRect rr, Rectangle isolatingRect, Room enveloperRoom) {
		super(rr, isolatingRect);
		this.enveloperRoom = enveloperRoom;
	}

	@Override
	float size() {
		return min(isolatingRect.width, isolatingRect.height) * 1.5f;
	}

	@Override
	boolean isSingularRR() {
		return true;
	}

	static ZigZagProtrusion findSmallest(FloorPlan fp, RoomRect rr, float maxProtrusionSize) {
		ZigZagProtrusion smallestProtrusion = null;

		for (Corner corner : Corner.ALL) {
			Optional<ZigZagProtrusion> protrusion = checkCorner(rr, corner, maxProtrusionSize);
			if (protrusion.isPresent() && protrusion.get().size() < maxProtrusionSize) {
				smallestProtrusion = protrusion.get();
				maxProtrusionSize = smallestProtrusion.size();
			}
		}

		return smallestProtrusion;
	}

	private static Optional<ZigZagProtrusion> checkCorner(RoomRect rr, Corner corner, float maxProtrusionSize) {
		if (rr.isCornerTouchingFloorPlanPoly(corner))
			return Optional.empty();

		Room enveloperRoom = rr.roomEnvelopingCorner(corner);

		if (enveloperRoom != null) {
			Rectangle isolatingRect = findProtrusionIsolatingRect(rr, corner, enveloperRoom);
			if (isolatingRect != null && !rr.assignedTo().get().importantRectsOverlap(isolatingRect)) {
				ZigZagProtrusion protrusion = new ZigZagProtrusion(rr, isolatingRect, enveloperRoom);
				if (protrusion.size() < maxProtrusionSize)
					return Optional.of(protrusion);
			}
		}

		return Optional.empty();
	}

	private static Rectangle findProtrusionIsolatingRect(RoomRect rr, Corner corner, Room enveloperRoom) {
		RoomRect verticalEnvelopingRoomNeighbor = findProtrusionEdge(rr, enveloperRoom, corner.verticalSide,
																	 corner.horizontalSide.opposite());
		RoomRect horizontalEnvelopingRoomNeighbor = findProtrusionEdge(rr, enveloperRoom, corner.horizontalSide,
																	   corner.verticalSide.opposite());

		if (verticalEnvelopingRoomNeighbor != null && horizontalEnvelopingRoomNeighbor != null)
			return isolatingRect(rr, corner, verticalEnvelopingRoomNeighbor,
								 horizontalEnvelopingRoomNeighbor);
		return null;
	}

	private static RoomRect findProtrusionEdge(RoomRect rr, Room enveloperRoom, Side sideTowardsEnveloper, Side walkDir) {
		if (!walkDir.isOrthogonalTo(sideTowardsEnveloper))
			throw new RuntimeException("protrusionSide and walkDir must be orthogonal");

		Side sideTowardsRR = sideTowardsEnveloper.opposite();

		RoomRect protrusionEndRR = rr.sideNeighborAtEgde(sideTowardsEnveloper, walkDir.opposite());
		while (protrusionEndRR.isAssignedTo(enveloperRoom)) {
			RoomRect nextRR = protrusionEndRR.sideNeighborAtEgde(walkDir, sideTowardsRR);
			if (nextRR == null)
				return null;
			else if (nextRR.isAssignedTo(enveloperRoom))
				protrusionEndRR = nextRR;
			else if (nextRR.isAssignedLike(rr))
				return protrusionEndRR;
			else
				return null;
		}

		return null;
	}

	private static Rectangle isolatingRect(RoomRect rr, Corner envelopedCorner,
										   RoomRect verticalEnvelopingRoomNeighbor,
										   RoomRect horizontalEnvelopingRoomNeighbor) {
		Side verticalStartSide = envelopedCorner.verticalSide.opposite();
		Side horizontalStartSide = envelopedCorner.horizontalSide.opposite();

		Rectangle narrowedHeightRect
				  = rr.movedSideTo(horizontalStartSide,
								   verticalEnvelopingRoomNeighbor.sidePos(horizontalStartSide));
		Rectangle isolatingRect
				  = narrowedHeightRect.movedSideTo(verticalStartSide,
												   horizontalEnvelopingRoomNeighbor.sidePos(verticalStartSide));
		return isolatingRect;
	}

	@Override
	public String toString() {
		return rr.id + " enveloped by " + enveloperRoom.name();
	}
}
