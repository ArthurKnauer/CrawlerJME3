package skybox.spatials;

import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;

/**
 *
 * @author VTPlusAKnauer
 */
abstract class RendersItself {

	protected abstract Geometry[] getGeometries();

	void render(RenderManager renderManager) {
		for (Geometry geom : getGeometries()) {
			renderManager.renderGeometry(geom);
		}
	}
}
