/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architect.processors.connector;

import architect.floorplan.FloorPlan;
import architect.room.Room;
import architect.walls.WallNode;
import architect.walls.WallNodeNeighbor;
import java.util.PriorityQueue;

/**
 *
 * @author VTPlusAKnauer
 */
public final class PathFinder {

	private PathFinder() {
	}

	public static Path find(FloorPlan fp, Room startRoom, Room endRoom,
							CostJudge costJudge, PathTrimmer startTrimmer, PathTrimmer endTrimmer) {
		/**
		 * *********************** check corners first ************************
		 */
		if (startRoom.isTouchingSides(endRoom)) { // toching corners -> no path
			return null;
		}

		for (WallNode node : fp.wallNodes) {
			node.resetSearchStats();
		}

		/**
		 * *********************** find path from room to room ************************
		 */
		// pick a node on the room, doesnt matter which one
		WallNode startNode = startRoom.getWallNodeLoop().getFirst();
		WallNode endNode = endRoom.getWallNodeLoop().getFirst();

		// do the A* search from start to end
		PriorityQueue<WallNode> openList = new PriorityQueue<>(16,
															   (WallNode wnA, WallNode wnB) -> wnA.distToEndEstimate < wnB.distToEndEstimate ? -1 : +1);

		startNode.searchStatus = WallNode.SearchStatus.OPEN;
		startNode.distToEndEstimate = startNode.squaredDist(endNode);
		openList.add(startNode);

		while (!openList.isEmpty()) { // A* search
			WallNode currentNode = openList.poll();
			currentNode.searchStatus = WallNode.SearchStatus.CLOSED;

			if (currentNode == endNode) { // found best way to the goal
				break;
			}

			for (WallNodeNeighbor neighbor : currentNode.neighbors.values()) {
				WallNode node = neighbor.getNode();
				float cost = costJudge.judge(neighbor);

				// moving along the walls the startRoom or the endRoom should not cost anything since these edges will be trimmed from the result
				if (neighbor.getWall().isAssignedTo(startRoom) || neighbor.getWall().isAssignedTo(endRoom))
					cost = 0;

				if (node.searchStatus == WallNode.SearchStatus.UNCHARTED) {
					node.pathParent = currentNode;
					node.distFromStart = cost + currentNode.distFromStart;
					node.distToEndEstimate = node.squaredDist(endNode) + node.distFromStart;
					node.searchStatus = WallNode.SearchStatus.OPEN;
					openList.add(node);
				}
				else if (node.searchStatus == WallNode.SearchStatus.OPEN) { // already charted, compare path costs
					float distFromStartThroughThis = cost + currentNode.distFromStart;
					if (node.distFromStart > distFromStartThroughThis) {
						openList.remove(node);
						node.pathParent = currentNode;
						node.distFromStart = distFromStartThroughThis;
						node.distToEndEstimate = node.squaredDist(endNode) + node.distFromStart;
						openList.add(node);
					}
				}
			}
		}

		// link nodes the other direction: startRoom -> endRoom 
		WallNode node = endNode;
		while (node.pathParent != null) {
			node.pathParent.pathChild = node;
			node = node.pathParent;
		}

		// enforce startTrimmer
		if (startTrimmer != null) {
			node = startNode;
			while (endNode != node) {
				Room alternativeStartRoom = startTrimmer.canTrim(node.neighbors.get(node.pathChild).getWall());
				if (alternativeStartRoom != null) {
					node = node.pathChild;
					startNode = node;
					startNode.pathParent = null;
					startRoom = alternativeStartRoom;
				}
				else
					break;
			}
		}

		// enforce endTrimmer
		if (endTrimmer != null) {
			node = endNode;
			while (startNode != node) {
				Room alternativeEndRoom = endTrimmer.canTrim(node.neighbors.get(node.pathParent).getWall());
				if (alternativeEndRoom != null) {
					node = node.pathParent;
					endNode = node;
					endNode.pathChild = null;
					endRoom = alternativeEndRoom;

				}
				else
					break;
			}
		}

		// sum up the total path cost
		return new Path(startNode, endNode, startRoom, endRoom, endNode.distFromStart);
	}

}
