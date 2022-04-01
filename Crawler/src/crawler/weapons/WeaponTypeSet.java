package crawler.weapons;

import common.lua.LuaCompiler;
import java.util.HashMap;
import lombok.extern.java.Log;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

/**
 *
 * @author VTPlusAKnauer
 */
@Log
public class WeaponTypeSet extends HashMap<String, WeaponType> {

	private final static String SCRIPT = "assets/lua/weapons.lua";
	private final static String SCRIPT_FUNCTION = "createWeaponSet";
	private static final long serialVersionUID = -674172649024369043L;

	private WeaponTypeSet() {
	}

	public static WeaponTypeSet createFromScript() {
		WeaponTypeSet set = new WeaponTypeSet();
		set.runScript();
		return set;
	}

	private void runScript() {
		LuaCompiler.compileFunction(SCRIPT, SCRIPT_FUNCTION)
				.ifPresent(func -> func.call(CoerceJavaToLua.coerce(this)));
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}
}
