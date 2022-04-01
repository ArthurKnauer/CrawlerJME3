package crawler.map.rooms;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;

public class Polygon {

	public Vector3f[] vertex = new Vector3f[3];
	public Vector3f[] normal = new Vector3f[3];
	public Vector2f[] texCoord = new Vector2f[3];

	public Polygon(Vector3f a, Vector3f b, Vector3f c, Vector3f n, boolean flip) {
		if (flip) {
			vertex[0] = c;
			vertex[1] = b;
			vertex[2] = a;
		}
		else {
			vertex[0] = a;
			vertex[1] = b;
			vertex[2] = c;
		}

		normal[0] = n;
		normal[1] = n;
		normal[2] = n;
	}
}
