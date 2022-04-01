/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package architect.processors;

import architect.floorplan.FloorPlan;
import java.util.Random;

/**
 *
 * @author VTPlusAKnauer
 */
public abstract class FloorPlanProcessor {

	protected Random rand;
	protected FloorPlan fp;

	protected abstract void process();

	public void processWhole(Random rand, FloorPlan fp) {
		set(rand, fp);
		process();
	}

	private void set(Random rand, FloorPlan fp) {
		this.rand = rand;
		this.fp = fp;
	}
}
