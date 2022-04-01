package architect.snapgrid;

import architect.Constants;
import static architect.Constants.EPSILON;
import architect.math.Rectangle;
import static java.lang.Math.abs;
import java.util.ArrayList;
import java.util.TreeSet;
import lombok.Getter;

public class SnapGrid {

	@Getter private final TreeSet<Float> xSnapLines;
	@Getter private final TreeSet<Float> ySnapLines;

	private final float minSnapLineDist;

	public SnapGrid(float minSnapLineDist) {
		xSnapLines = new TreeSet<>();
		ySnapLines = new TreeSet<>();
		this.minSnapLineDist = minSnapLineDist;
	}

	public float snapX(float x) {
		return snapX(x, null, SnapChoice.CLOSEST);
	}

	public float snapY(float y) {
		return snapY(y, null, SnapChoice.CLOSEST);
	}

	public float snapX(float x, Rectangle notToIntersect) {
		return snapX(x, notToIntersect, SnapChoice.CLOSEST);
	}

	public float snapY(float y, Rectangle notToIntersect) {
		return snapY(y, notToIntersect, SnapChoice.CLOSEST);
	}

	public float snapX(float x, SnapChoice snapChoice) {
		return snapX(x, null, snapChoice);
	}

	public float snapY(float y, SnapChoice snapChoice) {
		return snapY(y, null, snapChoice);
	}

	public float snapX(float x, Rectangle notToIntersect, SnapChoice snapChoice) {
		float result = x;
		Float nearSnapLineR = xSnapLines.ceiling(result);
		Float nearSnapLineL = xSnapLines.floor(result);

		if (nearSnapLineR != null && snapChoice == SnapChoice.LESS_EQUAL
			&& nearSnapLineR > x + EPSILON && abs(nearSnapLineR - result) < minSnapLineDist) {
			result -= minSnapLineDist;
			nearSnapLineR = xSnapLines.ceiling(result);
			nearSnapLineL = xSnapLines.floor(result);
		}
		else if (nearSnapLineL != null && snapChoice == SnapChoice.GREATER_EQUAL
				 && nearSnapLineL < x - EPSILON && abs(nearSnapLineL - result) < minSnapLineDist) {
			result += minSnapLineDist;
			nearSnapLineR = xSnapLines.ceiling(result);
			nearSnapLineL = xSnapLines.floor(result);
		}

		if (notToIntersect != null) { // dont use snap lines that are intersecting this rect
			ArrayList<Float> snapLinesToAvoid = new ArrayList<>();
			while (nearSnapLineR != null && notToIntersect.intersectsX(nearSnapLineR) && !notToIntersect.touchesX(nearSnapLineR)) { // snap line overlaps rect
				snapLinesToAvoid.add(nearSnapLineR);
				nearSnapLineR = xSnapLines.ceiling(nearSnapLineR + minSnapLineDist / 2); // find right snap line
			}
			while (nearSnapLineL != null && notToIntersect.intersectsX(nearSnapLineL) && !notToIntersect.touchesX(nearSnapLineL)) { // snap line overlaps rect
				snapLinesToAvoid.add(nearSnapLineL);
				nearSnapLineL = xSnapLines.floor(nearSnapLineL - minSnapLineDist / 2); // find left snap line
			}

			if (notToIntersect.intersectsX(result) && !notToIntersect.touchesX(result)) { // move result outside of that rect
				result = x > notToIntersect.centerX() ? notToIntersect.maxX : notToIntersect.minX;
			}

			// we cant be too close to snap lines that are inside the notToIntersect rect
			for (float snapLineToAvoid : snapLinesToAvoid) {
				if (abs(result - snapLineToAvoid) < minSnapLineDist) {
					if (result > snapLineToAvoid)
						result = snapLineToAvoid + minSnapLineDist;
					else
						result = snapLineToAvoid - minSnapLineDist;
					break;
				}
			}
		}

		if (nearSnapLineL != null && abs(nearSnapLineL - result) < minSnapLineDist)
			if (nearSnapLineR != null && abs(nearSnapLineR - result) < abs(nearSnapLineL - result))
				result = nearSnapLineR;
			else
				result = nearSnapLineL;
		else if (nearSnapLineR != null && abs(nearSnapLineR - result) < minSnapLineDist)
			result = nearSnapLineR;
		else {// did not snap
			xSnapLines.add(result);
			if (Constants.DEV_MODE)
				validateSnaplines(xSnapLines);
		}

		return result;
	}

	public float snapY(float y, Rectangle notToIntersect, SnapChoice snapChoice) {
		float result = y;
		Float nearSnapLineT = ySnapLines.ceiling(result);
		Float nearSnapLineD = ySnapLines.floor(result);

		if (nearSnapLineT != null && snapChoice == SnapChoice.LESS_EQUAL
			&& nearSnapLineT > y + EPSILON && abs(nearSnapLineT - result) < minSnapLineDist) {
			result -= minSnapLineDist;
			nearSnapLineT = ySnapLines.ceiling(result);
			nearSnapLineD = ySnapLines.floor(result);
		}
		else if (nearSnapLineD != null && snapChoice == SnapChoice.GREATER_EQUAL
				 && nearSnapLineD < y - EPSILON && abs(nearSnapLineD - result) < minSnapLineDist) {
			result += minSnapLineDist;
			nearSnapLineT = ySnapLines.ceiling(result);
			nearSnapLineD = ySnapLines.floor(result);
		}

		ArrayList<Float> snapLinesToAvoid = new ArrayList<>();
		if (notToIntersect != null) { // dont use snap lines that are intersecting this rect
			while (nearSnapLineT != null && notToIntersect.intersectsY(nearSnapLineT) && !notToIntersect.touchesY(nearSnapLineT)) { // snap line overlaps rect
				snapLinesToAvoid.add(nearSnapLineT);
				nearSnapLineT = ySnapLines.ceiling(nearSnapLineT + minSnapLineDist / 2); // find upper snap line
			}
			while (nearSnapLineD != null && notToIntersect.intersectsY(nearSnapLineD) && !notToIntersect.touchesY(nearSnapLineD)) { // snap line overlaps rect
				snapLinesToAvoid.add(nearSnapLineD);
				nearSnapLineD = ySnapLines.floor(nearSnapLineD - minSnapLineDist / 2); // find lower snap line
			}

			if (notToIntersect.intersectsY(result) && !notToIntersect.touchesY(result)) { // move result outside of that rect
				result = y > notToIntersect.centerY() ? notToIntersect.maxY : notToIntersect.minY;
			}

			// we cant be too close to snap lines that are inside the notToIntersect rect
			for (float snapLineToAvoid : snapLinesToAvoid) {
				if (abs(result - snapLineToAvoid) < minSnapLineDist) {
					if (result > snapLineToAvoid)
						result = snapLineToAvoid + minSnapLineDist;
					else
						result = snapLineToAvoid - minSnapLineDist;
					break;
				}
			}
		}

		if (nearSnapLineD != null && abs(nearSnapLineD - result) < minSnapLineDist)
			if (nearSnapLineT != null && abs(nearSnapLineT - result) < abs(nearSnapLineD - result))
				result = nearSnapLineT;
			else
				result = nearSnapLineD;
		else if (nearSnapLineT != null && abs(nearSnapLineT - result) < minSnapLineDist)
			result = nearSnapLineT;
		else {// did not snap
			ySnapLines.add(result);
			if (Constants.DEV_MODE)
				validateSnaplines(ySnapLines);
		}

		return result;
	}

	public Rectangle snap(Rectangle rect, SnapRule rule) {
		float minX, minY, maxX, maxY;

		if (rule == SnapRule.DONT_SHRINK_WIDTH || rule == SnapRule.DONT_SHRINK_AT_ALL) {
			minX = snapX(rect.minX, SnapChoice.LESS_EQUAL);
			maxX = snapX(rect.maxX, SnapChoice.GREATER_EQUAL);
		}
		else {
			minX = snapX(rect.minX, SnapChoice.CLOSEST);
			maxX = snapX(rect.maxX, SnapChoice.CLOSEST);
		}

		if (rule == SnapRule.DONT_SHRINK_HEIGHT || rule == SnapRule.DONT_SHRINK_AT_ALL) {
			minY = snapY(rect.minY, SnapChoice.LESS_EQUAL);
			maxY = snapY(rect.maxY, SnapChoice.GREATER_EQUAL);
		}
		else {
			minY = snapY(rect.minY, SnapChoice.CLOSEST);
			maxY = snapY(rect.maxY, SnapChoice.CLOSEST);
		}

		if (abs(minX - rect.minX) < Constants.EPSILON
			&& abs(minY - rect.minY) < Constants.EPSILON
			&& abs(maxX - rect.maxX) < Constants.EPSILON
			&& abs(maxY - rect.maxY) < Constants.EPSILON) { // already snapped
			return rect;
		}

		return new Rectangle(minX, minY, maxX - minX, maxY - minY);
	}

	public boolean isXSnapped(float x) {
		Float ceilingKey = xSnapLines.ceiling(x);
		if (ceilingKey != null && abs(ceilingKey - x) < EPSILON)
			return true;

		Float floorKey = xSnapLines.floor(x);
		return (floorKey != null && abs(floorKey - x) < EPSILON);
	}

	public boolean isYSnapped(float y) {
		Float ceilingKey = ySnapLines.ceiling(y);
		if (ceilingKey != null && abs(ceilingKey - y) < EPSILON)
			return true;

		Float floorKey = ySnapLines.floor(y);
		return (floorKey != null && abs(floorKey - y) < EPSILON);
	}

	public int getXSnapIndex(float x) {
		int index = 0;
		for (Float key : xSnapLines) {
			if (abs(key - x) < minSnapLineDist)
				return index;
			else
				index++;
		}
		throw new RuntimeException("No x snap key found for " + x + " in " + xSnapLines);
	}

	public int getYSnapIndex(float y) {
		int index = 0;
		for (Float key : ySnapLines) {
			if (abs(key - y) < minSnapLineDist)
				return index;
			else
				index++;
		}
		throw new RuntimeException("No y snap key found for " + y + " in " + ySnapLines);
	}

	private void validateSnaplines(TreeSet<Float> snapLines) {
		float previous = snapLines.last();
		if (snapLines.size() > 1) {
			for (float value : snapLines) {
				float dist = abs(previous - value);
				if (dist < minSnapLineDist - EPSILON)
					throw new RuntimeException("snaplines " + previous + " and " + value + " are too close: " + dist);
				previous = value;
			}
		}
	}

}
