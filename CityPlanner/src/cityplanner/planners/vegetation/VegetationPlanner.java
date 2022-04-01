package cityplanner.planners.vegetation;

import cityplanner.math.BoundingRect;
import cityplanner.planners.plots.Plot;
import cityplanner.planners.roads.Road;
import cityplanner.quadtree.Quadtree;
import cityplanner.quadtree.QuadtreeItem;
import com.jme3.math.Vector2f;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author VTPlusAKnauer
 */
public class VegetationPlanner {

	private final Random random;

	public VegetationPlanner(Random random) {
		this.random = random;
	}
	
	private final float clusterSize = 500;
	private final float clusterSizeHalf = clusterSize / 2;
	private final int iterationSize = (int) (2500 / clusterSize);
	private final static float treeDistance = 6;

	public final List<Cluster<Tree>> plan(Quadtree quadtree) {
		List<Cluster<Tree>> treeClusters = new ArrayList<>();
		
		ExecutorService executorService = Executors.newFixedThreadPool(16);

		final Vector2f clusterExtents = new Vector2f(clusterSizeHalf, clusterSizeHalf);

		for (int cx = -iterationSize; cx <= iterationSize; cx++) {
			for (int cy = -iterationSize; cy <= iterationSize; cy++) {

				final int threadCX = cx;
				final int threadCY = cy;

				executorService.execute(() -> {
					Cluster<Tree> cluster = new Cluster<>(new Vector2f(threadCX * clusterSize, threadCY * clusterSize),
														  clusterExtents);

					for (float x = cluster.center.x - cluster.extents.x; x < cluster.center.x + cluster.extents.x; x += treeDistance) {
						for (float y = cluster.center.y - cluster.extents.y; y < cluster.center.y + cluster.extents.y; y += treeDistance) {
							float size = (float) (Math.abs(random.nextGaussian() + 1) + 1) * 5;
							if (size > 20)
								size = 20;

							float xOffset = (random.nextFloat() - 0.5f) * treeDistance * 0.8f;
							float yOffset = (random.nextFloat() - 0.5f) * treeDistance * 0.8f;

							Vector2f location = new Vector2f(x + xOffset, y + yOffset);
							float heightPos = getHeight(location, quadtree);

							if (heightPos > 1) {
								size *= 0.5f + (random.nextFloat() * 0.5f);
							}
							else if (heightPos < 0) { // road!
								continue;
							}

							Tree tree = new Tree(location, heightPos, size);
							cluster.list.add(tree);
						}
					}

					treeClusters.add(cluster);
				});
			}
		}

		executorService.shutdown();
		try {
			executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			System.err.println("Failed to await thread termination:\n" + e);
		}
		
		return treeClusters;
	}

	private static float getHeight(Vector2f location, Quadtree quadtree) {
		BoundingRect queryBounds = new BoundingRect(location, new Vector2f(0, 0));
		ArrayList<QuadtreeItem> collidables = quadtree.retrieve(queryBounds);

		for (QuadtreeItem item : collidables) {
			if (item instanceof Plot) {
				Plot plot = (Plot) item;
				if (plot.contains(location))
					return plot.getHeight();
			}
			else if (item instanceof Road) {
				Road road = (Road) item;
				if (road.getRectangle().contains(location))
					return -1;
			}
		}

		return 0;
	}
}
