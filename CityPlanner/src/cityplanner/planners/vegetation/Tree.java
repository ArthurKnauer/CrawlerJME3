package cityplanner.planners.vegetation;

import com.jme3.math.Vector2f;

/**
 *
 * @author VTPlusAKnauer
 */
public class Tree {

	public Vector2f location;
	public float heightPos;
	public float size;

	public Tree(Vector2f location, float heightPos, float size) {
		this.location = location;
		this.heightPos = heightPos;
		this.size = size;
	}
}
