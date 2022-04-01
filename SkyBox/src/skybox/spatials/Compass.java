/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package skybox.spatials;

import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture;
import lombok.Getter;

/**
 *
 * @author VTPlusAKnauer
 */
class Compass extends RendersItself {
	
	@Getter private final Geometry[] geometries = new Geometry[1];

	Compass(AssetManager assetManager) {
		Spatial spatial = assetManager.loadModel("Models/compass.mesh.j3o");

		Material shader = new Material(assetManager, "MatDefs/SkyBox/SkyBox.j3md");
		Texture texture = assetManager.loadTexture(new TextureKey("Textures/compass.dds", false));
		shader.setTexture("DiffuseMap", texture);
		spatial.setMaterial(shader);
		
		geometries[0] = (Geometry)((Node)spatial).getChild(0);
	}
}
