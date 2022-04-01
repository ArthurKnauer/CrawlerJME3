/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelcompiler.modifiers.shelffinder;

import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import java.util.List;
import lombok.experimental.UtilityClass;

/**
 *
 * @author VTPlusAKnauer
 */
@UtilityClass
class TriangleClusterMesher {

	static Mesh buildMesh(TriangleCluster cluster)  {
		List<Integer> triangles = cluster.getIndices();
		float[] positions = new float[triangles.size() * 3 * 3]; // 3 members x,y,z
		short[] indices = new short[triangles.size() * 3];

		Vector3f corners[] = {new Vector3f(), new Vector3f(), new Vector3f()};

		int poly = 0;
		for (Integer trindex : triangles) {
			cluster.getMesh().getTriangle(trindex, corners[0], corners[1], corners[2]);
			for (int corner = 0; corner < 3; corner++) {
				positions[poly * 3 * 3 + corner * 3 + 0] = corners[corner].x;
				positions[poly * 3 * 3 + corner * 3 + 1] = corners[corner].y;
				positions[poly * 3 * 3 + corner * 3 + 2] = corners[corner].z;

				indices[poly * 3 + corner] = (short) (poly * 3 + corner);
			}

			poly++;
		}

		Mesh mesh = new Mesh();
		mesh.setMode(Mesh.Mode.Triangles);
		mesh.setBuffer(VertexBuffer.Type.Position, 3, positions);
		mesh.setBuffer(VertexBuffer.Type.Index, 3, indices);
		return mesh;
	}
}
