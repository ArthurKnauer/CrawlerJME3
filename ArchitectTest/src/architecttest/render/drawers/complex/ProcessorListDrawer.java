/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architecttest.render.drawers.complex;

import architect.Architect;
import architect.processors.FloorPlanProcessor;
import architect.processors.rrassigner.RoomRectAssigner;
import architecttest.render.reporters.RoomRectAssignerReporter;
import architecttest.render.text.Text;
import static architecttest.render.text.Text.skipLines;
import architecttest.render.utils.Color;
import static architecttest.render.utils.Color.*;

/**
 *
 * @author AK47
 */
class ProcessorListDrawer extends Drawer {

	@Override
	protected void drawForCurrentArchitect(Architect currentArchitect) {
		Text.setCaret(-0.9f, 0.9f);
		printProcessorList(currentArchitect);
		skipLines(1);
		printCurrentProcessorReport(currentArchitect);
	}

	private void printProcessorList(Architect architect) {
		int p = 0;
		for (String name : architect.getProcessorNameList()) {
			Color color = (p + 1 == architect.getCurrentStep()) ? LightGreen : LightGrey;
			Text.print(color, name);
			p++;
		}
	}

	private void printCurrentProcessorReport(Architect architect) {
		FloorPlanProcessor currentProcessor = architect.getCurrentProcessor();
		if (currentProcessor != null) {
			if (currentProcessor.getClass() == RoomRectAssigner.class)
				Text.printList(GreenYellowish, RoomRectAssignerReporter.report((RoomRectAssigner) currentProcessor));
		}
	}

}
