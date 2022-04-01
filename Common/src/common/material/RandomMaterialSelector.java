package common.material;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.scene.Spatial;
import static common.properties.CommonProperties.PROPERTIES;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import lombok.extern.java.Log;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author VTPlusAKnauer
 */
@Log
public final class RandomMaterialSelector {

	private static final String DIFFUSEMAP_FOLDER = PROPERTIES.getString("RandomMaterialSelector.DIFFUSEMAP_FOLDER");
	private static final String AMBIENT_OCCLUSION_FOLDER = PROPERTIES.getString("RandomMaterialSelector.AMBIENT_OCCLUSION_FOLDER");
	private static final String NORMALMAP_FOLDER = PROPERTIES.getString("RandomMaterialSelector.NORMALMAP_FOLDER");

	private final HashMap<String, List<MaterialDefinition>> definitionMap;

	private final Random random;

	public RandomMaterialSelector(AssetManager assetManager, Random random) {
		this.definitionMap = new HashMap<>();
		this.random = random;

		buildMaterialMapFromFolder();
	}

	public void applyByMeshName(Spatial child) {
		Material material = selectByMeshName(child.getName());
		child.setMaterial(material);

		//String fullName = child.getParent().getName();
		//String modelName = fullName.substring(0, fullName.length() - 9); // remove "-ogremesh" ending
		//String occlusionMapPath = AMBIENT_OCCLUSION_FOLDER + modelName + ".dds";
		//MaterialLoader.addAmbientOcclusion(material, occlusionMapPath);
	}

	private Material selectByMeshName(String categorySet) {
		String[] categories = categorySet.split(",");

		ArrayList<MaterialDefinition> randomFromEachCategory = new ArrayList<>(categories.length);

		for (String category : categories) {
			if (definitionMap.containsKey(category)) {
				List<MaterialDefinition> materialList = definitionMap.get(category);
				randomFromEachCategory.add(materialList.get(random.nextInt(materialList.size())));
			}
		}

		if (randomFromEachCategory.isEmpty()) {
			return MaterialLoader.getDefault();
		}
		else {
			MaterialDefinition randomDefinition = randomFromEachCategory.get(random.nextInt(randomFromEachCategory.size()));
			return MaterialLoader.load(randomDefinition);
		}
	}

	private void buildMaterialMapFromFolder() {
		definitionMap.clear();

		try {
			String encoding = null;
			InputStream diffuseFolderStream = RandomMaterialSelector.class.getClassLoader().getResourceAsStream(DIFFUSEMAP_FOLDER);
			List<String> files = IOUtils.readLines(diffuseFolderStream, encoding);
			for (String fileName : files) {
				if (fileName.endsWith(".dds")) {  // e.g. "wood_fine_1.dds"
					String category = fileName.replaceAll("(_\\d+\\.dds$)", ""); // e.g. wood_fine

					List<MaterialDefinition> definitions = definitionMap.get(category);
					if (definitions == null) {
						definitions = new ArrayList<>();
						definitionMap.put(category, definitions);
					}

					MaterialDefinition definition = MaterialDefinition.builder()
							.diffuseFile(DIFFUSEMAP_FOLDER + fileName)
							.normalFile(NORMALMAP_FOLDER + fileName).build();

					definitions.add(definition);

				}
			}
		} catch (IOException ex) {
			log.log(Level.SEVERE, null, ex);
		}
	}
}
