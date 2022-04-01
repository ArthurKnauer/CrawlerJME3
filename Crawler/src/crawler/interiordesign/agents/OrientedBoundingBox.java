package crawler.interiordesign.agents;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;

/**
 *
 * @author VTPlusAKnauer
 */
public class OrientedBoundingBox {

	private final Vector3f center;
	private final Vector3f[] axes = new Vector3f[3];
	private final Vector3f extents;
	private final float radius, radiusSquared;

	//public Geometry geo;
	public OrientedBoundingBox(Vector3f center, Vector3f extents) {
		this.center = new Vector3f(center);
		this.extents = new Vector3f(extents);
		this.radiusSquared = extents.lengthSquared();
		this.radius = FastMath.sqrt(radiusSquared);

		axes[0] = new Vector3f(Vector3f.UNIT_X);
		axes[1] = new Vector3f(Vector3f.UNIT_Y);
		axes[2] = new Vector3f(Vector3f.UNIT_Z);

//		// TEMP test
//		geo = new Geometry("Box", new Box(extents.x, extents.y, extents.z));
//		geo.setMaterial(MaterialLoader.load("Textures/floor.dds"));
//		geo.setLocalTranslation(center);
//
//		Quaternion rot = new Quaternion().fromAngles((float) Math.random() * 3.0f,
//													 (float) Math.random() * 3.0f,
//													 (float) Math.random() * 3.0f);
//		//Quaternion rot = new Quaternion().fromAngles(0, 0, (float) Math.PI * 0.5f);
//
//		rot.multLocal(axes[0]);
//		rot.multLocal(axes[1]);
//		rot.multLocal(axes[2]);
//		geo.setLocalRotation(rot);
	}

	OrientedBoundingBox(OrientedBoundingBox boundingBox) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public Vector3f getExtents(Vector3f store) {
		return extents.getCopy(store);
	}

	public float getXExtent() {
		return extents.x;
	}

	public float getYExtent() {
		return extents.y;
	}

	public float getZExtent() {
		return extents.z;
	}

	void setXExtent(float xExtent) {
		extents.x = xExtent;
	}

	void setYExtent(float yExtent) {
		extents.y = yExtent;
	}

	void setZExtent(float zExtent) {
		extents.z = zExtent;
	}
	
	public Vector3f getXAxis() {
		return getXAxis(null);
	}

	public Vector3f getYAxis() {
		return getYAxis(null);
	}

	public Vector3f getZAxis() {
		return getZAxis(null);
	}

	public Vector3f getXAxis(Vector3f store) {
		return axes[0].getCopy(store);
	}

	public Vector3f getYAxis(Vector3f store) {
		return axes[1].getCopy(store);
	}

	public Vector3f getZAxis(Vector3f store) {
		return axes[2].getCopy(store);
	}
	
	public Vector3f getPosition() {
		return getPosition(null);
	}

	public Vector3f getPosition(Vector3f store) {
		return center.getCopy(store);
	}

	public void setPosition(Vector3f position) {
		center.set(position);
		//	geo.setLocalTranslation(position);
	}

	public void move(Vector3f dist) {
		move(dist.x, dist.y, dist.z);
	}
	
	public void move(float x, float y, float z) {
		center.addLocal(x, y, z);
	}

	public void alignZAxis(Vector3f frontNormal) {
		axes[0].set(frontNormal.z, 0, -frontNormal.x);
		axes[1].set(0, 1, 0);		
		axes[2].set(frontNormal);
	}

	public boolean overlaps(OrientedBoundingBox other) {
		// translation into my frame
		Vector3f dist = other.center.subtract(center);

		// instead of "dist > ra + rb" asking "dist² > (ra + rb)²" (not using slow sqrt)
		if (dist.lengthSquared() > radiusSquared + other.radiusSquared + 2 * radius * other.radius)
			return false;

		// translation, in A's frame
		float T[] = {dist.dot(axes[0]), dist.dot(axes[1]), dist.dot(axes[2])};

		//B's basis with respect to A's local frame
		float R[][] = new float[3][3];
		float ra, rb, t;
		int i, k;

		float a[] = {extents.x, extents.y, extents.z};
		float b[] = {other.extents.x, other.extents.y, other.extents.z};

		Vector3f[] A = axes;
		Vector3f[] B = other.axes;

		// calculate rotation matrix
		for (i = 0; i < 3; i++) {
			for (k = 0; k < 3; k++) {
				R[i][k] = A[i].dot(B[k]);
			}
		}

		for (i = 0; i < 3; i++) { //A's axes
			ra = a[i];
			rb = b[0] * FastMath.abs(R[i][0]) + b[1] * FastMath.abs(R[i][1]) + b[2] * FastMath.abs(R[i][2]);
			t = FastMath.abs(T[i]);

			if (t > ra + rb)
				return false;
		}

		for (k = 0; k < 3; k++) { // B's axes
			ra = a[0] * FastMath.abs(R[0][k]) + a[1] * FastMath.abs(R[1][k]) + a[2] * FastMath.abs(R[2][k]);
			rb = b[k];
			t = FastMath.abs(T[0] * R[0][k] + T[1] * R[1][k] + T[2] * R[2][k]);

			if (t > ra + rb)
				return false;
		}

		for (i = 0; i < 3; i++) { // 9 cross products	
			int ip = (i + 2) % 3;
			int in = (i + 1) % 3;

			for (k = 0; k < 3; k++) {
				int kp = (k + 2) % 3;
				int kn = (k + 1) % 3;

				ra = a[in] * FastMath.abs(R[ip][k]) + a[ip] * FastMath.abs(R[in][k]);
				rb = b[kn] * FastMath.abs(R[i][kp]) + b[kp] * FastMath.abs(R[i][kn]);
				t = FastMath.abs(T[ip] * R[in][k] - T[in] * R[ip][k]);

				if (t > ra + rb)
					return false;
			}
		}

		return true;
	}

	public float distToResolveOverlapOnAxis(OrientedBoundingBox other, Vector3f moveDir) {
		if (!overlaps(other))
			return 0;

		float ra = extents.x * FastMath.abs(axes[0].dot(moveDir))
				   + extents.y * FastMath.abs(axes[1].dot(moveDir))
				   + extents.z * FastMath.abs(axes[2].dot(moveDir));

		float rb = other.extents.x * FastMath.abs(other.axes[0].dot(moveDir))
				   + other.extents.y * FastMath.abs(other.axes[1].dot(moveDir))
				   + other.extents.z * FastMath.abs(other.axes[2].dot(moveDir));

		Vector3f dist = other.center.subtract(center);
		float t = -dist.dot(moveDir);

		return (ra + rb) - t;
	}

//	public void checkTest(OrientedBoundingBox other) {		
//		setPosition(other.center);
//
//		Quaternion rot = geo.getLocalRotation();
//		Quaternion rotMore = new Quaternion().fromAngles(0, 0.01f, 0.01f);
//		rot.multLocal(rotMore);
//		geo.setLocalRotation(rot);
//
//		axes[0].set(Vector3f.UNIT_X);
//		axes[1].set(Vector3f.UNIT_Y);
//		axes[2].set(Vector3f.UNIT_Z);
//
//		rot.multLocal(axes[0]);
//		rot.multLocal(axes[1]);
//		rot.multLocal(axes[2]);
//
//		if (overlaps(other)) {
//			Vector3f move = new Vector3f(axes[0]);
//			float dist = distToResolveOverlapOnAxis(other, move);
//			Vector3f pos = center.add(move.mult(dist));
//			geo.setLocalTranslation(pos);
//			geo.setMaterial(MaterialLoader.load("Textures/rgby.dds"));
//		}
//		else {
//			geo.setMaterial(MaterialLoader.load("Textures/floor.dds"));
//		}
//	}
	public Vector3f[] getCorners() {
		Vector3f xAxisScaled = axes[0].mult(extents.x);
		Vector3f yAxisScaled = axes[1].mult(extents.y);
		Vector3f zAxisScaled = axes[2].mult(extents.z);
		Vector3f nxAxisScaled = axes[0].mult(-extents.x);
		Vector3f nyAxisScaled = axes[1].mult(-extents.y);
		Vector3f nzAxisScaled = axes[2].mult(-extents.z);

		return new Vector3f[]{center.add(xAxisScaled).add(yAxisScaled).addLocal(zAxisScaled),
							  center.add(xAxisScaled).add(yAxisScaled).addLocal(nzAxisScaled),
							  center.add(xAxisScaled).add(nyAxisScaled).addLocal(zAxisScaled),
							  center.add(xAxisScaled).add(nyAxisScaled).addLocal(nzAxisScaled),
							  center.add(nxAxisScaled).add(yAxisScaled).addLocal(zAxisScaled),
							  center.add(nxAxisScaled).add(yAxisScaled).addLocal(nzAxisScaled),
							  center.add(nxAxisScaled).add(nyAxisScaled).addLocal(zAxisScaled),
							  center.add(nxAxisScaled).add(nyAxisScaled).addLocal(nzAxisScaled)};
	}

}
