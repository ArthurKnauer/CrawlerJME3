/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architect.processors.wallpressure;

import static architect.Constants.EPSILON;
import architect.floorplan.FloorPlan;
import architect.room.Room;
import static architect.utils.ConstructionFlags.Flag.DELETED;
import architect.walls.RoomRectWall;
import architect.walls.WallType;
import static java.lang.Math.max;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author VTPlusAKnauer
 */
class WallChainBuilder {

	public static LinkedList<LinkedList<RoomRectWall>> forRoom(Room room, FloorPlan fp,
															   List<CutRect> appliedFixCutters) {
		for (RoomRectWall wall : room.getWallLoop()) {
			wall.recheckRoomRects(fp.roomRects);
		}

		LinkedList<LinkedList<RoomRectWall>> wallChains = new LinkedList<>();
		LinkedList<RoomRectWall> currentChain = new LinkedList<>();

		wallChains.add(currentChain);
		for (RoomRectWall wall : room.getWallLoop()) {
			// check if wall was totally cut away by previous cutters
			if (!wall.flags.contains(DELETED) && wall.type == WallType.INTERNAL) {
				if (!wall.isAssignedTo(room)) // rr could have been isolated and assigned to different room
					wall.flags.add(DELETED);
				else {
					float overlap = 0;
					for (CutRect cutter : appliedFixCutters) {
						if (wall.isHorizontal()) {
							if (cutter.getRect().intersectsY(wall.pos))
								overlap += max(0, cutter.getRect().xOverlap(wall.min, wall.max));
						}
						else if (cutter.getRect().intersectsX(wall.pos))
							overlap += max(0, cutter.getRect().yOverlap(wall.min, wall.max));
					}

					if (overlap > wall.length() - EPSILON)
						wall.flags.add(DELETED);
				}
			}

			// if wall already used by previous rooms or just an outer wall
			if (wall.flags.contains(DELETED) || wall.type != WallType.INTERNAL) {
				if (!currentChain.isEmpty()) { // end this chain, start a new one						
					currentChain = new LinkedList<>();
					wallChains.add(currentChain);
				}
				continue;
			}

			// try to connect to first chain if we are nearing the end of the loop
			LinkedList<RoomRectWall> firstChain = wallChains.peekFirst();

			if (currentChain != firstChain && !firstChain.isEmpty()
				&& firstChain.peekFirst().canFuseWith(wall, fp.attribs.maxWallOptimalPosFuseDist)) { // we can connect the end to the start
				currentChain = firstChain;
			}
			else if (!currentChain.isEmpty()
					 && (!currentChain.peekFirst().canFuseWith(wall, fp.attribs.maxWallOptimalPosFuseDist)
						 || (currentChain.peekFirst().flags.contains(DELETED)))) { // new chain
				currentChain = new LinkedList<>();
				wallChains.add(currentChain);
			}

			currentChain.add(wall);
		}

		return wallChains;
	}

}
