package modelcompiler.properties;

import common.properties.Properties;
import java.util.ResourceBundle;
import lombok.experimental.UtilityClass;

/**
 *
 * @author VTPlusAKnauer
 */
@UtilityClass
public class ModelCompilerProperties {

	public static final Properties PROPERTIES = new Properties(ResourceBundle.getBundle("modelcompiler.modelcompiler"));
}
