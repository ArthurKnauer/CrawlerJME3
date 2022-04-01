/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architect.processors.wallpressure;

import architect.math.segments.Orientation;
import architect.math.Rectangle;
import architect.math.segments.Side;
import architect.utils.ConstructionFlags;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author VTPlusAKnauer
 */
class CutRect {

	@Getter @Setter private Rectangle rect;
	final Orientation orientation;
	final ConstructionFlags flags = new ConstructionFlags();
	final Side ownerEdge;

	CutRect(Rectangle rect, Orientation orientation, Side ownerEdge) {
		this.rect = rect;
		this.orientation = orientation;
		this.ownerEdge = ownerEdge;
	}

	boolean isHorizontal() {
		return orientation == Orientation.Horizontal;
	}

	boolean isVertical() {
		return orientation == Orientation.Vertical;
	}


}
