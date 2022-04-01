/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelcompiler.modifiers.shelffinder;

import com.jme3.math.Triangle;
import com.jme3.scene.Mesh;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import lombok.experimental.UtilityClass;

/**
 *
 * @author VTPlusAKnauer
 */
@UtilityClass
class FlatTriangleClusterFinder {

	private static final float MIN_NORMAL_Y_FOR_FLATNESS = 0.9f;

	static List<TriangleCluster> find(Mesh mesh) {
		TriangleGraph graph = new TriangleGraph(mesh);
		HashSet<Integer> parsedTriangles = new HashSet<>(mesh.getTriangleCount());

		List<TriangleCluster> results = new ArrayList<>();

		for (int trindex = 0; trindex < mesh.getTriangleCount(); trindex++) {
			if (parsedTriangles.contains(trindex))
				continue;

			parsedTriangles.add(trindex);

			Triangle triangle = new Triangle();
			mesh.getTriangle(trindex, triangle);

			if (isFlat(triangle)) {
				ArrayList<Integer> flatConnected = new ArrayList<>();

				collectConnectingFlatTriangles(mesh, trindex, graph, parsedTriangles, flatConnected);
				results.add(new TriangleCluster(mesh, flatConnected));
			}
		}

		return results;
	}

	private static boolean isFlat(Triangle triangle) {
		return triangle.getNormal().y > MIN_NORMAL_Y_FOR_FLATNESS;
	}

	private static void collectConnectingFlatTriangles(Mesh mesh, int trindex, TriangleGraph graph, HashSet<Integer> parsedTriangles, List<Integer> flatCluster) {
		Triangle triangle = new Triangle();
		mesh.getTriangle(trindex, triangle);
		flatCluster.add(trindex);

		TriangleGraphNode node = graph.getTriangleToNode().get(trindex);
		for (int neighbor : node.getNeighbors()) {
			if (!parsedTriangles.contains(neighbor)) {
				parsedTriangles.add(trindex);

				triangle = new Triangle();
				mesh.getTriangle(trindex, triangle);

				if (isFlat(triangle)) {
					collectConnectingFlatTriangles(mesh, neighbor, graph, parsedTriangles, flatCluster);
				}
			}
		}
	}
}
