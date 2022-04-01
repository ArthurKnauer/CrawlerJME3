/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architecttest.render.utils;

import architect.math.Rectangle;
import java.util.Optional;

/**
 *
 * @author VTPlusAKnauer
 */
public class RectangleParser {

	private RectangleParser() {

	}

	public static Optional<Rectangle> parse(String text) {
		text = text.trim();
		try {
			float[] coordinates = cleanAndParseCoordinates(text);

			if (text.charAt(0) == '[')  // parse "[minX, maxX]x, [minY, maxY]y"
				return Optional.of(Rectangle.fromMinMax(coordinates[0], coordinates[2], coordinates[1], coordinates[3]));
			else //  parse "minX, minY, maxX, maxY"
				return Optional.of(Rectangle.fromMinMax(coordinates[0], coordinates[1], coordinates[2], coordinates[3]));

		} catch (IllegalArgumentException ex) {
			System.err.println(ex);
			return Optional.empty();
		}
	}

	private static float[] cleanAndParseCoordinates(String text) throws NumberFormatException {
		float coordinates[] = new float[4];

		text = text.replaceAll("\\[|\\]|x|y|,", " ").trim();
		String[] coordinatesAsString = text.split("\\s+");

		if (coordinatesAsString.length == 4) {
			coordinates[0] = Float.parseFloat(coordinatesAsString[0]);
			coordinates[1] = Float.parseFloat(coordinatesAsString[1]);
			coordinates[2] = Float.parseFloat(coordinatesAsString[2]);
			coordinates[3] = Float.parseFloat(coordinatesAsString[3]);
		}
		else
			throw new NumberFormatException("wrong amount of numbers");

		return coordinates;
	}
}
