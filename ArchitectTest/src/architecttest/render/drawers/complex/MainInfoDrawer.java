/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architecttest.render.drawers.complex;

import architect.Architect;
import static architect.Constants.*;
import static architect.logger.LogManager.decimalFormat;
import architect.math.Vector2D;
import architecttest.Test;
import architecttest.render.text.Text;
import static architecttest.render.text.Text.skipLines;
import static architecttest.render.utils.Color.*;

/**
 *
 * @author AK47
 */
class MainInfoDrawer extends Drawer {

	@Override
	protected void drawAlways() {		
		printDevModes(); 
	}
	
	@Override
	protected void drawForMousePoint(Vector2D mousePoint) {
		Text.setCaret(-0.5f, 0.9f);
		Text.print(LightRed, "mouse: " + decimalFormat.format(mousePoint.x) + ", " + decimalFormat.format(mousePoint.y));		
	}

	@Override
	protected void drawForCurrentArchitect(Architect currentArchitect) {
		Text.setCaret(-0.9f, 0.9f);
		Text.print(White, "Seed: " + Test.getSeed());
		Text.print(LightGrey, "Step: " + currentArchitect.getCurrentStep());
	}

	private void printDevModes() {
		Text.setCaret(0.0f, 0.9f);
		StringBuilder devModesSB = new StringBuilder(32);
		if (DEV_MODE)
			devModesSB.append("DevMode, ");
		if (DEV_MODE_ROOMRECTASSIGNER_STEPS)
			devModesSB.append("RRAssignerSteps, ");
		if (DEV_MODE_WALLPRESSURERELAXER_STEPS)
			devModesSB.append("WallPressureRelaxerSteps, ");
		if (DEV_MODE_PROTRUSIONREMOVER_STEPS)
			devModesSB.append("ProtrusionRemoverSteps, ");
		if (DEV_MODE_HALLWAYCREATOR_STEPS)
			devModesSB.append("HallwayCreatorSteps, ");

		if (devModesSB.length() > 0) {
			String devModes = devModesSB.substring(0, devModesSB.lastIndexOf(", "));
			Text.print(Pink, devModes);
		}
	}

}
