package crawler.main;

import com.jme3.system.AppSettings;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import jlibs.core.util.logging.AnsiFormatter;
import org.lwjgl.opengl.Display;

/**
 *
 * @author VTPlusAKnauer
 */
public class Main {
	
	public static void main(String[] args) {
		// setup logging first
		Logger logger = LogManager.getLogManager().getLogger("");
		logger.setLevel(Level.INFO);
		Handler handler = logger.getHandlers()[0];
		handler.setLevel(Level.FINEST);
		handler.setFormatter(new AnsiFormatter());

		// setup and start application
		CrawlerApp app = new CrawlerApp();
		AppSettings settings = new AppSettings(true);
		settings.setResolution(1600, 900);
		//settings.setFullscreen(true);
		app.setShowSettings(false); // no splashscreen
		app.setSettings(settings);
		app.start();
		
		Display.setLocation(160, 0);
	}
	
}