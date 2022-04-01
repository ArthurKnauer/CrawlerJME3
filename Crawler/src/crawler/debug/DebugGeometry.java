package crawler.debug;

import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.debug.WireBox;
import crawler.main.Globals;
import static crawler.properties.CrawlerProperties.PROPERTIES;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

/**
 *
 * @author VTPlusAKnauer
 */
public class DebugGeometry {

	private static final int MAX_OBJECTS = PROPERTIES.getIntGreaterZero("DebugGeometry.maxObjects");
	private static final Queue<Geometry> addedGeometry = new ArrayBlockingQueue<>(MAX_OBJECTS);

	public static void clearAll() {
		for (Geometry geometry : addedGeometry) {
			geometry.removeFromParent();
		}
		addedGeometry.clear();
	}

	public static Geometry addWireBox(Vector3f pos, float size, ColorRGBA color) {
		Geometry cube = new Geometry("wireframe cube", new WireBox(size, size, size));
		Material mat = new Material(Globals.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
		mat.getAdditionalRenderState().setWireframe(true);
		mat.setColor("Color", color);
		cube.setMaterial(mat);
		cube.setQueueBucket(RenderQueue.Bucket.Transparent);
		cube.setLocalTranslation(pos);

		Globals.getRootNode().attachChild(cube);
		addToQueue(cube);
		return cube;
	}

	private static void addToQueue(Geometry geometry) {
		addedGeometry.add(geometry);
		if (addedGeometry.size() > 31) {
			Geometry toRemove = addedGeometry.poll();
			toRemove.removeFromParent();
		}
	}

}
