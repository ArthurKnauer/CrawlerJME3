package cityplanner.mesher;

import cityplanner.planners.roads.Road;
import com.jme3.math.Vector2f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import java.util.List;
import java.util.Random;

/**
 *
 * @author VTPlusAKnauer
 */
public class RoadMesher {
	
	private static final Vector2f[] texiePattern = {new Vector2f(1, 0),
													new Vector2f(1, 1),
													new Vector2f(0, 1),
													new Vector2f(0, 0)};

	private static final float textureScale = 128; // 64m wide and 64m tall
	private static final float laneTextureWidth = 0.125f;

	public static Geometry buildGeometry(List<Road> roads, String name, Random random) {
		Mesh mesh = new Mesh();
		mesh.setMode(Mesh.Mode.Triangles);

		int vertexCount = roads.size() * 4;

		float[] vertices = new float[vertexCount * 3];
		float[] texies = new float[vertexCount * 2];
		float[] normals = new float[vertexCount * 3];
		short[] indices = new short[roads.size() * 2 * 3]; // two triangles per road (quad)

		short vertex = 0;
		short poly = 0;

		for (Road road : roads) {
			Vector2f rightWidth = road.getRight().mult(road.getWidth());

			Vector2f[] corners = {road.getStart().add(rightWidth),
								  road.getEnd().add(rightWidth),
								  road.getEnd().subtract(rightWidth),
								  road.getStart().subtract(rightWidth)};

			int randomXLaneOffset = random.nextInt(8);
			float randomYOffset = random.nextFloat();
			float texXScale = 1;
			if (road.isHighway())
				texXScale = 2;

			for (int v = 0; v < 4; v++) {
				vertices[vertex * 3 + 0] = corners[v].x;
				vertices[vertex * 3 + 1] = 0;
				vertices[vertex * 3 + 2] = corners[v].y;
				normals[vertex * 3 + 0] = 0;
				normals[vertex * 3 + 1] = 1;
				normals[vertex * 3 + 2] = 0;
				texies[vertex * 2 + 0] = (texiePattern[v].x * laneTextureWidth * texXScale) + laneTextureWidth * randomXLaneOffset;
				texies[vertex * 2 + 1] = (texiePattern[v].y * road.getLength() / textureScale) + randomYOffset;
				vertex++;
			}

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

		return new Geometry(name, mesh);
	}

}
