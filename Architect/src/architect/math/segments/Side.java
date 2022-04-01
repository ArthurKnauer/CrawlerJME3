/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architect.math.segments;

import static architect.math.segments.Orientation.Vertical;
import java.util.EnumSet;

/**
 *
 * @author VTPlusAKnauer
 */
public enum Side {

	Left(true, true, true),
	Top(false, true, false),
	Right(true, false, false),
	Bottom(false, false, true);

	private Side opposite;
	private Side nextClockwise;
	private Side nextCounterClockwise;

	public final boolean isLeftOrRight, isLeftOrTop, isLeftOrBottom;
	public final boolean isTopOrBottom, isRightOrTop, isRightOrBottom;

	public final static EnumSet<Side> allSides = EnumSet.of(Side.Left, Side.Right, Side.Top, Side.Bottom);

	static {
		Left.opposite = Right;
		Right.opposite = Left;
		Top.opposite = Bottom;
		Bottom.opposite = Top;

		Left.nextClockwise = Top;
		Right.nextClockwise = Bottom;
		Top.nextClockwise = Right;
		Bottom.nextClockwise = Left;

		Left.nextCounterClockwise = Bottom;
		Right.nextCounterClockwise = Top;
		Top.nextCounterClockwise = Left;
		Bottom.nextCounterClockwise = Right;
	}

	Side(boolean isLeftOrRight, boolean isLeftOrTop, boolean isLeftOrBottom) {
		this.isLeftOrRight = isLeftOrRight;
		this.isLeftOrTop = isLeftOrTop;
		this.isLeftOrBottom = isLeftOrBottom;

		isTopOrBottom = !isLeftOrRight;
		isRightOrTop = !isLeftOrBottom;
		isRightOrBottom = !isLeftOrTop;
	}

	public Side opposite() {
		return opposite;
	}

	public Side nextClockwise() {
		return nextClockwise;
	}

	public Side nextCounterClockwise() {
		return nextCounterClockwise;
	}

	public static Side lowSide(Orientation orientation) {
		return orientation == Vertical ? Bottom : Left;
	}

	public static Side highSide(Orientation orientation) {
		return orientation == Vertical ? Top : Right;
	}

	public boolean isOrthogonalTo(Side otherSide) {
		return isLeftOrRight != otherSide.isLeftOrRight;
	}
}
