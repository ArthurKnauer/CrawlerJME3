/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package crawler.navmesh.navpoly;

import architect.utils.UniqueID;
import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector3f;
import crawler.navmesh.astar.AStarNode;
import static crawler.properties.CrawlerProperties.PROPERTIES;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import lombok.*;

/**
 *
 * @author VTPlusAKnauer
 */
@EqualsAndHashCode(exclude = {"vertices", "neighbors", "bbox"}, callSuper = false)
@ToString(exclude = {"vertices", "neighbors"})
public class NavPoly extends AStarNode<NavPoly> {

	@Getter private final int id;
	@Getter private final List<Vector3f> vertices;

	private final HashMap<NavPoly, NavPolyNeighbor> neighbors = new HashMap<>();
	private final BoundingBox bbox;
	
	private static final boolean CHECK_CONVEXITY = PROPERTIES.getBoolean("NavPoly.checkConvexity");

	public NavPoly(List<Vector3f> vertices) {
		this.vertices = Collections.unmodifiableList(vertices);
		this.id = UniqueID.nextID(NavPoly.class);

		bbox = new BoundingBox();
		bbox.computeFromPoints(this.vertices);
		bbox.setYExtent(0.5f);

		if (CHECK_CONVEXITY)
			checkConvexity();
	}

	/**
	 * Vertices must go counter-clock-wise and turning left. Throws exception otherwise.
	 */
	private void checkConvexity() {
		Vector3f firstEdge = getVertex(1).subtract(getVertex(0));
		Vector3f lastEdge = getVertex(0).subtract(getVertex(-1));

		if (lastEdge.cross(firstEdge).y < 0)
			throw new RuntimeException("NavPoly " + id + " has clockwise vertices");
	}

	public Vector3f getVertex(int moduloIndex) {
		return vertices.get(Math.floorMod(moduloIndex, vertices.size()));
	}

	public Vector3f getCenter() {
		return bbox.getCenter();
	}

	public boolean containsVertex(Vector3f vector) {
		return vertices.contains(vector);
	}

	public Vector3f edgeCenter(int edge) {
		Vector3f v1 = getVertex(edge);
		Vector3f v2 = getVertex(edge + 1);
		return v1.add(v2).multLocal(0.5f);
	}

	public void setNeighbor(NavPoly navPoly, int edge) {
		edge = Math.floorMod(edge, vertices.size());
		float cost = navPoly.getCenter().distanceSquared(getCenter());
		neighbors.put(navPoly, new NavPolyNeighbor(cost, edge));
	}

	public void setNeighbor(NavPoly navPoly, Vector3f sideVertex) {
		setNeighbor(navPoly, getIndexOf(sideVertex));
	}

	private int getIndexOf(Vector3f vertex) {
		for (int side = 0; side < vertices.size(); side++) {
			if (getVertex(side) == vertex)
				return side;
		}

		throw new IllegalArgumentException(vertex + " is not part of " + this);
	}

	public NavPolyNeighbor getNeighbor(NavPoly navPoly) {
		return neighbors.get(navPoly);
	}

	public boolean containsLocation(Vector3f location) {
		if (bbox.contains(location)) {
			boolean inside = true;

			Vector3f previousVertex = getVertex(-1);
			for (Vector3f vertex : vertices) {
				Vector3f toPos = location.subtract(previousVertex);
				Vector3f edge = previousVertex.subtract(vertex);
				if (toPos.cross(edge).y < 0) {
					inside = false;
					break;
				}
				previousVertex = vertex;
			}

			return inside;
		}
		return false;
	}

	@Override
	protected float estimateCost(NavPoly node) {
		return node.getCenter().distanceSquared(getCenter());
	}

	@Override
	public Set<NavPoly> neighbors() {
		return neighbors.keySet();
	}

	@Override
	public float neighborCost(NavPoly neighbor) {
		return neighbors.get(neighbor).getCost();
	}
}
