/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common.properties;

import java.util.ResourceBundle;
import lombok.experimental.UtilityClass;

/**
 *
 * @author VTPlusAKnauer
 */
@UtilityClass
public class CommonProperties {

	public static final Properties PROPERTIES = new Properties(ResourceBundle.getBundle("common.common"));
}
