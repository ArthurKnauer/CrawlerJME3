package architect.processors.connector;

import architect.math.Rectangle;
import architect.room.Room;
import java.util.ArrayList;

/**
 *
 * @author VTPlusAKnauer
 */
public class Hallway {

	public final Room room;
	public final Path path;
	public final ArrayList<Rectangle> corridorRects;

	public Hallway(Room room, Path path, ArrayList<Rectangle> corridorRects) {
		this.room = room;
		this.path = path;
		this.corridorRects = corridorRects;
	}
}
