/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architect.utils;

import java.util.EnumSet;

/**
 *
 * @author AK47
 * @param <E> Enum to use as flag set
 */
public abstract class FlagSet<E extends Enum<E>> {

	private final EnumSet<E> flags;

	public FlagSet(Class<E> enumClass) {
		flags = EnumSet.noneOf(enumClass);
	}
	
	public void clear() {
		flags.clear();
	}
	
	public boolean contains(E flag) {
		return flags.contains(flag);
	}

	public void add(E flag) {
		flags.add(flag);
	}

	public void addAll(FlagSet<E> other) {
		this.flags.addAll(other.flags);
	}

	public void remove(E flag) {
		flags.remove(flag);
	}
}
