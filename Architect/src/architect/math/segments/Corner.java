/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architect.math.segments;

import static architect.math.segments.Side.*;

/**
 *
 * @author VTPlusAKnauer
 */
public class Corner {

	public static final Corner[] ALL = {new Corner(Left, Bottom),
										new Corner(Left, Top),
										new Corner(Right, Top),
										new Corner(Right, Bottom)};

	public final Side verticalSide;
	public final Side horizontalSide;

	private Corner(Side verticalSide, Side horizontalSide) {
		this.verticalSide = verticalSide;
		this.horizontalSide = horizontalSide;
		if (verticalSide.isTopOrBottom || horizontalSide.isLeftOrRight)
			throw new RuntimeException("Invalid Corner sides, verticalSide: " + verticalSide
									   + ", horizontalSide: " + horizontalSide);
	}

	@Override
	public String toString() {
		return verticalSide + " " + horizontalSide;
	}
}
