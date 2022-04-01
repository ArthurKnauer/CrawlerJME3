/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architect.processors.rrassigner;

import architect.utils.FloatComparable;

/**
 *
 * @author VTPlusAKnauer
 */
public class TwoHighestClaimers {

	public final FloatComparable<RoomWithClaim> highestClaimer;
	public final FloatComparable<RoomWithClaim> secondHighestClaimer;

	public TwoHighestClaimers(FloatComparable<RoomWithClaim> highestClaimer,
							  FloatComparable<RoomWithClaim> seondHighestClaimer) {
		if (highestClaimer == null && seondHighestClaimer != null)
			throw new IllegalArgumentException("highestClaimer can't be null if seondHighestClaimer is not null");
		this.highestClaimer = highestClaimer;
		this.secondHighestClaimer = seondHighestClaimer;
	}
}
