/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architect.utils;

import java.util.HashMap;

/**
 *
 * @author VTPlusAKnauer
 */
public final class UniqueID {

	private static class ID {

		private int currentID = 0;
		private int currentChar = (int) 'a';

		int nextID() {
			return currentID++;
		}

		char nextChar() {
			return (char) (currentChar++);
		}

		void reset() {
			currentID = 0;
			currentChar = (int) 'a';
		}
	}

	private static final HashMap<Class, ID> uniqueIdByClass = new HashMap<>();

	private UniqueID() {
	}

	public static int nextID(Class type) {
		return getOrCreate(type).nextID();
	}

	public static char nextChar(Class type) {
		return getOrCreate(type).nextChar();
	}

	public static void resetAll() {
		for (ID id : uniqueIdByClass.values()) {
			id.reset();
		}
	}

	private static ID getOrCreate(Class type) {
		ID id = uniqueIdByClass.get(type);
		if (id == null) {
			id = new ID();
			uniqueIdByClass.put(type, id);
		}

		return id;
	}
}
