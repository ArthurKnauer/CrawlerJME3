/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package skybox;

import common.lua.LuaCompiler;
import common.utils.FileChangeWatcher;
import java.nio.file.Paths;
import java.util.Calendar;
import lombok.Builder;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import skybox.atmosphere.AtmosphereShader;

/**
 *
 * @author VTPlusAKnauer
 */
class Configurator {

	private static final String CALENDAR_FUNCTION = "setupCalendar";
	private static final String ATMOSPHERE_FUNCTION = "setupAtmosphereShader";

	private final FileChangeWatcher scriptChangeWatcher;

	private final String scriptPath;
	private final AtmosphereShader atmosphereShader;
	private final Calendar calendar;

	@Builder
	private Configurator(String scriptPath,
						 AtmosphereShader atmosphereShader,
						 Calendar calendar) {
		this.scriptPath = scriptPath;
		this.atmosphereShader = atmosphereShader;
		this.calendar = calendar;

		scriptChangeWatcher = new FileChangeWatcher(Paths.get(scriptPath), 1.0f);
	}

	boolean hasScriptChanged(float tpf) {
		return scriptChangeWatcher.hasChanged(tpf);
	}

	void compileAndRunScript() {
		LuaCompiler.compileScript(scriptPath).ifPresent(this::runScript);
	}

	private void runScript(LuaValue script) {
		LuaFunction calendarFunction = (LuaFunction) script.get(CALENDAR_FUNCTION);
		LuaFunction atmosphereFunction = (LuaFunction) script.get(ATMOSPHERE_FUNCTION);

		calendarFunction.call(CoerceJavaToLua.coerce(calendar));
		atmosphereFunction.call(CoerceJavaToLua.coerce(atmosphereShader));
	}
}
