/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package crawler.navmesh;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector3f;
import crawler.navmesh.astar.AStar;
import crawler.navmesh.astar.AStarNode;
import crawler.navmesh.navpoly.NavPoly;
import java.util.*;
import lombok.Getter;

/**
 *
 * @author VTPlusAKnauer
 */
public class NavMesh extends AStarNode<NavMesh> {

	private final List<NavPoly> navPolys;
	private final BoundingBox bbox;
	@Getter private final String name;

	private final HashSet<NavMesh> neighbors = new HashSet<>();
	private final HashSet<NavMeshEntrance> entrances = new HashSet<>();
	private final HashMap<NavMesh, NavMeshEntrance> entranceMap = new HashMap<>();

	public NavMesh(String name, List<NavPoly> navPolys, BoundingBox bbox) {
		this.navPolys = navPolys;
		this.bbox = bbox;
		this.name = name;	
	}
	
	public List<NavPoly> getNavPolys() {
		return Collections.unmodifiableList(navPolys);
	}

	public Vector3f getCenter() {
		return bbox.getCenter();
	}

	public void addEntrance(NavMeshEntrance entrance) {
		entrances.add(entrance);
	}

	public Set<NavMeshEntrance> getEntrances() {
		return Collections.unmodifiableSet(entrances);
	}

	public void connectEntrancesWith(NavMesh neighbor) {
		//TODO: fix entrance pair search
		for (NavMeshEntrance myEntrance : entrances) {
			for (NavMeshEntrance hisEntrance : neighbor.entrances) {
				if (myEntrance.getCenter().distance(hisEntrance.getCenter()) < 0.5f) {
					connectEntranceTo(neighbor, myEntrance, hisEntrance);
					neighbor.connectEntranceTo(this, hisEntrance, myEntrance);
					return;
				}
			}
		}

		throw new RuntimeException("Couldn't find entrance neighbor for " + this);
	}

	private void connectEntranceTo(NavMesh neighbor, NavMeshEntrance entrance, NavMeshEntrance neighborEntrance) {
		neighbors.add(neighbor);
		entranceMap.put(neighbor, entrance);
		entrance.setNeighbor(neighbor);
		entrance.setNeighborEntrance(neighborEntrance);
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public int hashCode() {
		return bbox.getCenter().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		return hashCode() == obj.hashCode();
	}

	public Optional<NavPoly> getPolygonByLocation(Vector3f position) {
		if (bbox.contains(position)) {
			for (NavPoly poly : navPolys) {
				if (poly.containsLocation(position))
					return Optional.of(poly);
			}
		}
		return Optional.empty();
	}

	public List<Vector3f> findPath(Vector3f start, Vector3f dest, float radius) {
		Optional<NavPoly> startPoly = getPolygonByLocation(start);
		Optional<NavPoly> destPoly = getPolygonByLocation(dest);
		if (!startPoly.isPresent() || !destPoly.isPresent())
			return Collections.emptyList(); // positions not on this navmesh

		List<NavPoly> path = AStar.findPath(startPoly.get(), destPoly.get());
		return NavPolyTraversal.plan(path, start, dest, radius);
	}

	public List<Vector3f> findPathToNeighbor(NavMesh neighbor, Vector3f start, float radius) {
		NavMeshEntrance entrance = entranceMap.get(neighbor);
		if (entrance == null)
			throw new IllegalArgumentException("NavPoly " + this + " is not neighbor of " + neighbor);

		Optional<NavPoly> optStartPoly = getPolygonByLocation(start);
		if (!optStartPoly.isPresent())
			throw new IllegalArgumentException("Location " + start + " not in navmesh " + this);

		NavPoly startPoly = optStartPoly.get();
		NavPoly destPoly = entrance.getNavPoly();

		Vector3f finish = entrance.neighborEntrance().navPoly.getCenter();
		
		if (startPoly != destPoly) {
			List<NavPoly> polyPath = AStar.findPath(startPoly, destPoly);
			return NavPolyTraversal.plan(polyPath, start, finish, radius);
		}
		else {
			return Arrays.asList(start, finish);
		}
	}

	@Override
	protected float estimateCost(NavMesh node) {
		return node.bbox.getCenter().distanceSquared(bbox.getCenter());
	}

	@Override
	public Set<NavMesh> neighbors() {
		return neighbors;
	}

	@Override
	public float neighborCost(NavMesh neighbor) {
		return neighbor.bbox.getCenter().distanceSquared(bbox.getCenter());
	}
}
