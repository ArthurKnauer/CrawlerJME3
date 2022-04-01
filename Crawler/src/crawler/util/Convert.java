/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package crawler.util;

import architect.math.Vector2D;

/**
 *
 * @author VTPlusAKnauer
 */
public class Convert {

	public static com.jme3.math.Vector3f toJMEVector3f(Vector2D vec) {
		return new com.jme3.math.Vector3f(vec.x, 0, vec.y);
	}
}
