package cityplanner.planners.vegetation;

import com.jme3.math.Vector2f;
import java.util.ArrayList;

/**
 *
 * @author VTPlusAKnauer
 */


public class Cluster<T> {
	
	public final Vector2f center;
	public final Vector2f extents;
	
	public final ArrayList<T> list;

	public Cluster(Vector2f center, Vector2f extents) {
		this.center = center;
		this.extents = extents;
		list = new ArrayList<>();
	}	
}
