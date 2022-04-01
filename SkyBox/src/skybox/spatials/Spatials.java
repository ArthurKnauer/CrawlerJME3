/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package skybox.spatials;

import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import static com.jme3.math.FastMath.tan;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.texture.Texture;
import skybox.atmosphere.AtmosphereRenderer;

/**
 *
 * @author VTPlusAKnauer
 */
public class Spatials {
	
	private static final float ANGULAR_RADIUS = 0.0098902f; // ~ 34 arc minutes
	private static final float SUN_SIZE = 4 * tan(ANGULAR_RADIUS * 0.5f) * 2.0f; // moon quad is 1 unit away from camera

	private final AtmosphereSkybox atmosphereSkybox;
	private final StarsSkybox starsSkybox;
	private final Celestial sun;
	private final Celestial moon;
	private final Compass compass;

	public Spatials(AssetManager assetManager, AtmosphereRenderer atmosphereRenderer) {
		starsSkybox = new StarsSkybox(assetManager);
		atmosphereSkybox = new AtmosphereSkybox(assetManager, atmosphereRenderer);
		
		Texture sunTexture = assetManager.loadTexture(new TextureKey("Textures/sun.dds", false));
		Texture moonTexture = assetManager.loadTexture(new TextureKey("Textures/moon.dds", false));

		sun = new Celestial(assetManager, sunTexture, SUN_SIZE);
		moon = new Celestial(assetManager, moonTexture, SUN_SIZE);
		
		sun.setFluxScale(100);
		moon.setFluxScale(0.15f);
		
		compass = new Compass(assetManager);
	}

	public void setSunDirection(Vector3f sunDirection) {
		sun.setDirection(sunDirection);
	}

	public void setMoonDirection(Vector3f moonDirection) {
		moon.setDirection(moonDirection);
	}

	public void setStarsRotation(Quaternion starsRotation) {
		starsSkybox.setRotation(starsRotation);
	}

	public void render(RenderManager renderManager) {
		starsSkybox.render(renderManager);
		
		sun.render(renderManager);
		moon.render(renderManager);
		atmosphereSkybox.render(renderManager);
		compass.render(renderManager);
	}
}
