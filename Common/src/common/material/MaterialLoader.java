package common.material;

import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.math.Matrix3f;
import com.jme3.texture.Texture;
import lombok.experimental.UtilityClass;

/**
 *
 * @author VTPlusAKnauer
 */
@UtilityClass
public class MaterialLoader {

	private static AssetManager assetManager;
	private static Material defaultMaterial;

	public static void init(AssetManager assetManager) {
		MaterialLoader.assetManager = assetManager;
		MaterialLoader.defaultMaterial = new Material(assetManager, "MatDefs/Default/Default.j3md");
	}
	
	public static Material getDefault() {
		return defaultMaterial;
	}
	
	public static Material load(String diffuseFile) {
		MaterialDefinition matDef = MaterialDefinition.builder().diffuseFile(diffuseFile).build();
		return load(matDef);
	}

	public static Material load(String diffuseFile, String normalFile) {
		MaterialDefinition matDef = MaterialDefinition.builder().diffuseFile(diffuseFile).normalFile(normalFile).build();
		return load(matDef);
	}

	public static Material load(MaterialDefinition matDefModel) {
		Material material = new Material(assetManager, "MatDefs/LPVShaded/LPVShaded.j3md");
		Texture diffuse = loadTexture(matDefModel.getDiffuseFile());
		material.setTexture("DiffuseMap", diffuse);
		
		if (matDefModel.getNormalFile() != null) {
			Texture normal = loadTexture(matDefModel.getNormalFile());
			material.setTexture("NormalMap", normal);
		}

		return material;
	}

	public static Material load(MaterialDefStatic matDefStatic) {
		Material material = new Material(assetManager, "MatDefs/LPVShaded/LPVShaded.j3md");
		Texture diffuse = loadTexture(matDefStatic.getDiffuseFile());
		Texture normal = loadTexture(matDefStatic.getNormalFile());
		Texture ambientOcclusion = loadTexture(matDefStatic.getAmbientOcclusionFile());

		material.setTexture("DiffuseMap", diffuse);
		material.setTexture("NormalMap", normal);
		material.setTexture("AmbientOcclusionMap", ambientOcclusion);

		Matrix3f texTransform = new Matrix3f(Matrix3f.IDENTITY);
		texTransform.scale(matDefStatic.getTextureScale());
		material.setMatrix3("TextureTransform", texTransform);

		return material;
	}

	private static Texture loadTexture(String file) {
		Texture tex = assetManager.loadTexture(new TextureKey(file, false));
		tex.setWrap(Texture.WrapMode.Repeat);
		return tex;
	}
}
