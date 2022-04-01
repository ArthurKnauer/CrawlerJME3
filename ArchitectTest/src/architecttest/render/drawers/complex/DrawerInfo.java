/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architecttest.render.drawers.complex;

import architect.Architect;
import architect.FloorArchitect;
import architect.floorplan.FloorPlan;
import architect.math.Vector2D;
import architect.room.Room;
import architect.room.RoomRect;
import java.util.ArrayList;
import java.util.Optional;

/**
 *
 * @author AK47
 */
public class DrawerInfo {

	private final FloorPlan complexFloorPlan;
	private final Architect currentArchitect;
	private final Vector2D mousePoint;
	private final Room selectedRoom;
	private final RoomRect selectedRoomRect;

	public Optional<FloorPlan> getComplexFloorPlan() {
		return Optional.ofNullable(complexFloorPlan);
	}

	public Optional<Architect> getCurrentArchitect() {
		return Optional.ofNullable(currentArchitect);
	}

	public Optional<Vector2D> getMousePoint() {
		return Optional.ofNullable(mousePoint);
	}

	public Optional<Room> getSelectedRoom() {
		return Optional.ofNullable(selectedRoom);
	}

	public Optional<RoomRect> getSelectedRoomRect() {
		return Optional.ofNullable(selectedRoomRect);
	}

	public static DrawerInfo build(Iterable<FloorPlan> subFloorPlans, FloorPlan complexPlan,  Architect currentArchitect, Vector2D mousePoint) {
		Room selectedRoom = null;
		RoomRect selectedRoomRect = null;
	
		for (FloorPlan fp : subFloorPlans) {
			for (RoomRect rr : fp.roomRects) {
				if (rr.contains(mousePoint.x, mousePoint.y)) {
					selectedRoomRect = rr;
					selectedRoom = rr.assignedTo().orElse(null);
					break;
				}
			}
		}

		if (selectedRoom != null && selectedRoomRect == null)
			throw new IllegalArgumentException("if selectedRoom is not null, selectedRoomRect cannot be null too");

		return new DrawerInfo(complexPlan,
							  currentArchitect,
							  mousePoint,
							  selectedRoom,
							  selectedRoomRect);
	}

	private DrawerInfo(FloorPlan complexFloorPlan, Architect currentArchitect, Vector2D mousePoint, Room selectedRoom, RoomRect selectedRoomRect) {
		this.complexFloorPlan = complexFloorPlan;
		this.currentArchitect = currentArchitect;
		this.mousePoint = mousePoint;
		this.selectedRoom = selectedRoom;
		this.selectedRoomRect = selectedRoomRect;
	}
}
