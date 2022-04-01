/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architect.math.segments;

/**
 *
 * @author VTPlusAKnauer
 */
public enum Orientation {

	Horizontal,
	Vertical;

	private Orientation opposite;

	static {
		Horizontal.opposite = Vertical;
		Vertical.opposite = Horizontal;
	}

	public Orientation opposite() {
		return opposite;
	}

}
