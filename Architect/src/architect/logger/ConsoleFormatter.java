package architect.logger;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

class ConsoleFormatter extends Formatter {

	// This method is called for every log records
	@Override
	public String format(LogRecord rec) {
		return formatMessage(rec) + ", seed: " + "TODO ADD SEED" + "\n";
	}
}
