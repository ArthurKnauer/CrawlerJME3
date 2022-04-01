/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelcompiler.modifiers.shelffinder;

import com.google.common.collect.Multimap;
import com.jme3.scene.Mesh;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 *
 * @author VTPlusAKnauer
 */
@EqualsAndHashCode(of = {"index"})
class TriangleGraphNode {

	private final int index;
	@Getter private final Set<Integer> neighbors = new HashSet<>(3);

	TriangleGraphNode(int index) {
		this.index = index;
	}

	void build(Mesh mesh, Multimap<Integer, Integer> vertexToTriangles, Set<Integer> parsedTriangles,
			   HashMap<Integer, TriangleGraphNode> triangleToNode) {
		parsedTriangles.add(index);

		int vertices[] = new int[3];
		mesh.getTriangle(index, vertices);

		for (int ni : vertices) {
			for (int tri : vertexToTriangles.get(ni)) {
				neighbors.add(tri);
				
				if (!parsedTriangles.contains(tri)) {
					TriangleGraphNode node = new TriangleGraphNode(tri);
					triangleToNode.put(tri, node);
					node.build(mesh, vertexToTriangles, parsedTriangles, triangleToNode);
				}
			}
		}
	}
}
