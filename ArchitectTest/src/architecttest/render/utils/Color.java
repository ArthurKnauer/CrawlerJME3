/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architecttest.render.utils;

/**
 *
 * @author VTPlusAKnauer
 */
public enum Color {

	White(1, 1, 1),
	Black(0, 0, 0),
	Red(1, 0, 0),
	Green(0, 1, 0),
	Blue(0, 0, 1),
	Yellow(1, 1, 0),
	Magenta(1, 0, 1),
	Cyan(0, 1, 1),
	LightRed(1, 0.5f, 0.5f),
	LightGreen(0.5f, 1, 0.5f),
	LightBlue(0.5f, 0.5f, 1),
	LightGrey(0.75f, 0.75f, 0.75f),
	Grey(0.5f, 0.5f, 0.5f),
	DarkGrey(0.25f, 0.25f, 0.25f),
	Orange(1, 0.5f, 0),
	Pink(1, 0, 0.5f),
	GreenYellowish(0.5f, 1, 0),
	GreenBluish(0, 1, 0.5f),
	Violet(0.5f, 0, 1),
	BlueGreenish(0, 0.5f, 1);

	public final float red, green, blue;

	Color(float red, float green, float blue) {
		this.red = red;
		this.green = green;
		this.blue = blue;
	}
}
