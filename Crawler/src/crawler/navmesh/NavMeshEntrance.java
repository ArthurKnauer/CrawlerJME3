/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package crawler.navmesh;

import com.jme3.math.Vector3f;
import crawler.navmesh.navpoly.NavPoly;

/**
 *
 * @author VTPlusAKnauer
 */
public class NavMeshEntrance {

	public final int navPolySide;
	public final NavPoly navPoly;
	public final String name;
	
	private final Vector3f center;

	private NavMesh neighbor;
	private NavMeshEntrance neighborEntrance;

	public NavMeshEntrance(String name, NavPoly navPoly, int navPolySide) {
		this.name = name;
		this.navPoly = navPoly;
		this.navPolySide = navPolySide;
		this.center = navPoly.edgeCenter(navPolySide);
	}
	
	public Vector3f getCenter() {
		return center;
	}

	public NavPoly getNavPoly() {
		return navPoly;
	}

	public int getNavPolySide() {
		return navPolySide;
	}

	public void setNeighbor(NavMesh neighbor) {
		this.neighbor = neighbor;
	}

	public void setNeighborEntrance(NavMeshEntrance neighborEntrance) {
		this.neighborEntrance = neighborEntrance;
	}

	public NavMesh neighbor() {
		return neighbor;
	}

	public NavMeshEntrance neighborEntrance() {
		return neighborEntrance;
	}

	@Override
	public int hashCode() {
		return navPoly.hashCode() * 11 + navPolySide;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final NavMeshEntrance other = (NavMeshEntrance) obj;
		return navPolySide == other.navPolySide
			   && navPoly == other.navPoly;
	}
}
