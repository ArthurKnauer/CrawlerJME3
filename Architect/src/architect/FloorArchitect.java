/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architect;

import architect.floorplan.FloorPlan;
import architect.floorplan.FloorPlanBuilder;
import architect.floorplan.HousingUnitType;
import architect.room.Room;
import java.util.*;
import lombok.Getter;

/**
 *
 * @author Arthur
 */
public class FloorArchitect {

	private final Map<HousingUnitType, Architect> subArchitects;
	@Getter private final FloorPlan complexPlan;

	@Getter private final ArrayList<FloorPlan> subFloorPlans = new ArrayList<>();

	private final Iterator<FloorPlan> currentSubFloorPlanIt;
	@Getter private FloorPlan currentSubFloorPlan;

	public FloorArchitect(Map<HousingUnitType, Architect> subArchitects, FloorPlan complexPlan) {
		this.subArchitects = subArchitects;
		this.complexPlan = complexPlan;

		for (Room room : complexPlan.rooms) {
			subFloorPlans.add(FloorPlanBuilder.fromPoly(room));
		}

		currentSubFloorPlanIt = subFloorPlans.iterator();		
		currentSubFloorPlan = currentSubFloorPlanIt.next();
		System.out.println("next " + currentSubFloorPlan.id);
	}

	public void buildAll(Random rand) {
		getCurrentArchitect().runAllProcessors(rand, currentSubFloorPlan);
		while (currentSubFloorPlanIt.hasNext()) {
			currentSubFloorPlan = currentSubFloorPlanIt.next();
			System.out.println("next " + currentSubFloorPlan.id);
			getCurrentArchitect().runAllProcessors(rand, currentSubFloorPlan);
		}
	}

	public void buildCurrentFloorPlan(Random rand) {
		getCurrentArchitect().runAllProcessors(rand, currentSubFloorPlan);

		if (currentSubFloorPlanIt.hasNext()) {
			currentSubFloorPlan = currentSubFloorPlanIt.next();
			System.out.println("next " + currentSubFloorPlan.id);
		}
	}
  
	public Architect getCurrentArchitect() {
		return subArchitects.get(getCurrentSubFloorPlan().getHousingType());
	}

	public void nextStep(Random rand) {
		Architect currentArchitect = getCurrentArchitect();
		if (!currentArchitect.runNextProcessor(rand, currentSubFloorPlan)
			&& currentSubFloorPlanIt.hasNext()) {
			currentSubFloorPlan = currentSubFloorPlanIt.next();
			System.out.println("next " + currentSubFloorPlan.id);
		}
	}

}
