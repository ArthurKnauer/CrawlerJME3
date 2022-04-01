/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package crawler.interiordesign.agents;

import com.jme3.math.Vector3f;

/**
 *
 * @author VTPlusAKnauer
 */
public abstract class OrientedAgentBox extends OrientedBoundingBox {

	protected final boolean isObstructable, isObstructor;
	protected final OrientedBoundingBox unobstructBbox;

	OrientedAgentBox(OrientedAgentBoxBuilder builder) {	
		super(builder.location, builder.extents);
		
		super.alignZAxis(builder.forward);
		unobstructBbox = new OrientedBoundingBox(builder.location, builder.unobstructExtents);
		unobstructBbox.alignZAxis(builder.forward);
		
		this.isObstructable = builder.isObstructable;
		this.isObstructor = builder.isObstructor;
	}

	public float sideExtent(Side side) {
		if (side == Side.FRONT || side == Side.BACK) return getXExtent();
		else return getZExtent();
	}

	public float sideUnobstructExtent(Side side) {
		if (side == Side.FRONT || side == Side.BACK) return unobstructBbox.getXExtent();
		else return unobstructBbox.getZExtent();
	}

	public float normalExtent(Side side) {
		if (side == Side.FRONT || side == Side.BACK) return getZExtent();
		else return getXExtent();
	}

	public float normalUnobstructExtent(Side side) {
		if (side == Side.FRONT || side == Side.BACK) return unobstructBbox.getZExtent();
		else return unobstructBbox.getXExtent();
	}

	public Vector3f getSideNormal(Side side) {		
		Vector3f frontNormal = getZAxis();
		switch (side) {
			case FRONT:
				return new Vector3f(frontNormal.x, 0, frontNormal.z);
			case BACK:
				return new Vector3f(-frontNormal.x, 0, -frontNormal.z);
			case LEFT:
				return new Vector3f(frontNormal.z, 0, -frontNormal.x);
			case RIGHT:
				return new Vector3f(-frontNormal.z, 0, frontNormal.x);
		}
		throw new IllegalArgumentException("invalid side: " + side);
	}

//	protected void updateBoundingBox() {
//		Vector3f rightNormal = getSideNormal(Side.RIGHT);
//		Vector3f cornerA = location.add(frontNormal.mult(extents.z).add(rightNormal.mult(extents.x)).add(0, extents.y, 0));
//		Vector3f cornerB = location.add(frontNormal.mult(-extents.z).add(rightNormal.mult(-extents.x)).add(0, -extents.y, 0));
//
//		Vector3f min = new Vector3f(min(cornerA.x, cornerB.x), min(cornerA.y, cornerB.y), min(cornerA.z, cornerB.z));
//		Vector3f max = new Vector3f(max(cornerA.x, cornerB.x), max(cornerA.y, cornerB.y), max(cornerA.z, cornerB.z));
//		bbox = new BoundingBox(min, max);
//
//		if (unobstructExtents != null) {
//			cornerA = location.add(frontNormal.mult(unobstructExtents.z).add(rightNormal.mult(unobstructExtents.x)).add(0, unobstructExtents.y, 0));
//			cornerB = location.add(frontNormal.mult(-unobstructExtents.z).add(rightNormal.mult(-unobstructExtents.x)).add(0, -unobstructExtents.y, 0));
//
//			min = new Vector3f(min(cornerA.x, cornerB.x), min(cornerA.y, cornerB.y), min(cornerA.z, cornerB.z));
//			max = new Vector3f(max(cornerA.x, cornerB.x), max(cornerA.y, cornerB.y), max(cornerA.z, cornerB.z));
//			unobstructBbox = new BoundingBox(min, max);
//		}
//		else
//			unobstructBbox = bbox;
//
//		cylinderRadius = FastMath.sqrt(extents.x * extents.x + extents.z * extents.z);
//	}

	public void orientateToParent(OrientedAgentBox parent, Side parentSide, Side childSide) {
		Vector3f parentNormal = parent.getSideNormal(parentSide);
		Vector3f frontNormal = getZAxis();
		switch (childSide) {
			case FRONT:
				frontNormal.set(-parentNormal.x, 0, -parentNormal.z);
				break;
			case BACK:
				frontNormal.set(parentNormal.x, 0, parentNormal.z);
				break;
			case LEFT:
				frontNormal.set(parentNormal.z, 0, -parentNormal.x);
				break;
			case RIGHT:
				frontNormal.set(-parentNormal.z, 0, parentNormal.x);
				break;
		}
		
		alignZAxis(frontNormal);
	}

	@Override
	public void alignZAxis(Vector3f frontNormal) {
		super.alignZAxis(frontNormal);
		unobstructBbox.alignZAxis(frontNormal);
	}

	@Override
	public void move(float x, float y, float z) {
		super.move(x, y, z);
		unobstructBbox.move(x, y, z);
	}

	@Override
	public void move(Vector3f dist) {
		super.move(dist);
		unobstructBbox.move(dist);
	}

	@Override
	public void setPosition(Vector3f position) {
		super.setPosition(position);
		unobstructBbox.setPosition(position);
	}
	
	
}
