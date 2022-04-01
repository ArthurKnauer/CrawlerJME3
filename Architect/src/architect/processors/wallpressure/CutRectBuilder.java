/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architect.processors.wallpressure;

import architect.floorplan.FloorPlan;
import architect.math.segments.Orientation;
import architect.math.Rectangle;
import architect.math.segments.Side;
import architect.room.Room;
import static architect.utils.ConstructionFlags.Flag.DELETED;
import architect.walls.RoomRectWall;
import static java.lang.Math.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author VTPlusAKnauer
 */
class CutRectBuilder {

	public static List<CutRect> fromOptimalPosOnWallChains(Room room,
														   LinkedList<LinkedList<RoomRectWall>> wallChains,
														   FloorPlan fp) {
		List<CutRect> cutRects = new ArrayList<>();

		for (LinkedList<RoomRectWall> chain : wallChains) {
			if (chain.isEmpty())
				continue;

			float cutPos = 0;
			float wallSegmentWeights = 0;
			float min = Float.MAX_VALUE;
			float max = -Float.MAX_VALUE;
			Orientation orientation = chain.peekFirst().orientation;

			for (RoomRectWall wall : chain) {
				cutPos += wall.optimalPos * (wall.max - wall.min);
				wallSegmentWeights += wall.max - wall.min;

				if (min > wall.min)
					min = wall.min;
				if (max < wall.max)
					max = wall.max;
			}

			cutPos /= wallSegmentWeights;	// average by dividing with sum of all weights
			RoomRectWall firstWall = chain.peekFirst();
			float chainPos = firstWall.pos;
			boolean cutThisLine = false;
			if (abs(cutPos - chainPos) > fp.attribs.minWallFixDepth && max - min > fp.attribs.minWallFixLength) {
				if (orientation == Orientation.Horizontal) // check if cutting rect will be outside of room (no giving)
					cutThisLine = (cutPos > chainPos && firstWall.aboveOf(room)) || (cutPos < firstWall.pos && firstWall.belowOf(room));
				else
					cutThisLine = (cutPos > chainPos && firstWall.rightOf(room)) || (cutPos < firstWall.pos && firstWall.leftOf(room));
			}

			if (cutThisLine) { // chain is between rooms and with good offset, try to make a cut	
				if (orientation == Orientation.Horizontal) {
					cutPos = fp.snapGrid.snapY(cutPos);
					float minY = min(cutPos, chainPos);
					float height = max(cutPos, chainPos) - minY;
					cutRects.add(new CutRect(new Rectangle(min, minY, max - min, height),
											 Orientation.Horizontal, (cutPos < chainPos) ? Side.Top : Side.Bottom));
				}
				else {
					cutPos = fp.snapGrid.snapX(cutPos);
					float minX = min(cutPos, chainPos);
					float width = max(cutPos, chainPos) - minX;
					cutRects.add(new CutRect(new Rectangle(minX, min, width, max - min),
											 Orientation.Vertical, (cutPos < chainPos) ? Side.Right : Side.Left));
				}

				// reset cut line (later rooms will not try to cut here again)
				for (RoomRectWall wall : chain) {
					wall.optimalPos = wall.pos;
					wall.flags.add(DELETED);
				}
			}
		}

		return cutRects;
	}

}
