/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architect.floorplan;

import architect.room.RoomType;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author VTPlusAKnauer
 */
public class RoomsToBuild {

	private final ArrayList<RoomType> roomsToBuild;

	public static class Builder {

		private final ArrayList<RoomType> roomsToBuild = new ArrayList<>();

		public static Builder start() {
			return new Builder();
		}

		public Builder addRoomType(RoomType roomType) {
			roomsToBuild.add(roomType);
			return this;
		}

		public RoomsToBuild build() {
			return new RoomsToBuild(roomsToBuild);
		}
	}

	private RoomsToBuild(ArrayList<RoomType> roomsToBuild) {
		this.roomsToBuild = roomsToBuild;
	}

	public List<RoomType> list() {
		//TODO: resolve this: another list for already built rooms?
		return roomsToBuild;//Collections.unmodifiableList(roomsToBuild);
	}
}
