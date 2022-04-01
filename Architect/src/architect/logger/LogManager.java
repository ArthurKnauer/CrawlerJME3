package architect.logger;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import lombok.experimental.UtilityClass;

@UtilityClass
public class LogManager {

	private static FileHandler fileHTML;
	public final static DecimalFormat decimalFormat = new DecimalFormat("###.###");

	static public void setup() throws IOException {
		// Get the global logger to configure it
		Logger logger = getLogger("");
		logger.setLevel(Level.ALL);

		// change console output
		Handler consoleHandler = logger.getHandlers()[0];
		consoleHandler.setFormatter(new ConsoleFormatter());
		consoleHandler.setLevel(Level.OFF);

		// create HTML Formatter
		/*fileHTML = new FileHandler("logs/architect_log." + 
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yy_HH-mm-ss")) 
                        + ".html");
		fileHTML.setFormatter(new HtmlFormatter());
		logger.addHandler(fileHTML);*/

		DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
		otherSymbols.setDecimalSeparator('.');
		otherSymbols.setGroupingSeparator(',');
		decimalFormat.setDecimalFormatSymbols(otherSymbols);
	}
}
