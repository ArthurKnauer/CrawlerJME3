/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architect;

import architect.floorplan.FloorPlan;
import architect.floorplan.FloorPlanScriptSource;
import architect.floorplan.HousingUnitType;
import java.util.EnumMap;
import java.util.Random;

/**
 *
 * @author AK
 */
public class BuildingArchitect {
	
	private FloorArchitect floorArchitect;

	public BuildingArchitect(Random rand, String scriptFolder) {
		
		Architect apartmentArchitect = ArchitectBuilder.fromScript(scriptFolder + "/apartment.lua");
		Architect hallwayArchitect = ArchitectBuilder.fromScript(scriptFolder + "/hallway.lua");
		FloorPlanScriptSource complexPlanSource = new FloorPlanScriptSource(scriptFolder + "/apartment_complex_floorplan.lua");

		EnumMap<HousingUnitType, Architect> subArchitects = new EnumMap<>(HousingUnitType.class);
		subArchitects.put(HousingUnitType.Apartment, apartmentArchitect);
		subArchitects.put(HousingUnitType.Hallway, hallwayArchitect);
		
		Architect architectForComplex = ArchitectBuilder.fromScript(scriptFolder + "/apartment_complex.lua");
		FloorPlan complexFloorPlan = complexPlanSource.getFloorPlan();
		architectForComplex.runAllProcessors(rand, complexFloorPlan);
		
		floorArchitect = new FloorArchitect(subArchitects, complexFloorPlan);
	}

	public FloorPlan getComplexPlan() {
		return floorArchitect.getComplexPlan();
	}

	public Iterable<FloorPlan> getSubFloorPlans() {
		return floorArchitect.getSubFloorPlans();
	}
	
	public Architect getCurrentArchitect() {
		return floorArchitect.getCurrentArchitect();
	}

	public void nextStep(Random rand) {
		floorArchitect.nextStep(rand);
	}

	public void buildAll(Random rand) {
		floorArchitect.buildAll(rand);
	}

	public void nextSubFloorPlan(Random rand) {
		floorArchitect.buildCurrentFloorPlan(rand);
	}
	
	
}
