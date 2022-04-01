package cityplanner.planners.plots;

import cityplanner.math.Rectangle2D;
import com.jme3.math.Vector2f;

/**
 *
 * @author VTPlusAKnauer
 */
public class Plot extends Rectangle2D {

	public final int id;
	public static int idCounter = 0;
	private float height;

	public Plot(Vector2f center, Vector2f lengthDirection, float length, float width) {
		super(center, lengthDirection, length, width);
		id = idCounter++;
	}

	public void setHeight(float height) {
		this.height = height;
	}

	public float getHeight() {
		return height;
	}
}
