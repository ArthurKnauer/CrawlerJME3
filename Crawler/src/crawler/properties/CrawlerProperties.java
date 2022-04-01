package crawler.properties;

import common.properties.Properties;
import java.util.ResourceBundle;
import lombok.experimental.UtilityClass;

/**
 *
 * @author VTPlusAKnauer
 */
@UtilityClass
public class CrawlerProperties {

	public static final Properties PROPERTIES = new Properties(ResourceBundle.getBundle("crawler.crawler"));
}
