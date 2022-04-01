/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architecttest.input;

import common.properties.Properties;
import java.util.ResourceBundle;
import lombok.experimental.UtilityClass;

/**
 *
 * @author AK47
 */
@UtilityClass
public class InputBinds {
	public static final Properties PROPERTIES = new Properties(ResourceBundle.getBundle("architecttest.input.binds"));
}
