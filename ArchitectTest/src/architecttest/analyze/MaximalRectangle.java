/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architecttest.analyze;

import java.util.LinkedList;
import java.util.Stack;

final class Cell {

	final int col;
	final int row;

	Cell(final int col, final int row) {
		this.col = col;
		this.row = row;
	}

	@Override
	public String toString() {
		return String.format("[col=%d, row=%d]", col + 1, row + 1);
	}
}

final class Cache {

	private final LinkedList<Integer> aggregateHeights;

	Cache(final int size) {
		aggregateHeights = new LinkedList<>();
		for (int i = 0; i < size; i++) {
			aggregateHeights.add(0);
		}
	}

	public int get(final int col) {
		return aggregateHeights.get(col);
	}

	public void aggregate(final String row) {
		final String[] elements = row.split("\\s+");
		for (int col = 0; col < elements.length; col++) {
			final String element = elements[col];
			if ("0".equals(element)) {
				aggregateHeights.set(col, 0);
			}
			else {
				aggregateHeights.set(col, aggregateHeights.get(col) + 1);
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(aggregateHeights.size());
		for (Integer height : aggregateHeights) {
			builder.append(height);
		}
		return builder.toString();
	}
}

/**
 * @see http://stackoverflow.com/a/20039017/1028367
 * @see http://www.drdobbs.com/database/the-maximal-rectangle-problem/184410529
 */
public class MaximalRectangle {

	public static void main(final String... args) throws Exception {
		int bestArea = 0;
		Cell bestLowerLeftCorner = new Cell(0, 0);
		Cell bestUpperRightCorner = new Cell(-1, -1);

		final int numColumns = 4;
		final int numRows = 4;

		final Stack<Cell> stack = new Stack<>();
		final Cache rectangleHeightCache = new Cache(numColumns);

		String[] rows = {"1 0 1 0",
						 "1 1 1 1",
						 "0 0 1 1",
						 "1 1 1 1"};

		for (int row = 0; row < numRows; row++) {
			rectangleHeightCache.aggregate(rows[row]);

			for (int col = 0, currentRectHeight = 0; col <= numColumns; col++) {
				final int aggregateRectHeight = (col < numColumns) ? rectangleHeightCache.get(col) : 0;

				if (aggregateRectHeight > currentRectHeight) {
					stack.push(new Cell(col, currentRectHeight));
					currentRectHeight = aggregateRectHeight;
				}
				else if (aggregateRectHeight < currentRectHeight) {

					Cell rectStartCell;
					do {
						rectStartCell = stack.pop();
						final int rectWidth = col - rectStartCell.col;
						final int area = currentRectHeight * rectWidth;
						if (area > bestArea) {
							bestArea = area;
							bestLowerLeftCorner = new Cell(rectStartCell.col, row);
							bestUpperRightCorner = new Cell(col - 1, row - currentRectHeight + 1);
						}
						currentRectHeight = rectStartCell.row;
					} while (aggregateRectHeight < currentRectHeight);

					currentRectHeight = aggregateRectHeight;
					if (currentRectHeight != 0) {
						stack.push(rectStartCell);
					}
				}
			}
		}

		System.err.printf("The maximal rectangle has area %d.\n", bestArea);
		System.err.printf("Location: %s to %s\n", bestLowerLeftCorner, bestUpperRightCorner);
	}
}
