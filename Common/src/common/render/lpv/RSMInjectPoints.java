package common.render.lpv;

import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;

/**
 *
 * @author VTPlusAKnauer
 */
class RSMInjectPoints extends Geometry {

	public RSMInjectPoints(ReflectiveShadowMap rsm) {
		super("RSMInjectGeometry", createInjectMesh(rsm.getWidth(), rsm.getHeight()));
	}

	/**
	 * Creates a mesh of a plane of 3D points that can be injected into an LPV.
	 *
	 * @param width
	 * @param height
	 */
	private static Mesh createInjectMesh(int width, int height) {
		float[] vertices = new float[width * height * 3];
		short[] indices = new short[width * height];
		for (short x = 0, index = 0; x < width; x++) {
			for (short y = 0; y < height; y++) {
				vertices[index * 3 + 0] = (float) x / (float) width;
				vertices[index * 3 + 1] = (float) y / (float) height;
				vertices[index * 3 + 2] = 0;

				indices[index] = index;
				index++;
			}
		}

		Mesh fillMesh = new Mesh();
		fillMesh.setMode(Mesh.Mode.Points);
		fillMesh.setBuffer(VertexBuffer.Type.Position, 3, vertices);
		fillMesh.setBuffer(VertexBuffer.Type.Index, 3, indices);
		return fillMesh;
	}

}
