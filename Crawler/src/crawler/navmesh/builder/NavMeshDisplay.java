/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package crawler.navmesh.builder;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import crawler.debug.GeometryBuilder;
import crawler.navmesh.NavMesh;
import crawler.navmesh.NavMeshEntrance;
import crawler.navmesh.navpoly.NavPoly;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author VTPlusAKnauer
 */
public class NavMeshDisplay {

	public static Node createDisplay(NavMesh navMesh) {
		Node navMeshNode = new Node();

		List<NavPoly> navPolys = navMesh.getNavPolys();

		for (NavPoly poly : navPolys) {
			Vector3f[] lineVerticies = poly.getVertices().toArray(new Vector3f[poly.getVertices().size() + 1]);
			lineVerticies[lineVerticies.length - 1] = poly.getVertex(0);

			Spatial lines = GeometryBuilder.buildLineStrip(lineVerticies, ColorRGBA.Yellow);
			navMeshNode.attachChild(lines);

			// add poly info text
			Spatial textNode = GeometryBuilder.buildText(poly.getId() + "",
														 poly.getCenter().add(0, 0.1f, 0), ColorRGBA.Yellow);
			navMeshNode.attachChild(textNode);

			// add vertex idx text
			for (int v = 0; v < poly.getVertices().size(); v++) {
				Vector3f pos = poly.getVertex(v).clone();
				Vector3f dirA = poly.getVertex(v + 1).subtract(pos).normalizeLocal().multLocal(0.05f);
				Vector3f dirB = poly.getVertex(v - 1).subtract(pos).normalizeLocal().multLocal(0.05f);
				pos.addLocal(dirA).addLocal(dirB);
				textNode = GeometryBuilder.buildText(v + "", pos, ColorRGBA.Blue);
				navMeshNode.attachChild(textNode);
			}
		}

		for (NavMeshEntrance entrance : navMesh.getEntrances()) {
			Spatial textNode = GeometryBuilder.buildText(entrance.name, entrance.getCenter().add(0, 0.1f, 0), ColorRGBA.Red);
			navMeshNode.attachChild(textNode);
		}

		// navmesh name
		Spatial textNode = GeometryBuilder.buildText(navMesh.getName(), navMesh.getCenter(), ColorRGBA.Orange);
		navMeshNode.attachChild(textNode);

		Spatial neighborLines = createNeighborConnections(navPolys, ColorRGBA.Blue);
		navMeshNode.attachChild(neighborLines);

		return navMeshNode;
	}

	private static Spatial createNeighborConnections(List<NavPoly> navPolys, ColorRGBA color) {
		ArrayList<Vector3f> vertices = new ArrayList<>(navPolys.size() * 2);
		for (NavPoly poly : navPolys) {
			for (NavPoly neighbor : poly.neighbors()) {
				vertices.add(poly.getCenter());
				vertices.add(neighbor.getCenter());
			}
		}

		Vector3f[] vertArray = vertices.stream().toArray(Vector3f[]::new);
		short[] indices = new short[vertArray.length];
		for (short i = 0; i < indices.length; i++) {
			indices[i] = i;
		}

		return GeometryBuilder.buildLines(vertArray, indices, color);
	}
}
