package cityplanner.planners.plots;

import cityplanner.math.BoundingRect;
import cityplanner.noise.PopulationMap;
import cityplanner.planners.roads.Road;
import cityplanner.quadtree.Quadtree;
import cityplanner.quadtree.QuadtreeItem;
import com.jme3.math.Vector2f;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author VTPlusAKnauer
 */
public class PlotPlanner {

	private final Random random;
	private final PopulationMap populationMap;

	public final static float floorHeight = 3;

	public PlotPlanner(Random random, PopulationMap populationMap) {
		this.random = random;
		this.populationMap = populationMap;
	}

	public final List<Plot> plan(List<Road> roads, Quadtree quadtree) {
		ArrayList<Plot> builtPlots = new ArrayList<>();

		for (Road road : roads) {
			Vector2f rightSampleOffset = road.getRight().mult(populationMap.getPixelSize());
			Vector2f right = road.getRight().clone();
			Vector2f left = right.negate();

			float sideOffset = 10 + road.getWidth() * 0.5f + random.nextFloat() * 10;
			for (float step = 0; step < road.getLength();) {
				Vector2f currentStepPos = road.getDirection().mult(step).addLocal(road.getStart());
				Vector2f rightSample = currentStepPos.add(rightSampleOffset);

				float populationValue = populationMap.getValue(rightSample);
				if (populationValue < random.nextFloat() * 0.2f + 0.1f) {
					step += random.nextFloat() * 50 + 20;
					continue;
				}

				float size = randomPlotSize(rightSample);

				Vector2f position = right.mult(size + sideOffset).addLocal(currentStepPos);
				Plot plot = new Plot(position, road.getDirection(), size * random.nextFloat() * 0.5f + size, size);

				if (!isColliding(plot, quadtree)) {
					builtPlots.add(plot);
					quadtree.insert(plot);
					plot.setHeight(randomPlotHeight(populationValue));
					step += plot.getLength() + 10;
				}
				else
					step += size * 0.5f;
			}

			for (float step = 0; step < road.getLength();) {
				Vector2f currentStepPos = road.getDirection().mult(step).addLocal(road.getStart());
				Vector2f leftSample = currentStepPos.subtract(rightSampleOffset);

				float populationValue = populationMap.getValue(leftSample);
				if (populationValue < random.nextFloat() * 0.1f + 0.05f) {
					step += random.nextFloat() * 50 + 20;
					continue;
				}

				float size = randomPlotSize(leftSample);

				Vector2f position = left.mult(size + sideOffset).addLocal(currentStepPos);
				Plot plot = new Plot(position, road.getDirection(), size * random.nextFloat() * 0.5f + size, size);

				if (!isColliding(plot, quadtree)) {
					builtPlots.add(plot);
					quadtree.insert(plot);
					plot.setHeight(randomPlotHeight(populationValue));
					step += plot.getLength() + 10;
				}
				else
					step += size * 0.5f;
			}
		}

		return builtPlots;
	}

	private float randomPlotHeight(float populationValue) {
		int maxFloors = (int) (populationValue * populationValue * populationValue * 300);
		int minFloors = maxFloors - 5;
		int randomFloors = random.nextInt(5) + 3;
		if (minFloors < maxFloors) {
			randomFloors += random.nextInt(maxFloors - minFloors) + minFloors;
		}

		return randomFloors * floorHeight;
	}

	private float randomPlotSize(Vector2f position) {
		return random.nextFloat() * 50 * populationMap.getValue(position) + 20;
	}

	private boolean isColliding(Plot plot, Quadtree quadtree) {
		BoundingRect queryBounds = plot.getBoundingRect();
		ArrayList<QuadtreeItem> items = quadtree.retrieve(queryBounds);

		for (QuadtreeItem item : items) {
			if (item instanceof Road) {
				if (((Road) item).getRectangle().overlaps(plot))
					return true;
			}
			else if (item instanceof Plot) {
				if (((Plot) item).overlaps(plot))
					return true;
			}
		}

		return false;
	}

}
