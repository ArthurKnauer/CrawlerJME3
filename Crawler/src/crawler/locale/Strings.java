/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package crawler.locale;

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import lombok.experimental.UtilityClass;
import lombok.extern.java.Log;

/**
 *
 * @author VTPlusAKnauer
 */
@UtilityClass
@Log
public class Strings {

	private static final ResourceBundle strings = ResourceBundle.getBundle("crawler.locale.strings_en_US");

	public static String get(String key) {
		try {
			return strings.getString(key);
		} catch (MissingResourceException ex) {
			log.log(Level.SEVERE, ex.getMessage(), ex);
		}
		return key + "_stringNotFound";
	}

}
