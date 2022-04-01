/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architect.processors.rrassigner;

import architect.room.RoomRect;
import architect.utils.FloatComparable;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 *
 * @author VTPlusAKnauer
 */
public class RoomRectClaim {

	public final RoomRect rr;
	private final TreeSet<FloatComparable<RoomWithClaim>> claimers = new TreeSet<>();

	public RoomRectClaim(RoomRect rr) {
		this.rr = rr;
	}

	public Set<RoomWithClaim> claimers() {
		return claimers.stream()
				.map(comparableRoomWithClaim -> comparableRoomWithClaim.obj)
				.collect(Collectors.toSet());
	}

	public void addClaimer(RoomWithClaim claimer, float claimValue) {
		claimers.add(new FloatComparable<>(claimer, claimValue));
	}

	public void removeClaimer(FloatComparable<RoomWithClaim> claimer) {
		if (claimers.remove(claimer) == false)
			throw new RuntimeException("Couldn't remove " + claimer + " from " + this);
	}

	public TwoHighestClaimers twoHighestClaimers() {
		if (claimers.size() > 1) {
			Iterator<FloatComparable<RoomWithClaim>> descendingIterator = claimers.descendingIterator();
			return new TwoHighestClaimers(descendingIterator.next(), descendingIterator.next());
		}
		else if (claimers.size() == 1)
			return new TwoHighestClaimers(claimers.last(), null);
		else
			return new TwoHighestClaimers(null, null);
	}

	@Override
	public int hashCode() {
		return rr.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final RoomRectClaim other = (RoomRectClaim) obj;
		if (!Objects.equals(this.rr, other.rr))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(rr.id).append("-> ");
		String prefix = "";
		for (FloatComparable<RoomWithClaim> claimer : claimers) {
			builder.append(prefix).append(claimer.obj.room.name());
			prefix = ", ";
		}
		return builder.toString();
	}
}
