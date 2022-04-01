/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architect.processors.helpers;

import architect.floorplan.FloorPlan;
import architect.math.Rectangle;
import architect.room.Room;
import architect.room.RoomRect;
import java.security.InvalidParameterException;
import java.util.ArrayList;

/**
 *
 * @author VTPlusAKnauer
 */
public final class RoomRectFuser {

	private RoomRectFuser() { // static methods only		
	}

	/**
	 * Fusion RoomRect factory
	 *
	 * @param rrA
	 * @param rrB
	 * @param fp
	 * @return fused RR
	 */
	public static RoomRect fuse(FloorPlan fp, RoomRect rrA, RoomRect rrB) {
		if (!rrA.isAssignedLike(rrB))
			throw new InvalidParameterException("Can't create (fuse) an RR with two RRs from two different rooms "
												+ rrA.assignedTo() + " and " + rrB.assignedTo());

		RoomRect result = new RoomRect(rrA.scaledToContain(rrB), fp.floorPlanPoly);

		for (RoomRect rr : rrA.neighbors()) {
			if (rr != rrB) {
				rr.removeNeighbor(rrA);
				rr.addNeighbor(result);
				result.addNeighbor(rr);
			}
		}
		for (RoomRect rr : rrB.neighbors()) {
			if (rr != rrA) {
				rr.removeNeighbor(rrB);
				rr.addNeighbor(result);
				result.addNeighbor(rr);
			}
		}

		rrA.assignedTo().ifPresent(room -> {
			room.removeSubRect(rrA);
			room.removeSubRect(rrB);
			room.addSubRect(result);
		});

		fp.roomRects.remove(rrA);
		fp.roomRects.remove(rrB);
		fp.roomRects.add(result);

		return result;
	}

	/**
	 * Fusion constructor, where list = one big rectangle array fusion constructor
	 *
	 * @param fp
	 * @param rect encompasses all RRs in list
	 * @param list RRs to fuse
	 * @return
	 */
	public static RoomRect fuse(FloorPlan fp, Rectangle rect, ArrayList<RoomRect> list) {
		final Room room = list.get(0).assignedTo().get();

		for (RoomRect rr : list) {
			if (!rr.isAssignedTo(room))
				throw new InvalidParameterException("Can't create (fuse) an RR with two RRs from two different rooms "
													+ rr.assignedTo() + " and " + room);
		}

		RoomRect result = new RoomRect(rect, fp.floorPlanPoly);

		for (RoomRect rr : list) {
			for (RoomRect nb : rr.neighbors()) {
				nb.removeNeighbor(rr);
				nb.addNeighbor(result);
				result.addNeighbor(nb);
			}
		}

		for (RoomRect rr : list) {
			if (room != null)
				room.removeSubRect(rr);
			fp.roomRects.remove(rr);
			result.removeNeighbor(rr);
		}

		if (room != null)
			room.addSubRect(result);
		fp.roomRects.add(result);

		return result;
	}
}
