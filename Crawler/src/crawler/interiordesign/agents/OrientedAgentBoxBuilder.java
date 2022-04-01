package crawler.interiordesign.agents;

import com.jme3.math.Vector3f;

/**
 *
 * @author VTPlusAKnauer
 */


abstract class OrientedAgentBoxBuilder {
	
	Vector3f location = Vector3f.ZERO;
	Vector3f extents, unobstructExtents = Vector3f.ZERO;
	Vector3f forward = Vector3f.UNIT_X;
	boolean isObstructable;
	boolean isObstructor;
	
	public void setLocation(Vector3f location) {
		this.location = location;
	}
	
	public void setExtents(Vector3f extents) {
		this.extents = extents;
	}

	public void setUnobstructExtents(Vector3f unobstructExtents) {
		this.unobstructExtents = unobstructExtents;
	}	
	
	public void setForward(Vector3f forward) {
		this.forward = forward;
	}
	
	public void setObstructable(boolean isObstructable) {
		this.isObstructable = isObstructable;
	}
	
	public void setObstructor(boolean isObstructor) {
		this.isObstructor = isObstructor;
	}
	
}
