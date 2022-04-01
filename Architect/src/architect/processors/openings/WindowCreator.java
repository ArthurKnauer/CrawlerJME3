package architect.processors.openings;

import architect.processors.FloorPlanProcessor;
import architect.processors.helpers.Simplifier;
import architect.processors.helpers.WallCreator;
import architect.room.Room;

/**
 *
 * @author VTPlusAKnauer
 */
public class WindowCreator extends FloorPlanProcessor {

	@Override
	protected void process() {
		Simplifier.simplifyAll(fp);
		WallCreator.updateRoomsAndWalls(fp);

		for (Room room : fp.rooms) {
			room.addWindows(fp);
		}
	}

}
