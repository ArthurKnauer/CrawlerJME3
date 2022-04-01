package common.properties;

import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

/**
 *
 * @author VTPlusAKnauer
 */
public class Properties {

	private final ResourceBundle propertiesFile;

	public Properties(ResourceBundle propertiesFile) {
		this.propertiesFile = propertiesFile;
	}		
		
	public boolean getBoolean(String key) {
		return "true".equals(getString(key).toLowerCase());
	}

	public int getInt(String key) {
		return Integer.parseInt(getString(key));
	}

	public int getIntGreaterZero(String key) {
		int value = Integer.parseInt(getString(key));
		if (value <= 0) {
			throw new IllegalPropertyValueException(key + " is not positive: " + value);
		}
		return value;
	}
	
	public float getFloat(String key) {
		return Float.parseFloat(getString(key));
	}

	public String getString(String key) {
		return propertiesFile.getString(key);
	}
	
	public List<String> getKeys() {
		return Collections.list(propertiesFile.getKeys());
	}
	
	private static class IllegalPropertyValueException extends IllegalArgumentException {

		private static final long serialVersionUID = 8271687967340373807L;

		IllegalPropertyValueException(String string) {
			super(string);
		}
	}
}
