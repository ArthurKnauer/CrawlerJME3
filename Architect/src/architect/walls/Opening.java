package architect.walls;

import architect.room.Room;

public class Opening {

	public final float start;
	public final float end;
	public final float top;
	public final float bottom;
	public final float width;
	public final float height;

	public final Room neighborRoom;

	public static enum Type {

		DOOR,
		WINDOW
	}
	public final Type type;

	public Opening(float start, float end, float bottom, float top, Type type, Room neighborRoom) {
		this.start = start;
		this.end = end;
		this.top = top;
		this.bottom = bottom;
		this.type = type;
		width = end - start;
		height = top - bottom;
		this.neighborRoom = neighborRoom;
	}
}
