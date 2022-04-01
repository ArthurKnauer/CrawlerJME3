/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architecttest.render.drawers.complex;

import architecttest.input.KeyboardInput;
import architecttest.render.text.GLText;
import architecttest.render.text.Text;
import static architecttest.render.utils.Color.*;
import static org.lwjgl.opengl.GL11.glLineWidth;

/**
 *
 * @author AK47
 */
public class ComplexRenderer {

	private final GridDrawer grid = new GridDrawer();

	private final ImportantRectsDrawer importantRects = new ImportantRectsDrawer();
	private final SelectedRoomAttribsDrawer selectedRoomAttribs = new SelectedRoomAttribsDrawer();
	private final SelectedRoomChildrenDrawer selectedRoomChildren = new SelectedRoomChildrenDrawer();
	private final SelectedRoomRectNeighborsDrawer selectedRoomRectNeighbors = new SelectedRoomRectNeighborsDrawer();
	private final SelectedRoomDetailsDrawer selectedRoomDetails = new SelectedRoomDetailsDrawer();

	private final MainInfoDrawer textMain = new MainInfoDrawer();
	private final ProcessorListDrawer textProcessorList = new ProcessorListDrawer();
	private final SelectedInfoDrawer textSelectedInfo = new SelectedInfoDrawer();
	private final KeyBindsDrawer textKeyBinds = new KeyBindsDrawer();

	public ComplexRenderer(KeyboardInput keyboardInput) {
		grid.disable();

		//importantRects.disable();
		selectedRoomAttribs.disable();
		selectedRoomChildren.disable();
		selectedRoomRectNeighbors.disable();
		//selectedRoomDetails.disable();

		textSelectedInfo.disable();
		textKeyBinds.disable();

		keyboardInput.bindMethod("Grid", grid::toggle);

		keyboardInput.bindMethod("ImportantRects", importantRects::toggle);
		keyboardInput.bindMethod("SelectedRoomAttribs", selectedRoomAttribs::toggle);
		keyboardInput.bindMethod("SelectedRoomChildren", selectedRoomChildren::toggle);
		keyboardInput.bindMethod("SelectedRoomRectNeighbors", selectedRoomRectNeighbors::toggle);
		keyboardInput.bindMethod("SelectedRoomDetails", selectedRoomDetails::toggle);

		keyboardInput.bindMethod("ProcessorList", textProcessorList::toggle);
		keyboardInput.bindMethod("SelectedInfo", textSelectedInfo::toggle);
		keyboardInput.bindMethod("KeyBinds", textKeyBinds::toggle);
	}

	public void draw(DrawerInfo info) {
		glLineWidth(1);

		grid.draw(info);

		importantRects.draw(info);
		selectedRoomAttribs.draw(info);
		selectedRoomChildren.draw(info);
		selectedRoomRectNeighbors.draw(info);
		selectedRoomDetails.draw(info);
	}

	public void drawText(DrawerInfo info) {
		GLText.setScale(0.6f);
		GLText.beginForScreen();

		textMain.draw(info);
		textProcessorList.draw(info);
		textSelectedInfo.draw(info);
		textKeyBinds.draw(info);

		drawToggles();

		GLText.end();
	}

	public void drawToggles() {
		StringBuilder enabledToggles = new StringBuilder();

		if (importantRects.isEnabled())
			enabledToggles.append("ImportantRects ");
		if (selectedRoomAttribs.isEnabled())
			enabledToggles.append("RoomAttribs ");
		if (selectedRoomChildren.isEnabled())
			enabledToggles.append("RoomChildren ");
		if (selectedRoomRectNeighbors.isEnabled())
			enabledToggles.append("RRNeighbors ");
		if (selectedRoomDetails.isEnabled())
			enabledToggles.append("RoomDetails ");
		if (textSelectedInfo.isEnabled())
			enabledToggles.append("SelectedInfo ");

		Text.setCaretBottom(-0.9f);
		Text.print(LightBlue, enabledToggles.toString());
	}

}
