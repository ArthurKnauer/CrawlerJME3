package common.render.lpv;

import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;

/**
 *
 * @author VTPlusAKnauer
 */
/**
 * Volume used to render onto 3D LPV Textures, fills a cube (-1, 1)x(-1, 1)x(0, z). Rendered with an identity projection
 * matrix this geometry fills the whole 3D Texture with the depth sizeZ.
 *
 * @author VTPlusAKnauer
 */
class LPVFillGeometry extends Geometry {

	/**
	 * Creates a geometry that would fill a 3D Texture with the given depth, if rendered with an identity projection
	 * matrix.
	 *
	 * @param depth
	 */
	LPVFillGeometry(int depth) {
		super("lpvFillGeom", createFillMesh(depth));
	}

	/**
	 * Creates a Mesh of an array of quads (2 tris) that fill a cube cube (-1, 1)x(-1, 1)x(0, z)
	 *
	 * @param depth
	 * @return
	 */
	private static Mesh createFillMesh(int depth) {
		float[] vertices = new float[depth * 4 * 3]; // 4 corners, 3 members x,y,z
		short[] indices = new short[depth * 2 * 3];

		for (short z = 0; z < depth; z++) {
			vertices[z * 12 + 0] = -1;
			vertices[z * 12 + 1] = -1;
			vertices[z * 12 + 2] = z;

			vertices[z * 12 + 3] = +1;
			vertices[z * 12 + 4] = -1;
			vertices[z * 12 + 5] = z;

			vertices[z * 12 + 6] = +1;
			vertices[z * 12 + 7] = +1;
			vertices[z * 12 + 8] = z;

			vertices[z * 12 + 9] = -1;
			vertices[z * 12 + 10] = +1;
			vertices[z * 12 + 11] = z;

			indices[z * 6 + 0] = (short) (z * 4 + 0);
			indices[z * 6 + 1] = (short) (z * 4 + 1);
			indices[z * 6 + 2] = (short) (z * 4 + 2);

			indices[z * 6 + 3] = (short) (z * 4 + 2);
			indices[z * 6 + 4] = (short) (z * 4 + 3);
			indices[z * 6 + 5] = (short) (z * 4 + 0);
		}

		Mesh fillMesh = new Mesh();
		fillMesh.setMode(Mesh.Mode.Triangles);
		fillMesh.setBuffer(VertexBuffer.Type.Position, 3, vertices);
		fillMesh.setBuffer(VertexBuffer.Type.Index, 3, indices);

		return fillMesh;
	}

}
