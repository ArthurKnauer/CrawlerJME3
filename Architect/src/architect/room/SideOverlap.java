/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architect.room;

import architect.math.segments.Side;
import lombok.Data;

/**
 *
 * @author AK47
 */
@Data
public class SideOverlap {

	private final Side side;
	private final float overlap;
}
