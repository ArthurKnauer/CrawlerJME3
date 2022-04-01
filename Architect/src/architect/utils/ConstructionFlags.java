/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architect.utils;

import architect.utils.ConstructionFlags.Flag;

/**
 *
 * @author AK47
 */
public class ConstructionFlags extends FlagSet<Flag> {

	public ConstructionFlags() {
		super(Flag.class);
	}

	@SuppressWarnings("PublicInnerClass")
	public enum Flag {

		DELETED,
		PROCESSED,
		NOT_CUT,
		DONT_CUT,
	}
}
