/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package crawler.navmesh.astar;

import java.util.*;

public class AStar {

	// sorts nodes by their estimated path cost
	private static class NodeComparator implements Comparator<AStarNode> {

		@Override
		public int compare(AStarNode nodeFirst, AStarNode nodeSecond) {
			if (nodeFirst.getTotalPathCostEstimate() > nodeSecond.getTotalPathCostEstimate()) return 1;
			else return -1;
		}
	}

	/**
	 * Implements the A-star algorithm and returns the path from source to destination
	 *
	 * @param <T> a class extending mygame.navmesh.astar.AStarNode
	 * @param source the source node
	 * @param destination the destination node
	 * @return the path from source to destination
	 */
	public static <T extends AStarNode> List<T> findPath(T source, T destination) {
		Queue<T> openQueue = new PriorityQueue<>(10, new NodeComparator());
		final Map<T, T> pathMap = new HashMap<>();
		final Set<T> closedSet = new HashSet<>();
		final Set<T> encounteredSet = new HashSet<>(); // faster "is open" check than the openQueue
		
		source.setCostToSource(0);
		source.estimateTotalPathCost(destination);
		openQueue.add(source);

		while (!openQueue.isEmpty()) {
			final T node = openQueue.poll();
			if (node.equals(destination)) return buildPathList(pathMap, destination);
			closedSet.add(node);

			for (Object obj : node.neighbors()) { // fuckin' generics, how do they work? v=OvmvxAcT_Yc
				T neighbor = (T) obj;
				if (closedSet.contains(neighbor)) continue;
				
				float pathToNeighborCost = node.getCostToSource() + node.neighborCost(neighbor);

				// if neighbor is in encounteredSet it's distToSource value was set
				boolean neighborInOpenSet = encounteredSet.contains(neighbor);
				if (!neighborInOpenSet || pathToNeighborCost < neighbor.getCostToSource()) {
					neighbor.setCostToSource(pathToNeighborCost);
					neighbor.estimateTotalPathCost(destination);

					pathMap.put(neighbor, node);
					if (!neighborInOpenSet) {
						openQueue.add(neighbor);
						encounteredSet.add(neighbor);
					}
				}				
			}
		}

		return Collections.<T>emptyList();
	}

	/**
	 * Builds a list path from a path map (which is the result of A*)
	 * @param <T>
	 * @param pathMap
	 * @param destination
	 * @return 
	 */
	private static <T extends AStarNode> List<T> buildPathList(Map<T, T> pathMap, T destination) {
		assert pathMap != null;
		assert destination != null;

		final List<T> pathList = new ArrayList<>();
		pathList.add(destination);
		while (pathMap.containsKey(destination)) {
			destination = pathMap.get(destination);
			pathList.add(destination);
		}
		
		Collections.reverse(pathList);
		return pathList;
	}
}
