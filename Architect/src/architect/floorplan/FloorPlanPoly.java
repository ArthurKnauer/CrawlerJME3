package architect.floorplan;

import architect.math.segments.LineSegment;
import architect.math.Rectangle;
import architect.math.segments.Side;
import architect.rectpoly.RectilinearPoly;
import architect.walls.WallSide;
import architect.walls.WallType;
import java.util.List;
import java.util.Set;

public class FloorPlanPoly extends RectilinearPoly<FloorPlanPoly, Rectangle, WallSide> {

	private final float windowablePerimeter = 0;
	public final Rectangle entrance;

	public FloorPlanPoly(Set<Rectangle> subrects, List<WallSide> wallLoop, Rectangle entrance) {
		super(subrects, wallLoop);
		this.entrance = entrance;
	}

	public float windowablePerimeter() {
		return windowablePerimeter;
	}

	public float windowablePerimeter(Rectangle subrect) {
		float perimeter = 0;
		for (Side side : Side.values()) {
			LineSegment line = subrect.sideLineSegment(side);
			for (WallSide wall : edgeLoop) {
				if (wall.type == WallType.OUTER_WINDOWABLE) {
					perimeter += wall.touchOverlapLength(line);
				}
			}
		}
		return perimeter;
	}
}
