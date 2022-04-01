/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package architect.processors;

import architect.room.Room;
import architect.room.RoomStats;
import architect.room.RoomType;
import architect.utils.FloatComparable;
import architect.utils.FloatComparator;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import java.util.Collection;
import java.util.PriorityQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;

/**
 *
 * @author VTPlusAKnauer
 */
public class RoomTypeAssigner extends FloorPlanProcessor {

	private final static Logger LOGGER = getLogger(RoomTypeAssigner.class.getName());

	public RoomTypeAssigner() {
	}

	@Override
	protected void process() {
		PriorityQueue<FloatComparable<RoomType>> roomTypesToBuildQueue = sortRoomsToBuildIntoQueue(fp.roomsToBuild.list());
		PriorityQueue<FloatComparable<Room>> protoRoomAvailableQueue = sortAvailableProtoRoomsIntoQueue(fp.rooms);

		assignRoomTypesToProtoRooms(roomTypesToBuildQueue, protoRoomAvailableQueue);

		if (!protoRoomAvailableQueue.isEmpty())
			LOGGER.log(Level.WARNING, "Didn''t find room types for proto rooms: {0}", protoRoomAvailableQueue);
		if (!protoRoomAvailableQueue.isEmpty())
			LOGGER.log(Level.WARNING, "Didn''t find proto rooms for roomsToBuild: {0}", roomTypesToBuildQueue);
	}

	private PriorityQueue<FloatComparable<RoomType>> sortRoomsToBuildIntoQueue(Collection<RoomType> roomsToBuild) {
		return FloatComparator.createPriorityQueue(roomsToBuild,
												   roomType -> roomToBuildPriorityScore(fp.roomStats.get(roomType)));
	}

	private PriorityQueue<FloatComparable<Room>> sortAvailableProtoRoomsIntoQueue(Collection<Room> protoRooms) {
		return FloatComparator.createPriorityQueue(
				protoRooms.stream().filter(room -> room.type() == RoomType.None),
				room -> protoRoomPriorityScore(room));
	}

	private void assignRoomTypesToProtoRooms(PriorityQueue<FloatComparable<RoomType>> roomTypesToBuildQueue,
											 PriorityQueue<FloatComparable<Room>> protoRoomAvailableQueue) {
		while (!roomTypesToBuildQueue.isEmpty() && !protoRoomAvailableQueue.isEmpty()) {
			RoomType roomType = roomTypesToBuildQueue.poll().obj;
			Room protoRoom = protoRoomAvailableQueue.poll().obj;
			protoRoom.setType(roomType);
			protoRoom.setStats(fp.roomStats.get(roomType));
		}
	}

	private float protoRoomPriorityScore(Room room) {
		return (float) (sqrt(room.neighbors().size()) * room.area() * pow(room.windowablePerimeterSum(), 0.2f));
	}

	private float roomToBuildPriorityScore(RoomStats roomStats) {
		float publicFactor = roomStats.isPublic ? 1.5f : 1.0f;
		float windowedFactor = roomStats.needsWindow ? 1.5f : 1.0f;
		return roomStats.needsArea * publicFactor * windowedFactor;
	}
}
