/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common.material;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import lombok.experimental.UtilityClass;

/**
 *
 * @author VTPlusAKnauer
 */
@UtilityClass
public class DefaultMaterial {

	private static AssetManager assetManager = null;

	public static void setAssetManager(AssetManager assetManager) {
		DefaultMaterial.assetManager = assetManager;
	}

	public static Material withColor(Vector3f color) {
		if (assetManager == null) {
			throw new IllegalStateException("AssetManager was not set");
		}

		Material material = new Material(assetManager, "MatDefs/Default/Default.j3md");
		material.setVector3("Color", color);
		return material;
	}
}
