package common.gizmos;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.debug.Arrow;
import com.jme3.scene.debug.Grid;

/**
 *
 * @author VTPlusAKnauer
 */
public class Gizmos {

	public static AssetManager assetManager;

	public static void init(AssetManager assetManager) {
		Gizmos.assetManager = assetManager;
	}

	public static Node createGrid(int lines, float spacing, ColorRGBA color) {
		Geometry grid = new Geometry("wireframe grid", new Grid(lines, lines, spacing));
		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		mat.getAdditionalRenderState().setWireframe(true);
		mat.setColor("Color", color);
		grid.setMaterial(mat);
		grid.center().move(Vector3f.ZERO);
		grid.setQueueBucket(RenderQueue.Bucket.Transparent);

		Node node = new Node();
		node.attachChild(grid);
		return node;
	}

	public static Node createAxis(float lineWidth, float scale) {
		Node node = new Node();
		Geometry axis = new Geometry("xAxis", new Arrow(Vector3f.UNIT_X.mult(scale)));
		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		mat.setColor("Color", ColorRGBA.Red);
		axis.setMaterial(mat);
		axis.getMesh().setLineWidth(lineWidth);
		axis.setQueueBucket(RenderQueue.Bucket.Transparent);
		node.attachChild(axis);

		axis = new Geometry("xAxis", new Arrow(Vector3f.UNIT_Y.mult(scale)));
		mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		mat.setColor("Color", ColorRGBA.Green);
		axis.setMaterial(mat);
		axis.getMesh().setLineWidth(lineWidth);
		axis.setQueueBucket(RenderQueue.Bucket.Transparent);
		node.attachChild(axis);

		axis = new Geometry("xAxis", new Arrow(Vector3f.UNIT_Z.mult(scale)));
		mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		mat.setColor("Color", ColorRGBA.Blue);
		axis.setMaterial(mat);
		axis.getMesh().setLineWidth(lineWidth);
		axis.setQueueBucket(RenderQueue.Bucket.Transparent);
		node.attachChild(axis);

		return node;
	}
}
