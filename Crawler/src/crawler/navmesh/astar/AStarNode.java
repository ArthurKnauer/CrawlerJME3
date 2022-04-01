/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package crawler.navmesh.astar;

import java.util.Set;
import lombok.*;

public abstract class AStarNode<T extends AStarNode> {

	@Getter @Setter private float costToSource; // set and read by AStar
	@Getter private float costToDestEstimate; // estimated by the implementation of this class
	@Getter private float totalPathCostEstimate;  // totalPathCostEstimate = costToSource + costToDestEstimate 

	public AStarNode() {
	}

	final void estimateTotalPathCost(T destination) {
		this.costToDestEstimate = estimateCost(destination);
		this.totalPathCostEstimate = costToSource + costToDestEstimate;
	}

	protected abstract float estimateCost(T node);
	
	public abstract Set<T> neighbors();
	
	public abstract float neighborCost(T neighbor);	
}
