package cityplanner.mesher;

import cityplanner.planners.vegetation.Cluster;
import cityplanner.planners.vegetation.Tree;
import com.jme3.math.Vector2f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 *
 * @author VTPlusAKnauer
 */
public class TreeMesher {

	public static List<Geometry> buildGeometry(List<Cluster<Tree>> treeClusters, String name, Random random) {
		List<Geometry> treeGeometries = Collections.synchronizedList(new ArrayList<Geometry>());

		long timeStart = System.currentTimeMillis();
		
		Vector2f lookAt = new Vector2f(0, 0);

		//ExecutorService executorService = Executors.newFixedThreadPool(8);
		for (Cluster<Tree> cluster : treeClusters) {

			//	executorService.execute(() -> {
			Mesh mesh = new Mesh();
			mesh.setMode(Mesh.Mode.Triangles);

			int vertexCount = cluster.list.size() * 4;

			float[] vertices = new float[vertexCount * 3];
			float[] texies = new float[vertexCount * 2];
			float[] normals = new float[vertexCount * 3];
			short[] indices = new short[cluster.list.size() * 2 * 3]; // two triangles per tree (quad)

			short vertex = 0;
			short poly = 0;

			final float texScale = 1 / 8.0f;

			for (Tree tree : cluster.list) {

				int texXOffset = random.nextInt(8);
				int texYOffset = random.nextInt(8);

				Vector2f tangent = tree.location.subtract(lookAt).normalizeLocal().rotateLocal90CW();
				float halfSize = tree.size * 0.5f;

				vertices[vertex * 3 + 0] = tree.location.x - tangent.x * halfSize;
				vertices[vertex * 3 + 1] = 0 + tree.heightPos;
				vertices[vertex * 3 + 2] = tree.location.y - tangent.y * halfSize;
				normals[vertex * 3 + 0] = 0;
				normals[vertex * 3 + 1] = 1;
				normals[vertex * 3 + 2] = 0;
				texies[vertex * 2 + 0] = 0 + texScale * texXOffset;
				texies[vertex * 2 + 1] = 0 + texScale * texYOffset;
				vertex++;

				vertices[vertex * 3 + 0] = tree.location.x + tangent.x * halfSize;
				vertices[vertex * 3 + 1] = 0 + tree.heightPos;
				vertices[vertex * 3 + 2] = tree.location.y + tangent.y * halfSize;
				normals[vertex * 3 + 0] = 0;
				normals[vertex * 3 + 1] = 1;
				normals[vertex * 3 + 2] = 0;
				texies[vertex * 2 + 0] = 1 * texScale + texScale * texXOffset;
				texies[vertex * 2 + 1] = 0 + texScale * texYOffset;
				vertex++;

				vertices[vertex * 3 + 0] = tree.location.x + tangent.x * halfSize;
				vertices[vertex * 3 + 1] = tree.size + tree.heightPos;
				vertices[vertex * 3 + 2] = tree.location.y + tangent.y * halfSize;
				normals[vertex * 3 + 0] = 0;
				normals[vertex * 3 + 1] = 1;
				normals[vertex * 3 + 2] = 0;
				texies[vertex * 2 + 0] = 1 * texScale + texScale * texXOffset;
				texies[vertex * 2 + 1] = 1 * texScale + texScale * texYOffset;
				vertex++;

				vertices[vertex * 3 + 0] = tree.location.x - tangent.x * halfSize;
				vertices[vertex * 3 + 1] = tree.size + tree.heightPos;
				vertices[vertex * 3 + 2] = tree.location.y - tangent.y * halfSize;
				normals[vertex * 3 + 0] = 0;
				normals[vertex * 3 + 1] = 1;
				normals[vertex * 3 + 2] = 0;
				texies[vertex * 2 + 0] = 0 + texScale * texXOffset;
				texies[vertex * 2 + 1] = 1 * texScale + texScale * texYOffset;
				vertex++;

				indices[poly * 3 + 0] = (short) (vertex - 2);
				indices[poly * 3 + 1] = (short) (vertex - 3);
				indices[poly * 3 + 2] = (short) (vertex - 4);
				poly++;

				indices[poly * 3 + 0] = (short) (vertex - 4);
				indices[poly * 3 + 1] = (short) (vertex - 1);
				indices[poly * 3 + 2] = (short) (vertex - 2);
				poly++;
			}

			mesh.setBuffer(VertexBuffer.Type.Position, 3, vertices);
			mesh.setBuffer(VertexBuffer.Type.Normal, 3, normals);
			mesh.setBuffer(VertexBuffer.Type.TexCoord, 2, texies);
			mesh.setBuffer(VertexBuffer.Type.Index, 3, indices);

			treeGeometries.add(new Geometry(name + cluster, mesh));
			//	});
		}

//		executorService.shutdown();
//		try {
//			executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
//		} catch (InterruptedException e) {
//			System.err.println("Failed to await thread termination:\n" + e);
//		}
		long timeEnd = System.currentTimeMillis();
		System.out.println("time spent: " + (timeEnd - timeStart) / 1000.0f);

		return treeGeometries;
	}

}
