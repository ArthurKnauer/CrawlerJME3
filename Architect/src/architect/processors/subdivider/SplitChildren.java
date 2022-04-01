/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architect.processors.subdivider;

import architect.math.Rectangle;

/**
 *
 * @author VTPlusAKnauer
 */
public class SplitChildren {

	public final Rectangle childA, childB;

	public SplitChildren(Rectangle childA, Rectangle childB) {
		this.childA = childA;
		this.childB = childB;
	}

	public Rectangle rectOverlapping(Rectangle toOverlap) {
		return childA.overlaps(toOverlap) ? childA : childB;
	}

	public Rectangle rectNotOverlapping(Rectangle toOverlap) {
		return childA.overlaps(toOverlap) ? childA : childB;
	}

	Rectangle otherThan(Rectangle toCompare) {
		return childB == toCompare ? childA : childB;
	}
}
