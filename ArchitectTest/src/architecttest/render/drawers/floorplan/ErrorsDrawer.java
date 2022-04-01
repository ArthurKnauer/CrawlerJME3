/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architecttest.render.drawers.floorplan;

import architect.floorplan.FloorPlan;
import architect.math.segments.Side;
import architect.room.Room;
import architect.room.RoomRect;
import architecttest.analyze.Validator;
import architecttest.analyze.Validator.BadRelation;
import static architecttest.render.Shapes.*;
import architecttest.render.text.GLText;
import architecttest.render.text.Text;
import static architecttest.render.utils.Color.Red;
import static java.lang.Math.sin;
import static java.lang.System.currentTimeMillis;
import static org.lwjgl.opengl.GL11.glColor4f;

/**
 *
 * @author AK47
 */
class ErrorsDrawer extends Drawer {

	private String missingNeighbors, shouldBeNeighbors, shouldNotBeNeighbors, missingRRs,
			assignedToWrongRoom, roomsShouldBeNeighbors, roomsShouldNotBeNeighbors, unsnappedSides;

	@Override
	public void drawForFloorPlan(FloorPlan foorPlan) {
		missingNeighbors = "";
		shouldBeNeighbors = "";
		shouldNotBeNeighbors = "";
		missingRRs = "";
		assignedToWrongRoom = "";
		roomsShouldBeNeighbors = "";
		roomsShouldNotBeNeighbors = "";
		unsnappedSides = "";

		float arrowSize = GLText.getScale() / 50.0f;

		if (Validator.findErrors(foorPlan)) {
			for (BadRelation<RoomRect, RoomRect> badRelation : Validator.rrMissingRRNeighbor) {
				glColor4f(1, 0, 0, 0.75f);
				drawRectStriped(badRelation.b);
				drawArrow(badRelation.a.centerPoint(), badRelation.b.centerPoint(), arrowSize, 0.1f);
				missingNeighbors += "(" + badRelation.a.id + ", " + badRelation.b.id + "), ";
			}

			for (BadRelation<RoomRect, RoomRect> badRelation : Validator.rrsShouldNotBeNeighbors) {
				glColor4f(1, 0, 0, (float) sin(currentTimeMillis() * 0.01));
				drawRectFilled(badRelation.b);
				drawArrow(badRelation.a.centerPoint(), badRelation.b.centerPoint(), arrowSize, 0.1f);

				shouldNotBeNeighbors += "(" + badRelation.a.id + ", " + badRelation.b.id + "), ";
			}

			for (BadRelation<RoomRect, RoomRect> badRelation : Validator.rrsShouldBeNeighbors) {
				glColor4f(0, 0, 1, (float) sin(currentTimeMillis() * 0.01));
				drawRectFilled(badRelation.b);
				drawArrow(badRelation.a.centerPoint(), badRelation.b.centerPoint(), arrowSize, 0.1f);
				shouldBeNeighbors += "(" + badRelation.a.id + ", " + badRelation.b.id + "), ";
			}

			for (BadRelation<Room, RoomRect> badRelation : Validator.roomMissingRR) {
				glColor4f(1, 0.5f, 0, (float) sin(currentTimeMillis() * 0.005 + badRelation.b.id));
				drawRectFilled(badRelation.b);
				missingRRs += "(" + badRelation.a.name() + ", " + badRelation.b.id + "), ";
			}

			for (BadRelation<Room, RoomRect> badRelation : Validator.rrAssignedToWrongRoom) {
				glColor4f(0.5f, 1.0f, 0, (float) sin(currentTimeMillis() * 0.005 + badRelation.b.id));
				drawRectFilled(badRelation.b);
				assignedToWrongRoom += "(" + badRelation.a.name() + ", " + badRelation.b.id + "), ";
			}

			for (BadRelation<Room, Room> badRelation : Validator.roomsShouldBeNeighbors) {
				roomsShouldBeNeighbors += "(" + badRelation.a.name() + ", " + badRelation.b.name() + "), ";
			}

			for (BadRelation<Room, Room> badRelation : Validator.roomsShouldNotBeNeighbors) {
				roomsShouldNotBeNeighbors += "(" + badRelation.a.name() + ", " + badRelation.b.name() + "), ";
			}

			for (BadRelation<RoomRect, Side> badRelation : Validator.unsnappedSides) {
				unsnappedSides += "(" + badRelation.a.id + ", " + badRelation.b + "), ";
			}
		}
	}

	public void printErrors(FloorPlan floorPlan) {
		if (Validator.findErrors(floorPlan)) {
			if (missingNeighbors.length() > 0)
				Text.print(Red, "missingNeighbors: " + missingNeighbors);
			if (shouldBeNeighbors.length() > 0)
				Text.print(Red, "shouldBeNeighbors: " + shouldBeNeighbors);
			if (shouldNotBeNeighbors.length() > 0)
				Text.print(Red, "shouldNotBeNeighbors: " + shouldNotBeNeighbors);
			if (missingRRs.length() > 0)
				Text.print(Red, "missingRRs: " + missingRRs);
			if (assignedToWrongRoom.length() > 0)
				Text.print(Red, "assignedToWrongRoom: " + assignedToWrongRoom);
			if (roomsShouldBeNeighbors.length() > 0)
				Text.print(Red, "roomsShouldBeNeighbors: " + roomsShouldBeNeighbors);
			if (roomsShouldNotBeNeighbors.length() > 0)
				Text.print(Red, "roomsShouldNotBeNeighbors: " + roomsShouldNotBeNeighbors);
			if (unsnappedSides.length() > 0)
				Text.print(Red, "unsnappedSides: " + unsnappedSides);

			Text.skipLines(1);
		}
	}

}
