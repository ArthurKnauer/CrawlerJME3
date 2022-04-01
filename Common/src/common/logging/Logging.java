/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common.logging;

import java.util.Arrays;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.experimental.UtilityClass;

/**
 *
 * @author VTPlusAKnauer
 */
@UtilityClass
public class Logging {

	public static void setGlobalLevel(Level level) {
		Arrays.stream(Logger.getLogger("").getHandlers())
				.filter(handler -> handler instanceof ConsoleHandler)
				.forEach(handler -> handler.setLevel(level));
	}
}
