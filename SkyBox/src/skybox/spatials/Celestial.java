package skybox.spatials;

import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import static com.jme3.math.FastMath.tan;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.CenteredQuad;
import com.jme3.texture.Texture;
import lombok.Getter;

/**
 * Suns and Moons
 *
 * @author VTPlusAKnauer
 */
class Celestial extends RendersItself {

	@Getter private final Vector3f direction = new Vector3f();

	@Getter private final Geometry[] geometries = new Geometry[1];
	
	private final Material shader;

	Celestial(AssetManager assetManager, Texture texture, float size) {
		shader = new Material(assetManager, "MatDefs/Celestial/Celestial.j3md");
		shader.setTexture("Texture", texture);
		
		geometries[0] = new Geometry("sun", new CenteredQuad(size, size));
		geometries[0].setMaterial(shader);
	}

	void setDirection(Vector3f direction) {
		this.direction.set(direction);

		geometries[0].setLocalTranslation(direction);
		geometries[0].lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
		geometries[0].updateGeometricState();
	}

	void setFluxScale(float scale) {
		shader.setFloat("FluxScale", scale);
	}
}
