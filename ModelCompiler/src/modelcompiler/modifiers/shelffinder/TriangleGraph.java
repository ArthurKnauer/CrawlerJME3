/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelcompiler.modifiers.shelffinder;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.jme3.scene.Mesh;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;

/**
 *
 * @author VTPlusAKnauer
 */
class TriangleGraph {

	@Getter private final Multimap<Integer, Integer> vertexToTriangles;
	@Getter private final HashMap<Integer, TriangleGraphNode> triangleToNode;
	private final Set<Integer> parsedTriangles;

	TriangleGraph(Mesh mesh) {
		vertexToTriangles = ArrayListMultimap.create();
		buildVertexToTrianglesMap(mesh);

		parsedTriangles = new HashSet<>(mesh.getTriangleCount());
		triangleToNode = new HashMap<>(mesh.getTriangleCount());

		for (int triangle = 0; triangle < mesh.getTriangleCount(); triangle++) {
			if (!parsedTriangles.contains(triangle)) {
				TriangleGraphNode start = new TriangleGraphNode(triangle);
				triangleToNode.put(triangle, start);
				
				start.build(mesh, vertexToTriangles, parsedTriangles, triangleToNode);
			}
		}	
	}

	private void buildVertexToTrianglesMap(Mesh mesh) {
		int indices[] = new int[3];

		for (int triangle = 0; triangle < mesh.getTriangleCount(); triangle++) {
			mesh.getTriangle(triangle, indices);
			vertexToTriangles.put(indices[0], triangle);
			vertexToTriangles.put(indices[1], triangle);
			vertexToTriangles.put(indices[2], triangle);
		}
	}

}
