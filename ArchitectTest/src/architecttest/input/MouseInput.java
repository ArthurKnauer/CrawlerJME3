/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architecttest.input;

import architect.math.Vector2D;
import architecttest.render.utils.OrbitCamera;
import lombok.Getter;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector3f;

/**
 *
 * @author AK47
 */
public class MouseInput {
	
	@Getter private final Vector2D mouseWorldPosition = new Vector2D();
	private boolean lmbWasDown = false; 
	
	public void process(OrbitCamera camera) {
		int scroll = Mouse.getDWheel();
		if (scroll != 0) {
			camera.zoom(scroll * -0.05f);
		}

		if (Mouse.isButtonDown(0) && Mouse.isButtonDown(1)) {
			//camera.orbit(Mouse.getDX() * 0.01f, Mouse.getDY() * 0.01f);
		}
		else if (Mouse.isButtonDown(2)) {
			camera.zoom(Mouse.getDY() * -0.2f);
		}
		else if (Mouse.isButtonDown(1)) {
			camera.pan(Mouse.getDX() * -0.01f, Mouse.getDY() * -0.01f);
		}
		else if (Mouse.isButtonDown(0)) {
			if (!lmbWasDown) {
				lmbWasDown = true;
				//floorPlanComplex.currentArchitect().runNextProcessor(rand, floorPlan);
			}
		}
		else
			lmbWasDown = false;

		if (Mouse.isInsideWindow()) {
			Vector3f mousePosition3D = camera.castScreenRay(Mouse.getX(), Mouse.getY(), Display.getWidth(), Display.getHeight());
			mouseWorldPosition.set(mousePosition3D.x, mousePosition3D.y);
		}
	}
}
