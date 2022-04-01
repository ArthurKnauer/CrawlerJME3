/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package architect.processors;

import architect.processors.helpers.Simplifier;
import architect.processors.helpers.WallCreator;
import architect.room.Room;
import architect.room.RoomType;
import static java.lang.Math.*;
import java.util.ArrayList;

/**
 *
 * @author VTPlusAKnauer
 */
public class RoomMerger extends FloorPlanProcessor {

	@Override
	protected void process() {
		WallCreator.updateRoomsAndWalls(fp);
		boolean hasWeldedRooms = false;
		boolean foundRoomsToWeld = true;
		while (foundRoomsToWeld) {
			Room roomA = null, roomB = null;
			foundRoomsToWeld = false;

			for (Room room : fp.rooms) {
				float bestScore = 0.5f;
				if (room.type() == RoomType.Hallway) {
					for (Room neighbor : room.neighbors()) {
						if (neighbor.type() == RoomType.Hallway) {
							roomA = room;
							roomB = neighbor;
							break; // hallway-hallway weld has max priority
						}
						else if (neighbor.type() == RoomType.LivingRoom) {
							float score = min(neighbor.commonYPerimeter(room) / room.boundingRect().height,
											  neighbor.commonXPerimeter(room) / room.boundingRect().width);

							// a small hallway should be joined to a living room
							if (room.boundingRect().area() < fp.attribs.minHallwayArea) {
								float score2 = 1.0f - room.boundingRect().area() / (fp.attribs.minHallwayArea);
								if (score2 > score)
									score = score2;
							}

							if (score > bestScore) {
								bestScore = score;
								roomA = neighbor; // restult must become livingroom
								roomB = room;
							}
						}
					}
				}
				if (roomA != null && roomB != null)
					break;
			}

			if (roomA != null && roomB != null) {
				//ArchitectHelper.cutConnector(fp, roomA, roomB);
				roomA.addAllSubRects(new ArrayList<>(roomB.subRects()));
				fp.rooms.remove(roomB);
				hasWeldedRooms = true;
				foundRoomsToWeld = true;

				roomA.addImportantRects(roomB.importantRects());
			}
		}

		if (hasWeldedRooms) {
			Simplifier.simplifyAll(fp);
			WallCreator.updateRoomsAndWalls(fp);
		}
	}
}
