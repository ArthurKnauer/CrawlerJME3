/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architect.processors.helpers;

import architect.floorplan.FloorPlan;
import architect.room.Room;
import architect.room.RoomRect;
import static architect.room.RoomStatus.Flags.*;
import static architect.utils.ConstructionFlags.Flag.*;
import java.util.ArrayList;

/**
 *
 * @author VTPlusAKnauer
 */
public final class Simplifier {

	private Simplifier() { // static methods only		
	}

	public static void simplifyAll(FloorPlan fp) {
		for (Room room : fp.rooms) {
			if (!room.status.contains(Simplified)) {
				simplify(fp, room);
				room.status.add(Simplified);
			}
		}
	}

	private static void simplify(FloorPlan fp, Room room) {
		if (!room.subRects().isEmpty()) {
			boolean foundAndFused = true;
			while (foundAndFused) {
				foundAndFused = false;
				ArrayList<RoomRect> originalList = new ArrayList<>(room.subRects());

				for (RoomRect rrA : originalList) {
					for (RoomRect rrB : originalList) {
						if (rrA != rrB && !rrA.flags.contains(DELETED) && !rrB.flags.contains(DELETED)
							&& rrA.isUnionARectangle(rrB)) {
							RoomRectFuser.fuse(fp, rrA, rrB);
							rrA.flags.add(DELETED);
							rrB.flags.add(DELETED);
							foundAndFused = true;
						}
					}
				}
			}
		}
	}
}
