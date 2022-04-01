/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architect.processors.helpers;

import architect.floorplan.FloorPlan;
import architect.room.Room;
import architect.room.RoomType;

/**
 *
 * @author VTPlusAKnauer
 */
public class OptimalRectFinder {

	private OptimalRectFinder() { // static methods only
	}

	public static void findAll(FloorPlan fp) {
		// update optimal rect for each room -> helps to calculate which RRs belong to which room		
		for (Room room : fp.rooms) {
			if (room.type() != RoomType.Hallway) {
				room.getOptimalRect().update(room, fp.floorPlanPoly);
			}
		}

		// relax optimalrect positions by moving rects away from each other
		for (Room room : fp.rooms) {
			if (room.getOptimalRect().isValid())
				room.getOptimalRect().relaxCollisions(room, fp.floorPlanPoly, fp.rooms);
		}
	}

	

	
}
