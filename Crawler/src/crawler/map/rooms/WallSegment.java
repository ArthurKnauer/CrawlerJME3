/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package crawler.map.rooms;

import com.jme3.math.Vector3f;

/**
 *
 * @author VTPlusAKnauer
 */
public class WallSegment {

	WallType type;
	Vector3f center;
	Vector3f normal;
	Vector3f extents;

	public WallSegment(WallType type, Vector3f center, Vector3f normal, Vector3f extents) {
		this.type = type;
		this.center = center;
		this.normal = normal;
		this.extents = extents;
	}

	public void setType(WallType type) {
		this.type = type;
	}

	public void setCenter(Vector3f center) {
		this.center = center;
	}

	public void setNormal(Vector3f normal) {
		this.normal = normal;
	}

	public void setExtents(Vector3f extents) {
		this.extents = extents;
	}

	public WallType getType() {
		return type;
	}

	public Vector3f getCenter() {
		return center;
	}

	public Vector3f getNormal() {
		return normal;
	}

	public Vector3f getExtents() {
		return extents;
	}

}
