package modelcompiler.io;

import com.jme3.asset.AssetManager;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.scene.Spatial;
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
public class ModelImporter {

	public static Optional<Spatial> importSpatial() {
		return askForXMLModelFile().flatMap(file -> modelFromFile(file));
	}
	
	public static Optional<Spatial> openSpatial() {
		return askForJ3OModelFile().flatMap(file -> modelFromFile(file));
	}
	
	private static Optional<File> askForXMLModelFile() {
		return FileChooser.choose("Ogre XML Mesh (*.mesh.xml)", ".mesh.xml");
	}
	
	private static Optional<File> askForJ3OModelFile() {
		return FileChooser.choose("JMonkey J3O Mesh (*.mesh.j3o)", ".mesh.j3o");
	}

	public static Optional<Spatial> modelFromFile(File file) {
		try {
			AssetManager assetManager = Globals.getAssetManager();
			assetManager.registerLocator(file.getParent(), FileLocator.class);
			assetManager.removeModelFromCache(file.getName());
			Spatial model = assetManager.loadModel(file.getName());
			assetManager.unregisterLocator(file.getParent(), FileLocator.class);

			return Optional.of(model);
		} catch (Exception ex) { // if the model doesn't load (parse error or something)
			ex.printStackTrace();
			ScrollableMessageDialog.showException("Couldn't load model", ex);
		}
		return Optional.empty();
	}
}
