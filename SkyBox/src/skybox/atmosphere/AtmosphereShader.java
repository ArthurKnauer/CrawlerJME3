package skybox.atmosphere;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;

/**
 *
 * @author VTPlusAKnauer
 */
public final class AtmosphereShader {

	private final Material shader;

	AtmosphereShader(AssetManager assetManager) {
		shader = new Material(assetManager, "MatDefs/Atmosphere/Atmosphere.j3md");

		setFluxScale(2);

		setRadiusEarth(6360e3f);
		setRadiusAtmosphereSquared(425104e8f);
		setRayleighScaleHeight(7994);
		setMieScaleHeight(1200);
		setSunIntensity(15);
		setMieGFactor(0.76f);
		setRayleighWavelengths(new Vector3f(5.5e-6f, 13.0e-6f, 22.4e-6f));
		setMieWavelengths(new Vector3f(21e-6f, 21e-6f, 21e-6f));
		setSunDirection(Vector3f.UNIT_Y);
		setSamples(8);
		setLightSamples(4);
	}

	public void setFluxScale(float fluxScale) {
		shader.setFloat("FluxScale", fluxScale);
	}

	public void setSunDirection(Vector3f direction) {
		shader.setVector3("SunDirection", direction.normalizeLocal());
	}

	public void setSamples(int samples) {
		shader.setInt("Samples", samples);
	}

	public void setLightSamples(int samples) {
		shader.setInt("LightSamples", samples);
	}

	public void setRadiusEarth(float radius) {
		shader.setFloat("RadiusEarth", radius);
	}

	public void setRadiusAtmosphereSquared(float radiusSquared) {
		shader.setFloat("RadiusAtmosphereSquared", radiusSquared);
	}

	public void setRayleighScaleHeight(float scale) {
		shader.setFloat("RayleighScaleHeight", scale);
	}

	public void setMieScaleHeight(float scale) {
		shader.setFloat("MieScaleHeight", scale);
	}

	public void setSunIntensity(float intensity) {
		shader.setFloat("SunIntensity", intensity);
	}

	public void setMieGFactor(float factor) {
		shader.setFloat("MieGFactor", factor);
	}

	public void setRayleighWavelengths(Vector3f wavelengths) {
		shader.setVector3("RayleighWavelengths", wavelengths);
	}

	public void setMieWavelengths(Vector3f wavelengths) {
		shader.setVector3("MieWavelengths", wavelengths);
	}

	void setForward(Vector3f forward) {
		shader.setVector3("Forward", forward);
	}

	void setRight(Vector3f right) {
		shader.setVector3("Right", right);
	}

	void setUp(Vector3f up) {
		shader.setVector3("Up", up);
	}

	void render(Geometry renderRect, RenderManager renderManager) {
		shader.render(renderRect, renderManager);
	}

}
