package skybox.spatials;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import lombok.Getter;
import skybox.atmosphere.AtmosphereRenderer;

/**
 *
 * @author VTPlusAKnauer
 */
class AtmosphereSkybox extends RendersItself {
	
	@Getter private final Geometry[] geometries = new Geometry[2];

	AtmosphereSkybox(AssetManager assetManager, AtmosphereRenderer atmosphereRenderer) {
		Material shaderUp = new Material(assetManager, "MatDefs/SkyBox/SkyBox.j3md");
		shaderUp.setTexture("DiffuseMap", atmosphereRenderer.getTextureUp());
		
		Material shaderSides = new Material(assetManager, "MatDefs/SkyBox/SkyBox.j3md");
		shaderSides.setTexture("DiffuseMap", atmosphereRenderer.getTextureSides());

		Spatial spatial = assetManager.loadModel("Models/skybox.mesh.j3o");
	
		geometries[0] = (Geometry)((Node)spatial).getChild(0);
		geometries[1] = (Geometry)((Node)spatial).getChild(1);
		
		geometries[0].setMaterial(shaderUp);
		geometries[1].setMaterial(shaderSides);
	}
}
