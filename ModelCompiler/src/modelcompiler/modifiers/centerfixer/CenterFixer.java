package modelcompiler.modifiers.centerfixer;

import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.bounding.BoundingVolume;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class CenterFixer {

	public static Vector3f fix(AssetManager assetManager, Spatial spatial) {
		BoundingBox totalBound = null;

		Node node = (Node) spatial;
		for (Spatial child : node.getChildren()) {
			Geometry geometry = (Geometry) child;
			BoundingVolume bound = geometry.getModelBound();

			if (totalBound == null) {
				totalBound = new BoundingBox((BoundingBox) bound);
			}
			else {
				totalBound.mergeLocal(bound);
			}
		}

		Vector3f offset = totalBound.getCenter().mult(-1);

		for (Spatial child : node.getChildren()) {
			Geometry geometry = (Geometry) child;
			movePositions(geometry.getMesh(), offset);
			geometry.updateModelBound();
		}

		spatial.updateModelBound();

		return totalBound.getExtent(null);
	}

	private static void movePositions(Mesh mesh, Vector3f offset) {
		FloatBuffer positions = mesh.getFloatBuffer(VertexBuffer.Type.Position);
		int components = mesh.getVertexCount() * 3;
		float movedPositions[] = new float[components];

		for (int v = 0; v < mesh.getVertexCount(); v++) {
			movedPositions[v * 3 + 0] = positions.get(v * 3 + 0) + offset.x;
			movedPositions[v * 3 + 1] = positions.get(v * 3 + 1) + offset.y;
			movedPositions[v * 3 + 2] = positions.get(v * 3 + 2) + offset.z;
		}

		mesh.clearBuffer(VertexBuffer.Type.Position);
		mesh.setBuffer(VertexBuffer.Type.Position, 3, makeFloatBuffer(movedPositions));
		mesh.updateBound();
	}

	private static FloatBuffer makeFloatBuffer(float[] arr) {
		ByteBuffer bb = ByteBuffer.allocateDirect(arr.length * 4);
		bb.order(ByteOrder.nativeOrder());
		FloatBuffer fb = bb.asFloatBuffer();
		fb.put(arr);
		fb.position(0);
		return fb;
	}
}
