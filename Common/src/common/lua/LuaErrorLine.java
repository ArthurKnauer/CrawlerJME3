/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common.lua;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;
import lombok.extern.java.Log;
import org.luaj.vm2.LuaError;

/**
 *
 * @author VTPlusAKnauer
 */
@Log
@UtilityClass
class LuaErrorLine {

	static String lineFromLuaError(String scriptPath, LuaError luaError) {
		int lineNumber = lineNumberFromLuaError(luaError);
		if (lineNumber < 0)
			return "Could not parse line number.";
		else
			return "|" + lineNumber + "| " + textAtLineNumber(scriptPath, lineNumber);
	}

	private static int lineNumberFromLuaError(LuaError luaError) {
		// line number should after first ':', e.g. "assets/lua/lpv.lua:4: function arguments expected"
		try {
			Matcher matcher = Pattern.compile("(?<=:)\\d+").matcher(luaError.getMessage());
			matcher.find();
			String lineNumber = matcher.group();
			return Integer.parseInt(lineNumber);
		} catch (IllegalStateException | NumberFormatException ex) {
			// could not parse linenumber -> must be some kind of other error
		}
		
		return 0;
	}

	private static String textAtLineNumber(String scriptPath, int lineNumber) {
		try (Stream<String> lines = Files.lines(Paths.get(scriptPath))) {
			String line = lines.skip(lineNumber - 1).findFirst().get();
			return line;
		} catch (IOException ex) {
			log.log(Level.SEVERE, null, ex);
		}

		return "";
	}
}
