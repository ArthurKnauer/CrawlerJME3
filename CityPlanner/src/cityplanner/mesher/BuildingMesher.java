package cityplanner.mesher;

import cityplanner.planners.plots.Plot;
import com.jme3.material.Material;
import com.jme3.math.Vector2f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author VTPlusAKnauer
 */
public class BuildingMesher {

	private static final Vector2f[] texiePattern = {new Vector2f(0, 0),
													new Vector2f(1, 0),
													new Vector2f(1, 1),
													new Vector2f(0, 1)};

	private static final int[] texieIndizes = {3, 2, 0, 1};

	private static final int[] signs = {1, -1, -1, 1};

	private static final boolean[] lengthSide = {true, false, true, false};

	public static List<Geometry> buildGeometries(List<Plot> plots, Random random,
												 ArrayList<Material> facadeMats, Material roofMat) {

		ArrayList<Geometry> geometries = new ArrayList<>(plots.size());

		for (Plot plot : plots) {
			Mesh facadeMesh = buildFacade(plot);
			Geometry facade = new Geometry("facade_" + plot.id, facadeMesh);
			facade.setMaterial(facadeMats.get(random.nextInt(facadeMats.size())));
			geometries.add(facade);

			Mesh roofMesh = buildRoof(plot);
			Geometry roof = new Geometry("roof_" + plot.id, roofMesh);
			roof.setMaterial(roofMat);
			geometries.add(roof);
		}

		return geometries;
	}

	private static Mesh buildFacade(Plot plot) {
		Mesh mesh = new Mesh();
		mesh.setMode(Mesh.Mode.Triangles);

		short vertexCount = 4 * 4;

		float[] vertices = new float[vertexCount * 3];
		float[] texies = new float[vertexCount * 2];
		float[] normals = new float[vertexCount * 3];
		short[] indices = new short[2 * 3 * 4]; // two triangles per side

		short vertex = 0;
		short poly = 0;

		Vector2f[] corners = plot.buildCorners();
		float height = plot.getHeight();

		int floors = (int) (height / 3) / 2;
		int length = (int) ((plot.getLength() * 2) / 3) / 2;
		int width = (int) ((plot.getWidth() * 2) / 3) / 2;

		for (int side = 0; side < 4; side++) { // 4 side walls
			for (int v = 0; v < 4; v++) {
				vertices[vertex * 3 + 0] = corners[(v % 2 + side) % 4].x;
				vertices[vertex * 3 + 1] = v > 1 ? height : 0;
				vertices[vertex * 3 + 2] = corners[(v % 2 + side) % 4].y;

				float sign = signs[side];

				float texXScale = (side % 2 == 1 ? length : width);

				normals[vertex * 3 + 0] = sign * (lengthSide[side] ? plot.getLengthDirection().x : plot.getWidthDirection().x);
				normals[vertex * 3 + 1] = 0;
				normals[vertex * 3 + 2] = sign * (lengthSide[side] ? plot.getLengthDirection().y : plot.getWidthDirection().y);
				texies[vertex * 2 + 0] = texiePattern[texieIndizes[v]].x * (texXScale / 8.0f);
				texies[vertex * 2 + 1] = texiePattern[texieIndizes[v]].y * (floors / 12.0f);
				vertex++;
			}

			indices[poly * 3 + 0] = (short) (vertex - 1);
			indices[poly * 3 + 1] = (short) (vertex - 3);
			indices[poly * 3 + 2] = (short) (vertex - 4);
			poly++;

			indices[poly * 3 + 0] = (short) (vertex - 2);
			indices[poly * 3 + 1] = (short) (vertex - 1);
			indices[poly * 3 + 2] = (short) (vertex - 4);
			poly++;
		}

		mesh.setBuffer(VertexBuffer.Type.Position, 3, vertices);
		mesh.setBuffer(VertexBuffer.Type.Normal, 3, normals);
		mesh.setBuffer(VertexBuffer.Type.TexCoord, 2, texies);
		mesh.setBuffer(VertexBuffer.Type.Index, 3, indices);

		return mesh;
	}

	private static Mesh buildRoof(Plot plot) {
		Mesh mesh = new Mesh();
		mesh.setMode(Mesh.Mode.Triangles);

		short vertexCount = 4;

		float[] vertices = new float[vertexCount * 3];
		float[] texies = new float[vertexCount * 2];
		float[] normals = new float[vertexCount * 3];
		short[] indices = new short[2 * 3]; // two triangles per side

		short vertex = 0;
		short poly = 0;

		Vector2f[] corners = plot.buildCorners();
		float height = plot.getHeight();

		// 1 roof
		for (int v = 0; v < 4; v++) {
			vertices[vertex * 3 + 0] = corners[v].x;
			vertices[vertex * 3 + 1] = height;
			vertices[vertex * 3 + 2] = corners[v].y;
			normals[vertex * 3 + 0] = 0;
			normals[vertex * 3 + 1] = 1;
			normals[vertex * 3 + 2] = 0;
			texies[vertex * 2 + 0] = texiePattern[v].x;
			texies[vertex * 2 + 1] = texiePattern[v].y;
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

		mesh.setBuffer(VertexBuffer.Type.Position, 3, vertices);
		mesh.setBuffer(VertexBuffer.Type.Normal, 3, normals);
		mesh.setBuffer(VertexBuffer.Type.TexCoord, 2, texies);
		mesh.setBuffer(VertexBuffer.Type.Index, 3, indices);

		return mesh;
	}
}
