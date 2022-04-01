/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architect.processors.rrassigner;

import static architect.Constants.EPSILON;
import architect.math.Rectangle;
import architect.room.Room;
import architect.room.RoomRect;
import architect.utils.FloatComparable;
import static java.lang.Math.*;
import java.util.*;

/**
 *
 * @author VTPlusAKnauer
 */
public class RoomWithClaim {

	public final Room room;
	private float totalClaimedArea = 0;

	private final HashSet<RoomRectClaim> preparedToClaim = new HashSet<>();
	private final HashMap<RoomRectClaim, Float> claimValueMap = new HashMap<>();

	public RoomWithClaim(Room room) {
		this.room = room;
	}

	public float totalClaimedArea() {
		return totalClaimedArea;
	}

	public Set<RoomRectClaim> claimedRoomRects() {
		return Collections.unmodifiableSet(claimValueMap.keySet());
	}

	public void prepareToClaim(RoomRectClaim rrc) {
		totalClaimedArea += rrc.rr.area();
		preparedToClaim.add(rrc);
	}

	public void takeClaimedRoomRect(RoomRectClaim claimedRR, Set<RoomRectClaim> claimableRoomRects) {
		room.addSubRect(claimedRR.rr);

		prepareToClaimNeighbors(claimedRR, claimableRoomRects);

		HashSet<RoomWithClaim> claimers = new HashSet<>(claimedRR.claimers());
		claimers.stream().forEach(roomWithClaim -> roomWithClaim.removeClaimFor(claimedRR));
		claimers.stream().forEach(roomWithClaim -> roomWithClaim.updateAllClaims());
	}

	private void prepareToClaimNeighbors(RoomRectClaim claimedRR, Set<RoomRectClaim> claimableRoomRects) {
		claimableRoomRects.stream().filter(claimableRR -> claimableRR.rr.isNeighborWith(claimedRR.rr))
				.forEach(neighbor -> {
					preparedToClaim.add(neighbor);
					totalClaimedArea += neighbor.rr.area();
				});
	}

	private void removeClaimFor(RoomRectClaim claimedRR) {
		claimedRR.removeClaimer(new FloatComparable<>(this, claimValueMap.get(claimedRR)));
		claimValueMap.remove(claimedRR);
		totalClaimedArea -= claimedRR.rr.area();
	}

	public void updateAllClaims() {
		for (RoomRectClaim cRR : preparedToClaim) {
			claim(cRR);
		}
		preparedToClaim.clear();

		HashSet<RoomRectClaim> sett = new HashSet(claimValueMap.keySet());

		for (RoomRectClaim cRR : sett) {
			cRR.removeClaimer(new FloatComparable<>(this, claimValueMap.get(cRR)));
			claim(cRR);
		}
	}

	public void claim(RoomRectClaim claimedRR) {
		float claimValue = claimValueFor(claimedRR.rr);
		claimValueMap.put(claimedRR, claimValue);
		claimedRR.addClaimer(this, claimValue);
	}

	/**
	 * Computes the claim value a room has on the given RR. This depends on room area need, perimeter overlap and the
	 * shape the room will have, if it takes this RR.
	 *
	 * @param room
	 * @param totalClaimedArea amount of area this room is currently claiming (all RoomRectClaims summed up)
	 * @param areaToNeedRatio current area / areaNeed value, the higher this value the less the claim score
	 * @param roomBoundingRect the bounding rectangle of the room, the less of a protrusion the RR will be the higher
	 * the score
	 * @return
	 */
	private float claimValueFor(RoomRect rr) {
		Rectangle roomBoundingRect = room.boundingRect();

		Rectangle scaledBoundingRect = roomBoundingRect.scaledToContain(rr);
		// if both height and width increased -> there is likely a double protrusion (2 inner corners) (0 is max, 1 is min protrusion)
		float protrusion = 1.0f - min((scaledBoundingRect.width - roomBoundingRect.width) / rr.width,
									  (scaledBoundingRect.height - roomBoundingRect.height) / rr.height);

		float areaFill = 1.0f + roomBoundingRect.overlapArea(rr) / rr.area(); // the more this rr fits into the bounding rect, the better
		float newBoundngRectRatio = roomBoundingRect.scaledToContain(rr).inverseAspectRatio(); // the closer this ratio is to 1 the better
		float claimedAreaRatio = (float) sqrt(rr.area() / totalClaimedArea);

		float newBoundingRectArea = scaledBoundingRect.area() - roomBoundingRect.area();
		float windowLitFactor = 1.0f; // completely lit by a window (not behind a corner) 0.5 is lowest value
		if (room.stats().needsWindow && rr.windowablePerimeterSum < EPSILON && newBoundingRectArea > rr.area() * 0.5f) { // could be hidden from light behind corner
			windowLitFactor = (room.maxWindowableOverlapRatio(rr) + 0.5f) / 1.5f;
		}

		float commonWallOverlap = 0;
		for (RoomRect neighbor : rr.neighbors()) {
			if (neighbor.isAssignedTo(room))
				commonWallOverlap += rr.commonPerimeter(neighbor);
		}
		float wallOverlapRatio = (float) pow(commonWallOverlap / (rr.width + rr.height), 2);

		float claimValue = (EPSILON * 2) + (float) (protrusion * wallOverlapRatio * newBoundngRectRatio * areaFill
													* windowLitFactor * claimedAreaRatio / pow(room.areaToNeedRatio(), 2));

		return claimValue;
	}

	@Override
	public int hashCode() {
		return room.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final RoomWithClaim other = (RoomWithClaim) obj;
		if (!Objects.equals(this.room, other.room))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(room.name()).append("-> ");
		String prefix = "";
		for (Map.Entry<RoomRectClaim, Float> entry : claimValueMap.entrySet()) {
			builder.append(prefix).append(entry.getValue()).append('\'').append(entry.getKey().rr.id);
			prefix = ", ";
		}
		return builder.toString();
	}
}
