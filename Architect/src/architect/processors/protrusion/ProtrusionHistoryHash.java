/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architect.processors.protrusion;

import architect.math.Rectangle;
import architect.room.Room;
import architect.room.RoomRect;
import architect.snapgrid.SnapGrid;

/**
 *
 * @author VTPlusAKnauer
 */
class ProtrusionHistoryHash {

	private final Room room;
	private final Rectangle rect;
	private final int hashCode;

	ProtrusionHistoryHash(SnapGrid snapGrid, RoomRect rr) {
		this.room = rr.assignedTo().get();
		this.rect = new Rectangle(rr);
		int hash = 7 * room.hashCode();
		hash = 43 * hash + snapGrid.getXSnapIndex(rect.minX);
		hash = 43 * hash + snapGrid.getXSnapIndex(rect.maxX);
		hash = 43 * hash + snapGrid.getYSnapIndex(rect.minY);
		hash = 43 * hash + snapGrid.getYSnapIndex(rect.maxY);
		hashCode = hash;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || getClass() != obj.getClass())
			return false;
		final ProtrusionHistoryHash other = (ProtrusionHistoryHash) obj;
		if (!room.equals(other.room) || !rect.equals(other.rect))
			return false;
		return true;
	}

}
