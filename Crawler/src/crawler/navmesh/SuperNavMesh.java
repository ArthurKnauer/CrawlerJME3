/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package crawler.navmesh;

import com.jme3.math.Vector3f;
import crawler.navmesh.astar.AStar;
import crawler.navmesh.navpoly.NavPoly;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author VTPlusAKnauer
 */
public class SuperNavMesh {

	private final ArrayList<NavMesh> navMeshes = new ArrayList<>();

	public SuperNavMesh() {
	}

	public void add(NavMesh navMesh) {
		navMeshes.add(navMesh);
	}

	public List<Vector3f> findBestPath(Vector3f source, Vector3f dest, float radius) {
		Optional<NavMesh> optStartMesh = findNavMesh(source);
		Optional<NavMesh> optDestMesh = findNavMesh(dest);

		if (optStartMesh.isPresent() && optDestMesh.isPresent()) {
			NavMesh startMesh = optStartMesh.get();
			NavMesh destMesh = optDestMesh.get();

			if (startMesh == destMesh) {
				return startMesh.findPath(source, dest, radius);
			}
			else {
				List<NavMesh> superPath = AStar.findPath(startMesh, destMesh);

				if (!superPath.isEmpty()) {
					return constructSuperPath(superPath, source, dest, radius);
				}

			}
		}

		return Collections.<Vector3f>emptyList();
	}

	public Optional<NavMesh> findNavMesh(Vector3f location) {
		//Ray ray = new Ray(location.add(0, 0.5f, 0), new Vector3f(0, -1, 0));

		for (NavMesh navMesh : navMeshes) {
			Optional<NavPoly> poly = navMesh.getPolygonByLocation(location);
			if (poly.isPresent()) {
				return Optional.of(navMesh);
			}
		}
		return Optional.empty();
	}

	private List<Vector3f> constructSuperPath(List<NavMesh> superPath, Vector3f source, Vector3f dest, float radius) {
		NavMesh previousMesh = null;
		ArrayList<Vector3f> path = new ArrayList<>();
		
		Vector3f currentStart = source.clone();

		// stitch paths from NavMesh to NavMesh
		for (NavMesh navMesh : superPath) {
			if (previousMesh != null) {
				List<Vector3f> subPath = previousMesh.findPathToNeighbor(navMesh, currentStart, radius);
				currentStart = subPath.get(subPath.size() - 1);
				path.addAll(subPath);
			}
			previousMesh = navMesh;
		}
		
		// find path through last NavMesh to the destination
		NavMesh lastNavMesh = superPath.get(superPath.size() - 1);
		List<Vector3f> lastSubPath = lastNavMesh.findPath(currentStart, dest, radius);
		path.addAll(lastSubPath);
		
		return path;
	}
}
