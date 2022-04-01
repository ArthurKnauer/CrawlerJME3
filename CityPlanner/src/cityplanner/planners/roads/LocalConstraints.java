package cityplanner.planners.roads;

import cityplanner.math.BoundingRect;
import cityplanner.math.LineSegment2D.Intersection;
import cityplanner.quadtree.Quadtree;
import cityplanner.quadtree.QuadtreeItem;
import com.jme3.math.Vector2f;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 *
 * @author VTPlusAKnauer
 */
public class LocalConstraints {

	private final Random random;

	private float maxJoinRadius = 400;
	private float gridSize = 5000;
	private float minAngle = 0.5f;
	private float minDistanceForMinAngle = 100;

	public LocalConstraints(Random random) {
		this.random = random;
	}

	public Optional<Road> checkAndAdd(Road proposedRoad, List<Road> builtRoads, Quadtree quadtree) {
		if (isOutsideOfGrid(proposedRoad))
			return Optional.empty();

		Vector2f extendedJoinCenter = proposedRoad.getDirection().mult(maxJoinRadius).addLocal(proposedRoad.getEnd());
		Vector2f closestJoinEndpoint = null;
		float closestJoinDistSquared = maxJoinRadius * maxJoinRadius;

		Intersection intersection = new Intersection();
		boolean intersectionFound = false;
		float closestIntersectionDistSquared = Float.MAX_VALUE;
		Vector2f closestIntersection = new Vector2f();

		BoundingRect roadBounds = proposedRoad.getIncreasedBoundingRect(minDistanceForMinAngle);
		BoundingRect joinBounds = new BoundingRect(extendedJoinCenter, new Vector2f(maxJoinRadius, maxJoinRadius));
		ArrayList<QuadtreeItem> closeRoads = quadtree.retrieve(roadBounds.merge(joinBounds));

		for (QuadtreeItem item : closeRoads) {
			Road builtRoad = (Road) item;
			if (proposedRoad.getProposer() != builtRoad) {

				if (tooCloseAndNarrow(builtRoad, proposedRoad, intersection))
					return Optional.empty();

				if (intersection.found) { // check if this intersection is closer then previous one
					intersectionFound = true;

					float intersectionDistSquared = intersection.point.distanceSquared(proposedRoad.getStart());
					if (intersectionDistSquared < closestIntersectionDistSquared) {
						closestIntersectionDistSquared = intersectionDistSquared;
						closestIntersection.set(intersection.point);
					}
				}

				if (!intersectionFound) { // try to find close road to connect to
					Vector2f joinEndpoint = builtRoad.closestPoint(extendedJoinCenter);
					float distSquared = joinEndpoint.distanceSquared(proposedRoad.getEnd());
					if (distSquared < closestJoinDistSquared) {
						closestJoinDistSquared = distSquared;
						closestJoinEndpoint = joinEndpoint;
					}
				}
			}
		}

		Road lastAddedRoad = proposedRoad;
		builtRoads.add(proposedRoad);
		quadtree.insert(proposedRoad);

		if (intersectionFound) {
			proposedRoad.setEnd(closestIntersection);
		}
		else if (closestJoinEndpoint != null) {
			Road connectingRoad = createConnectingRoad(proposedRoad, closestJoinEndpoint);
			builtRoads.add(connectingRoad);
			quadtree.insert(connectingRoad);
			lastAddedRoad = connectingRoad;
		}

		return Optional.of(lastAddedRoad);
	}

	private boolean isOutsideOfGrid(Road proposedRoad) {
		return (proposedRoad.getStart().x < -gridSize || proposedRoad.getStart().x > gridSize
				|| proposedRoad.getStart().y < -gridSize || proposedRoad.getStart().y > gridSize
				|| proposedRoad.getEnd().x < -gridSize || proposedRoad.getEnd().x > gridSize
				|| proposedRoad.getEnd().y < -gridSize || proposedRoad.getEnd().y > gridSize);
	}

	private boolean tooCloseAndNarrow(Road roadA, Road roadB, Intersection intersection) {
		return roadA.distance(roadB, intersection) < minDistanceForMinAngle && roadA.angle(roadB) < minAngle;
	}

	private Road createConnectingRoad(Road parent, Vector2f endJoinPoint) {
		Road connectingRoad = new Road(parent.getEnd(), endJoinPoint, parent.getWidth());
		connectingRoad.setHighway(parent.isHighway());
		connectingRoad.setStartConnected(true);
		return connectingRoad;
	}

	public float getMaxJoinRadius() {
		return maxJoinRadius;
	}

	public void setMaxJoinRadius(float maxJoinRadius) {
		this.maxJoinRadius = maxJoinRadius;
	}

	public float getGridSize() {
		return gridSize;
	}

	public void setGridSize(float gridSize) {
		this.gridSize = gridSize;
	}

	public float getMinAngle() {
		return minAngle;
	}

	public void setMinAngle(float minAngle) {
		this.minAngle = minAngle;
	}

	public float getMinDistanceForMinAngle() {
		return minDistanceForMinAngle;
	}

	public void setMinDistanceForMinAngle(float minDistanceForMinAngle) {
		this.minDistanceForMinAngle = minDistanceForMinAngle;
	}
}
