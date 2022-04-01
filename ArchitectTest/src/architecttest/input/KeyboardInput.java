/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architecttest.input;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.lwjgl.input.Keyboard;

/**
 *
 * @author VTPlusAKnauer
 */
public class KeyboardInput {

	private final Map<Integer, String> binds = new HashMap<>();
	private final Set<Integer> keyWasPressed = new HashSet<>();

	private final Map<String, Method> commandToMethod = new HashMap<>();
	private final static String VALID_KEY_NAMES
								= IntStream.range(1, 223)
										.mapToObj(Keyboard::getKeyName)
										.filter(Objects::nonNull)
										.collect(Collectors.joining("\n"));

	public KeyboardInput() {

		List<String> commands = InputBinds.PROPERTIES.getKeys();

		for (String command : commands) {
			String keyName = InputBinds.PROPERTIES.getString(command).toUpperCase();
			int keyIndex = Keyboard.getKeyIndex(keyName);
			if (keyIndex == Keyboard.KEY_NONE) {
				throw new IllegalArgumentException(keyName + " is not a valid key\nall valid keys:\n" + VALID_KEY_NAMES);
			}

			binds.put(keyIndex, command);
		}
	}

	public void bindMethod(String name, Method method) {
		commandToMethod.put(name, method);
	}

	public void process() {
		for (Integer keyIndex : binds.keySet()) {
			if (Keyboard.isKeyDown(keyIndex)) {
				if (!keyWasPressed.contains(keyIndex)) {
					commandToMethod.get(binds.get(keyIndex)).execute();
					keyWasPressed.add(keyIndex);
				}
			}
			else {
				keyWasPressed.remove(keyIndex);
			}
		}
	}

}
