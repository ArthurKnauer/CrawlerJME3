package cityplanner.planners.roads;

import cityplanner.quadtree.Quadtree;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

/**
 *
 * @author VTPlusAKnauer
 */
public class RoadPlanner {

	private final Queue<Road> proposedRoads;
	private final ArrayList<Road> builtRoads;
	private GlobalGoals globalGoals;
	private LocalConstraints localConstraints;

	public RoadPlanner() {
		this.proposedRoads = new ArrayDeque<>();
		this.builtRoads = new ArrayList<>();
	}

	public void setGlobalGoals(GlobalGoals globalGoals) {
		this.globalGoals = globalGoals;
	}

	public void setLocalConstraints(LocalConstraints localConstraints) {
		this.localConstraints = localConstraints;
	}

	public List<Road> plan(Quadtree quadtree, int maxIterations) {
		Road.idCounter = 0;

		proposedRoads.offer(globalGoals.proposeInitialHighway());

		localConstraints.setMaxJoinRadius(400);
		localConstraints.setMinAngle(0.5f);
		localConstraints.setMinDistanceForMinAngle(100);

		int retryBuiltRoadIndex = 0; // index of "feeding" old road, if proposedRoads goes empty
		for (int i = 0; i < maxIterations; i++) {
			if (proposedRoads.isEmpty()) {
				if (retryBuiltRoadIndex >= builtRoads.size())
					break; // no success with old roads either

				globalGoals.proposeHighways(builtRoads.get(retryBuiltRoadIndex), proposedRoads);
				retryBuiltRoadIndex++;
			}

			Road road = proposedRoads.poll();

			Optional<Road> lastAdded = localConstraints.checkAndAdd(road, builtRoads, quadtree);
			if (lastAdded.isPresent()) {
				globalGoals.proposeHighways(lastAdded.get(), proposedRoads);
			}
		}
		proposedRoads.clear(); // finished with highways

		for (Road road : builtRoads) {
			globalGoals.proposeStreets(road, proposedRoads);
		}
		
		localConstraints.setMaxJoinRadius(100);
		localConstraints.setMinAngle(0.25f);
		localConstraints.setMinDistanceForMinAngle(50);

		for (int i = 0; i < maxIterations * 20 && !proposedRoads.isEmpty(); i++) {
			Road street = proposedRoads.poll();

			Optional<Road> lastAdded = localConstraints.checkAndAdd(street, builtRoads, quadtree);
			if (lastAdded.isPresent()) {
				globalGoals.proposeStreets(lastAdded.get(), proposedRoads);
			}
		}

		return builtRoads;
	}
}
