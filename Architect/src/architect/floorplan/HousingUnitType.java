/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architect.floorplan;

import architect.room.RoomType;

/**
 *
 * @author Arthur
 */
public enum HousingUnitType {
	Hallway,
	Apartment,
	Office,
	Shop,
	ApartmentComplex;

	public static HousingUnitType fromRoomType(RoomType roomType) {
		switch (roomType) {
			case Apartment:
				return HousingUnitType.Apartment;
			case Hallway:
				return HousingUnitType.Hallway;
			default:
				throw new IllegalArgumentException("Cannot convert " + roomType + " to a HousingUnitType");
		}
	}

}
