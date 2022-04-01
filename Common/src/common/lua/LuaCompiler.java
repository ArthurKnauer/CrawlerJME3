package common.lua;

import java.util.Optional;
import java.util.logging.Level;
import lombok.experimental.UtilityClass;
import lombok.extern.java.Log;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

/**
 *
 * @author VTPlusAKnauer
 */
@Log
@UtilityClass
public class LuaCompiler {

	private static final LuaValue GLOBALS = JsePlatform.standardGlobals();

	public static Optional<LuaFunction> compileFunction(String scriptPath, String functionName) {
		return compileScript(scriptPath).map(script -> (LuaFunction) script.get(functionName));
	}

	public static Optional<LuaValue> compileScript(String scriptPath) {
		try {
			GLOBALS.get("dofile").call(LuaValue.valueOf(scriptPath));
			return Optional.of(GLOBALS);
		} catch (LuaError luaError) {
			String line = LuaErrorLine.lineFromLuaError(scriptPath, luaError);
			log.log(Level.SEVERE, scriptPath + " error in line: " + line, luaError);
		} catch (Exception ex) {
			log.log(Level.SEVERE, scriptPath + " file error", ex);
		}

		return Optional.empty();
	}

}
