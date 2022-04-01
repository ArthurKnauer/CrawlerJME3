/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package crawler.map;

import architect.floorplan.FloorPlanAttribs;
import architect.room.Room;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import crawler.map.rooms.Polygon;
import crawler.map.rooms.RoomMesh;
import crawler.map.rooms.WallSegment;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author VTPlusAKnauer
 */
public class FloorPlanMesher {

	public static ArrayList<Mesh> createRoomMeshes(Room room, List<WallSegment> wallSegments, FloorPlanAttribs attribs) {
		RoomMesh roomMesh = new RoomMesh(room, wallSegments, attribs);
		ArrayList<Mesh> meshes = new ArrayList<>(RoomMesh.MAX_MESH_IDX);

		for (int meshType = 0; meshType < RoomMesh.WINDOW_LIGHT_POINTS; meshType++) {
			Mesh mesh = new Mesh();
			mesh.setMode(Mesh.Mode.Triangles);

			ArrayList<Polygon> polySet = roomMesh.polySuperSet.get(meshType);
			int vertexCount = polySet.size() * 3;

			float[] vertices = new float[vertexCount * 3];
			float[] texies = new float[vertexCount * 2];
			float[] normals = new float[vertexCount * 3];
			short[] indices = new short[vertexCount];

			float texScale = 0.5f;

			short vertex = 0;
			int vm[] = {0, 2, 1};

			for (Polygon poly : polySet) {
				for (int v = 0; v < 3; v++) {
					vertices[vertex * 3 + 0] = poly.vertex[vm[v]].x;
					vertices[vertex * 3 + 1] = poly.vertex[vm[v]].y;
					vertices[vertex * 3 + 2] = poly.vertex[vm[v]].z;
					normals[vertex * 3 + 0] = poly.normal[vm[v]].x;
					normals[vertex * 3 + 1] = poly.normal[vm[v]].y;
					normals[vertex * 3 + 2] = poly.normal[vm[v]].z;
					if (Math.abs(poly.normal[vm[v]].y) < 0.5) { // tex coordinate calculation depends on normal
						texies[vertex * 2 + 0] = (poly.vertex[vm[v]].x + poly.vertex[vm[v]].z) * texScale;
						texies[vertex * 2 + 1] = (poly.vertex[vm[v]].y + poly.vertex[vm[v]].z) * texScale;
					}
					else {
						texies[vertex * 2 + 0] = (poly.vertex[vm[v]].x) * texScale;
						texies[vertex * 2 + 1] = (poly.vertex[vm[v]].z) * texScale;
					}
					indices[vertex] = vertex++;
				}
			}

			mesh.setBuffer(VertexBuffer.Type.Position, 3, vertices);
			mesh.setBuffer(VertexBuffer.Type.Normal, 3, normals);
			mesh.setBuffer(VertexBuffer.Type.TexCoord, 2, texies);
			mesh.setBuffer(VertexBuffer.Type.Index, 3, indices);
			meshes.add(mesh);
		}

		meshes.add(createWindowPoints(roomMesh));

		return meshes;
	}

	private static Mesh createWindowPoints(RoomMesh roomMesh) {
		int vertexCount = 0;
		vertexCount += roomMesh.windowPoints.size();

		float[] vertices = new float[vertexCount * 3];
		float[] normals = new float[vertexCount * 3];
		short[] indices = new short[vertexCount];

		short vertex = 0;
		for (int p = 0; p < roomMesh.windowPoints.size(); p++) {
			vertices[vertex * 3 + 0] = roomMesh.windowPoints.get(p).x;
			vertices[vertex * 3 + 1] = roomMesh.windowPoints.get(p).y;
			vertices[vertex * 3 + 2] = roomMesh.windowPoints.get(p).z;
			normals[vertex * 3 + 0] = roomMesh.windowPointNormals.get(p).x;
			normals[vertex * 3 + 1] = roomMesh.windowPointNormals.get(p).y;
			normals[vertex * 3 + 2] = roomMesh.windowPointNormals.get(p).z;

			indices[vertex] = vertex++;
		}

		Mesh mesh = new Mesh();
		mesh.setMode(Mesh.Mode.Points);
		mesh.setBuffer(VertexBuffer.Type.Position, 3, vertices);
		mesh.setBuffer(VertexBuffer.Type.Normal, 3, normals);
		mesh.setBuffer(VertexBuffer.Type.Index, 3, indices);

		return mesh;
	}
}
