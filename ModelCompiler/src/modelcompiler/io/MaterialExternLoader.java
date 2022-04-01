package modelcompiler.io;

import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.material.Material;
import com.jme3.texture.Texture;
import common.utils.FileChooser;
import common.utils.ScrollableMessageDialog;
import java.io.File;
import java.util.Optional;
import lombok.experimental.UtilityClass;
import modelcompiler.main.Globals;

/**
 *
 * @author VTPlusAKnauer
 */
@UtilityClass
public class MaterialExternLoader {

	public static Optional<Material> load() {
		return materialFromFiles(askForTextureFiles());
	}

	private static File[] askForTextureFiles() {
		return FileChooser.chooseMultiple("DDS Texture (*.dds)", ".dds");
	}

	public static Optional<Material> materialFromFiles(File... files) {
		if (files.length == 0)
			return Optional.empty();

		Material material = null;
		try {
			material = new Material(Globals.getAssetManager(), "MatDefs/LPVShaded.j3md");
			for (File file : files) {
				addTexture(material, file);
			}
		} catch (Exception ex) {
			ScrollableMessageDialog.showException("Couldn't load texture", ex);
		}

		return Optional.ofNullable(material);
	}

	private static void addTexture(Material material, File file) {
		AssetManager assetManager = Globals.getAssetManager();

		assetManager.registerLocator(file.getParent(), FileLocator.class);
		Texture texture = assetManager.loadTexture(new TextureKey(file.getName(), false));
		material.setTexture(isBumpMapFile(file) ? "NormalMap" : "DiffuseMap", texture);
		assetManager.unregisterLocator(file.getParent(), FileLocator.class);
	}

	private static boolean isBumpMapFile(File file) {
		return file.getName().endsWith("_bump.dds");
	}
}
