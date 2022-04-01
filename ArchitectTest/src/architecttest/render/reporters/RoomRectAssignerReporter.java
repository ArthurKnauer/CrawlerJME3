/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architecttest.render.reporters;

import architect.processors.rrassigner.RoomRectAssigner;
import architect.processors.rrassigner.RoomRectClaim;
import architect.processors.rrassigner.RoomWithClaim;
import architect.utils.FloatComparable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

/**
 *
 * @author VTPlusAKnauer
 */
public class RoomRectAssignerReporter {

	public static List<String> report(RoomRectAssigner rrAssigner) {
		ArrayList<String> output = new ArrayList<>();

		output.add("---- roomQueue ---- ");
		addRoomQueueInfo(output, rrAssigner);
		output.add("---- claimableRoomRects ---- ");
		addClaimableRoomRectsInfo(output, rrAssigner);

		return output;
	}

	private static void addRoomQueueInfo(List<String> output, RoomRectAssigner rrAssigner) {
		PriorityQueue<FloatComparable<RoomWithClaim>> roomQueue = (PriorityQueue<FloatComparable<RoomWithClaim>>) getField(rrAssigner, "roomQueue");
		if (roomQueue != null)
			roomQueue.stream().sorted().forEach(comparableRWC -> {
				output.add(comparableRWC.value() + ": " + comparableRWC.obj.room.name() + "-> "
						   + comparableRWC.obj.claimedRoomRects());
			});
	}

	private static void addClaimableRoomRectsInfo(List<String> output, RoomRectAssigner rrAssigner) {
		Set<RoomRectClaim> claimableRoomRects = (Set<RoomRectClaim>) getField(rrAssigner, "claimableRoomRects");
		StringBuilder builder = new StringBuilder();
		String prefix = "";
		for (RoomRectClaim crr : claimableRoomRects) {
			builder.append(prefix).append(crr.rr.id);
			prefix = ", ";
		}
		output.add(builder.toString());
	}

	private static Object getField(RoomRectAssigner rrAssigner, String fieldName) {
		try {
			Field field = RoomRectAssigner.class.getDeclaredField(fieldName);
			field.setAccessible(true);
			return field.get(rrAssigner);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
			throw new RuntimeException(ex);
		}
	}
}
