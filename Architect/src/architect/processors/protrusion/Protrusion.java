/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architect.processors.protrusion;

import architect.floorplan.FloorPlan;
import architect.math.Rectangle;
import architect.processors.helpers.Cutter;
import architect.processors.helpers.RoomRectFuser;
import architect.room.RoomRect;
import java.util.ArrayList;
import java.util.HashSet;

abstract class Protrusion {

	final RoomRect rr;
	final Rectangle isolatingRect;

	protected Protrusion(RoomRect rr, Rectangle isolatingRect) {
		this.rr = rr;
		this.isolatingRect = isolatingRect;
	}

	abstract float size();

	abstract boolean isSingularRR();

	RoomRect isolateRoomRect(FloorPlan fp) {
		if (isSingularRR() && isolatingRect.equals(rr)) {
			return rr;
		}
		else {
			ArrayList<RoomRect> insideCutter = Cutter.cut(fp, isolatingRect, new HashSet(rr.assignedTo().get().subRects()));
			if (insideCutter.size() > 1)
				return RoomRectFuser.fuse(fp, isolatingRect, insideCutter);
			else
				return insideCutter.get(0);
		}
	}
}
