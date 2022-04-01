/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architect.processors.protrusion;

import architect.floorplan.FloorPlan;
import architect.room.Room;
import architect.room.RoomRect;
import java.util.HashSet;

/**
 *
 * @author VTPlusAKnauer
 */
class ProtrusionFinder {

	static Protrusion smallestProtrusion(Room room, HashSet<ProtrusionHistoryHash> protrusionHistory, FloorPlan fp) {
		float maxProtrusionSize = room.stats().maxProtrusionSize;
		Protrusion smallestProtrusion = null;

		for (RoomRect rr : room.subRects()) {
			if (!protrusionHistory.contains(new ProtrusionHistoryHash(fp.snapGrid, rr))) {

				Protrusion protrusion = ToothProtrusion.findSmallest(rr, maxProtrusionSize);
				if (protrusion != null) {
					maxProtrusionSize = protrusion.size();
					smallestProtrusion = protrusion;
				}

				protrusion = ZigZagProtrusion.findSmallest(fp, rr, maxProtrusionSize);
				if (protrusion != null) {
					maxProtrusionSize = protrusion.size();
					smallestProtrusion = protrusion;
				}
			}
		}

		if (smallestProtrusion != null)
			protrusionHistory.add(new ProtrusionHistoryHash(fp.snapGrid, smallestProtrusion.rr));

		return smallestProtrusion;
	}
}
