package modelcompiler.io;

import com.jme3.asset.AssetManager;
import com.jme3.export.binary.BinaryExporter;
import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import lombok.experimental.UtilityClass;
import modelcompiler.main.ModelCompiler;

/**
 *
 * @author VTPlusAKnauer
 */
@UtilityClass
public class ModelExporter {
	
	private final static String FILE_SUFFIX = ".mesh.j3o";
	
	public static void export(Spatial model, AssetManager assetManager) {
		if (model != null) {
			JFileChooser chooser = new JFileChooser();
			Preferences prefs = Preferences.userRoot().node(ModelCompiler.class.getName()); // recall last directory
			String folder = prefs.get("lastUsedSaveFolder", System.getProperty("user.dir"));
			chooser.setCurrentDirectory(new File(folder));
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			String meshName = model.getKey().getName().replaceAll("\\..*", "");
			chooser.setSelectedFile(new File(meshName));

			chooser.setFileFilter(new FileFilter() {
				@Override
				public String getDescription() {
					return "j3o Mesh (" + FILE_SUFFIX + ")";
				}

				@Override
				public boolean accept(File file) {
					return file.isDirectory() || file.getName().endsWith(".j3o");
				}
			});

			if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
				prefs.put("lastUsedSaveFolder", chooser.getSelectedFile().getParent()); // remember this directory
				try {
					// don't save material with the mesh
					Material defaultMat = ((Geometry)((Node)model).getChild(0)).getMaterial();
					model.setMaterial(new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md"));
					BinaryExporter exporter = BinaryExporter.getInstance();
					exporter.save(model, withSuffix(chooser.getSelectedFile()));
					model.setMaterial(defaultMat);	 // set material back	
				} catch (IOException ex) {
					JOptionPane.showMessageDialog(null, "Couldn't save model:\n" + ex,
												  "File error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}
	
	private static File withSuffix(File file) {
		if (!file.getAbsolutePath().endsWith(FILE_SUFFIX))
			return new File(file + FILE_SUFFIX);
		else 
			return file;
	}
	
}
