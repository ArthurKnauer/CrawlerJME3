/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architect.room;

import architect.math.Vector2D;

/**
 *
 * @author VTPlusAKnauer
 */
public class RoomPerimeter {

	public final Room room;
	public final Vector2D perimeter;

	public RoomPerimeter(Room room, Vector2D perimeter) {
		this.room = room;
		this.perimeter = perimeter;
	}
}
