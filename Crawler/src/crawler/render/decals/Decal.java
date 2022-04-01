/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package crawler.render.decals;

import com.jme3.math.Matrix4f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

/**
 *
 * @author VTPlusAKnauer
 */
public class Decal {

	public Matrix4f decalToWorld, worldToDecal;
	public Vector3f normal;
	public float size, recipSize;

	public Decal(Vector3f location, Vector3f normal, float size) {
		this.normal = normal;
		decalToWorld = new Matrix4f();
		Quaternion rot = new Quaternion();
		rot.lookAt(normal, new Vector3f(0, -1, 0));
		rot.toRotationMatrix(decalToWorld);
		decalToWorld.setTranslation(location);

		worldToDecal = decalToWorld.invert();

		this.size = size;
		recipSize = 1.0f / size;
	}
}
