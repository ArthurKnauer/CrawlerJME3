package crawler.navmesh.navpoly;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class NavPolyNeighbor {

	@Getter private final float cost;
	@Getter private final int edge;
}