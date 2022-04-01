/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architect;

import architect.floorplan.FloorPlan;
import architect.processors.FloorPlanProcessor;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import lombok.Getter;

/**
 *
 * @author VTPlusAKnauer
 */
public class Architect {

	private final ArrayList<FloorPlanProcessor> processorList;

	@Getter private int currentStep = 0;
	private FloorPlan currentFloorplan = null;

	Architect(ArrayList<FloorPlanProcessor> processorList) {
		this.processorList = processorList;
	}

	public FloorPlanProcessor getCurrentProcessor() {
		if (currentStep < 1)
			return null;
		return processorList.get(currentStep - 1);
	}

	public List<String> getProcessorNameList() {
		return processorList.stream()
				.map(proc -> proc.getClass().getSimpleName())
				.collect(Collectors.toList());
	}			

	public boolean runNextProcessor(Random rand, FloorPlan fp) {		
		if (currentFloorplan != fp) { // new floorplan -> reset steps
			currentFloorplan = fp;
			currentStep = 0;
		}
		
		if (currentStep < processorList.size()) {
			processorList.get(currentStep).processWhole(rand, fp);
			currentStep++;
		}
		
		return currentStep < processorList.size();
	}

	public void runProcessorsUntilStep(Random rand, FloorPlan fp, int step) {
		if (currentFloorplan != fp) { // new floorplan -> reset steps
			currentFloorplan = fp;
			currentStep = 0;
		}
		
		while (currentStep < Math.min(processorList.size(), step)) {
			runNextProcessor(rand, fp);
		}
	}

	public void runAllProcessors(Random rand, FloorPlan fp) {
		runProcessorsUntilStep(rand, fp, processorList.size());
	}
}
