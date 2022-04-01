package skybox.spatials;

import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.TextureCubeMap;
import lombok.Getter;

/**
 *
 * @author VTPlusAKnauer
 */
public class StarsSkybox extends RendersItself {

	private final static Quaternion FIX_ME_ROTATION = new Quaternion().fromAngleNormalAxis(FastMath.HALF_PI, Vector3f.UNIT_Y);
	
	@Getter private final Geometry[] geometries = new Geometry[1];

	public StarsSkybox(AssetManager assetManager) {
		Geometry geometry = new Geometry("Sky", new Box(new Vector3f(-1, -1, -1), new Vector3f(1, 1, 1)));

		Material shader = new Material(assetManager, "MatDefs/StarBox/StarBox.j3md");
		Texture texture = assetManager.loadTexture(new TextureKey("Textures/stars_cubemap.dds", false));
		Image image = texture.getImage();
		texture = new TextureCubeMap();
		texture.setImage(image);
		shader.setTexture("CubeMap", texture);
		
		geometry.setMaterial(shader);
		
		geometries[0] = geometry;
	}

	public void setRotation(Quaternion rotation) {
		geometries[0].setLocalRotation(rotation.mult(FIX_ME_ROTATION));
	}

}
