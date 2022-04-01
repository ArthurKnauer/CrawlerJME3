package common.utils;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.logging.Level;
import lombok.extern.java.Log;

/**
 *
 * @author VTPlusAKnauer
 */
@Log
public class FileChangeWatcher {

	private final float timeBetweenChecks;
	private float timePassedSinceCheck;

	private WatchService watcher;
	private final Path fileName;

	public FileChangeWatcher(Path filePath, float timeBetweenChecks) {
		this.fileName = filePath.getFileName();
		this.timeBetweenChecks = timeBetweenChecks;
		this.timePassedSinceCheck = 0;

		try {
			watcher = FileSystems.getDefault().newWatchService();
			Path folder = filePath.getParent();
			folder.register(watcher, ENTRY_MODIFY);
		} catch (IOException ex) {
			log.log(Level.SEVERE, null, ex);
		}
	}

	public boolean hasChanged(float timeSinceLastCall) {
		timePassedSinceCheck += timeSinceLastCall;
		if (timePassedSinceCheck > timeBetweenChecks) {
			timePassedSinceCheck = 0;

			return hasChanged();
		}
		return false;
	}

	private boolean hasChanged() {
		WatchKey key = watcher.poll();
		if (key == null) {
			return false;
		}

		for (WatchEvent<?> event : key.pollEvents()) {
			if (event.kind() == ENTRY_MODIFY) {
				Path changedFile = ((WatchEvent<Path>) event).context();

				if (changedFile.equals(fileName)) {
					key.reset();
					return true;
				}
			}
		}

		key.reset();
		return false;
	}

}
