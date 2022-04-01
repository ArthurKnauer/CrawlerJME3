/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architect.room;

import architect.room.RoomStatus.Flags;
import architect.utils.FlagSet;


/**
 *
 * @author AK47
 */
public class RoomStatus extends FlagSet<Flags> {

	public RoomStatus() {
		super(Flags.class);
	}
	
	
	@SuppressWarnings("PublicInnerClass")
	public static enum Flags {
		Simplified,
		ProtrusionsRemoved,
		WallsCreated
	}
	
}
