package common.utils;

import java.io.File;
import java.util.Optional;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author VTPlusAKnauer
 */
public class FileChooser {

	public static Optional<File> choose(String description, String extension) {
		JFileChooser chooser = createChooser(description, extension);
		File file = null;
		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			file = chooser.getSelectedFile();
			setPreferedFolder(description, file.getParent());
		}
		return Optional.ofNullable(file);
	}
	
	public static File[] chooseMultiple(String description, String extension) {
		JFileChooser chooser = createChooser(description, extension);
		chooser.setMultiSelectionEnabled(true);
		chooser.showOpenDialog(null);
		File[] files = chooser.getSelectedFiles();
		if (files.length > 0)
			setPreferedFolder(description, files[0].getParent());
					
		return files;
	}

	private static JFileChooser createChooser(String description, String extension) {
		JFileChooser chooser = new JFileChooser();
		String folder = getPreferedFolder(description);
		chooser.setCurrentDirectory(new File(folder));
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setFileFilter(new FileFilter() {
			@Override
			public String getDescription() {
				return description;
			}

			@Override
			public boolean accept(File file) {
				return file.isDirectory() || file.getName().endsWith(extension);
			}
		});

		return chooser;
	}	
	
	private static String getPreferedFolder(String description) {
		return getPreferences().get(description, System.getProperty("user.dir"));
	}
	
	private static void setPreferedFolder(String description, String folder) {
		getPreferences().put(description, folder); // remember this directory
	}
	
	private static Preferences getPreferences() {
		return Preferences.userRoot().node(FileChooser.class.getName());
	}
	
}
