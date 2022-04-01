package cityplanner.planners.roads;

import cityplanner.noise.PopulationMap;
import com.jme3.math.Vector2f;
import java.util.Collection;
import java.util.Random;

/**
 *
 * @author VTPlusAKnauer
 */
public class GlobalGoals {

	private final Random random;
	private final PopulationMap populationDensityMap;

	public GlobalGoals(Random random, PopulationMap populationDensityMap) {
		this.random = random;
		this.populationDensityMap = populationDensityMap;
	}

	Road proposeInitialHighway() {
		float length = random.nextFloat() * 1000 + 200; // 200m - 1200m
		Vector2f randomStart = new Vector2f((random.nextFloat() - 0.5f) * 500,
											(random.nextFloat() - 0.5f) * 500);
		Vector2f randomEnd = new Vector2f(randomStart.x + (random.nextFloat() - 0.5f) * 500,
										  randomStart.y + (random.nextFloat() - 0.5f) * 500);

		randomEnd.subtractLocal(randomStart).normalizeLocal().multLocal(length).addLocal(randomStart);

		float width = (random.nextInt(2) + 2) * 3.7f * 2; // 3.7m is witdth of one lane

		Road road = new Road(randomStart, randomEnd, width);
		road.setHighway(true);
		return road;
	}

	void proposeHighways(Road road, Collection<Road> proposedRoads) {
		float randMinAngle = -random.nextFloat() * 0.75f - 1.0f;
		float randMaxAngle = random.nextFloat() * 0.75f + 1.0f;

		Road child = proposeHighwayOffshoot(road.getEnd(), road.getDirection(), road.getWidth(), 500, 1000, randMinAngle, randMaxAngle);
		child.setStartConnected(true);
		child.setHighway(true);
		child.setProposer(road);
		proposedRoads.add(child);

		if (!road.isStartConnected()) {
			randMinAngle = -random.nextFloat() * 0.75f - 1.0f;
			randMaxAngle = random.nextFloat() * 0.75f + 1.0f;

			child = proposeHighwayOffshoot(road.getStart(), road.getDirection().negate(), road.getWidth(), 500, 1000, randMinAngle, randMaxAngle);
			child.setStartConnected(true);
			child.setHighway(true);
			child.setProposer(road);
			proposedRoads.add(child);
		}

		int sideOffshoots = random.nextInt(3);

		for (int i = 0; i < sideOffshoots; i++) {
			randMinAngle = -random.nextFloat() * 0.4f - 0.2f;
			randMaxAngle = random.nextFloat() * 0.4f + 0.2f;

			Vector2f start = road.getDirection().mult(random.nextFloat()).addLocal(road.getStart());
			Vector2f direction = random.nextBoolean() ? road.getRight() : road.getRight().negate();
			child = proposeHighwayOffshoot(start, direction, road.getWidth(), 500, 1000, randMinAngle, randMaxAngle);
			child.setHighway(true);
			child.setProposer(road);
			proposedRoads.add(child);
		}
	}

	public void proposeStreets(Road road, Collection<Road> proposedRoads) {
		float maxStep = road.getLength() - randomStreetStep();

		Vector2f rightSampleOffset = road.getRight().mult(populationDensityMap.getPixelSize());
		Vector2f right = road.getRight().clone();
		Vector2f left = right.negate();
		
		
		if (!road.isHighway()) { // propose street continuation		
			float streetLength = populationDensityMap.getValue(road.getEnd()) * 2000;
			if (streetLength > 100) {
				float angle = random.nextFloat() * 0.8f - 0.4f;
				Vector2f randEndPos = road.getDirection().rotate(angle).multLocal(streetLength).addLocal(road.getEnd());
				Road child = new Road(road.getEnd(), randEndPos, 3.7f * 2);
				child.setHighway(false);
				child.setProposer(road);
				proposedRoads.add(child);
			}
		}

		for (float step = randomStreetStep(); step < maxStep; step += randomStreetStep()) {
			Vector2f currentStepPos = road.getDirection().mult(step).addLocal(road.getStart());

			Vector2f leftSample = currentStepPos.subtract(rightSampleOffset);
			Vector2f rightSample = currentStepPos.add(rightSampleOffset);

			float rightStreetLength = populationDensityMap.getValue(rightSample) * 2000;
			if (rightStreetLength > 100) {
				float angle = random.nextFloat() * 0.4f - 0.2f;
				Vector2f randEndPos = right.rotate(angle).multLocal(rightStreetLength).addLocal(currentStepPos);
				Road child = new Road(currentStepPos, randEndPos, 3.7f * 2);
				child.setHighway(false);
				child.setProposer(road);
				proposedRoads.add(child);
			}

			float leftStreetLength = populationDensityMap.getValue(leftSample) * 2000;
			if (leftStreetLength > 100) {
				float angle = random.nextFloat() * 0.4f - 0.2f;
				Vector2f randEndPos = left.rotate(angle).multLocal(leftStreetLength).addLocal(currentStepPos);
				Road child = new Road(currentStepPos, randEndPos, 3.7f * 2);
				child.setHighway(false);
				child.setProposer(road);
				proposedRoads.add(child);
			}
		}
	}

	private float randomStreetStep() {
		return random.nextFloat() * 70 + 70;
	}

	private Road proposeHighwayOffshoot(Vector2f start, Vector2f direction, float width, float minLength, float lengthRange,
										float minAngle, float maxAngle) {
		float length = random.nextFloat() * lengthRange + minLength; // 200m - 1200m
		Vector2f bestDirection = direction.clone();
		float bestWeight = 0;

		for (float angle = minAngle; angle < maxAngle; angle += 0.2f) {
			Vector2f possibleDirection = direction.rotate(angle);
			float weight = calculatePopulationDensityWeight(start, possibleDirection, length);
			if (weight > bestWeight) {
				bestDirection = possibleDirection;
				bestWeight = weight;
			}
		}

		Vector2f end = bestDirection.multLocal(length).addLocal(start);
		return new Road(start, end, width);
	}

	private float calculatePopulationDensityWeight(Vector2f start, Vector2f possibleDirection, float length) {
		float stepSize = populationDensityMap.getPixelSize() * 0.5f;
		int steps = (int) (length / stepSize);
		float distanceToEnd = length;
		Vector2f currentProbePos = new Vector2f(start);
		Vector2f stepVector = possibleDirection.mult(stepSize);
		float totalWeight = 0;

		for (int i = 0; i < steps; i++) {
			currentProbePos.addLocal(stepVector);
			distanceToEnd -= stepSize;

			float populationDensity = populationDensityMap.getValue(currentProbePos);
			totalWeight += populationDensity / distanceToEnd;
		}

		return totalWeight;
	}
}
