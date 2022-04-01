package crawler.interiordesign;

import architect.walls.Opening;
import architect.walls.WallSide;
import com.jme3.math.Vector3f;
import crawler.interiordesign.agents.Agent;
import crawler.interiordesign.agents.AgentBuilder;
import crawler.interiordesign.agents.AgentTypes;
import crawler.util.Convert;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author VTPlusAKnauer
 */
public class WallSideToAgent {

	static List<Agent> wallAgentsForRoom(List<WallSide> wallSides) {
		ArrayList<Agent> agents = new ArrayList(wallSides.size() + 4); // one for each wall +4 doors
		for (WallSide wall : wallSides) {
			agents.addAll(wallAgentsForWall(wall));
		}

		return agents;
	}

	private static List<Agent> wallAgentsForWall(WallSide wall) {
		float wt = 0.2f, wh = 3;
		ArrayList<Agent> agents = new ArrayList<>(2);

		AgentBuilder builder = AgentBuilder.start();
		builder.setObstructable(false);
		builder.setObstructor(false);

		Vector3f forward = Convert.toJMEVector3f(wall.getNormal());
		Vector3f thicknessOffset = forward.mult(wt * 0.5f);

		Vector3f location = Convert.toJMEVector3f(wall.center()).addLocal(thicknessOffset);
		location.y = wh * 0.5f;
		Vector3f extents = new Vector3f(wall.length() * 0.5f - wt * 0.5f, wh * 0.5f, 0);

		builder.setLocation(location);
		builder.setExtents(extents);
		builder.setForward(forward);
		builder.setType(wall.hasWindows() ? AgentTypes.WINDOW_WALL : AgentTypes.WINDOWLESS_WALL);
		agents.add(builder.build());
		agents.addAll(doorObstructorsForWall(wall));

		return agents;
	}

	private static List<Agent> doorObstructorsForWall(WallSide wall) {
		ArrayList<Agent> agents = new ArrayList<>(1);
		if (wall.hasDoors()) {
			
			for (Opening opening : wall.getOpenings().values()) {
				if (opening.type == Opening.Type.DOOR) {
					Vector3f doorCenter = Convert.toJMEVector3f(wall.getOpeningCenter(opening));
					doorCenter.addLocal(0, opening.height * 0.5f, 0);
					Vector3f forward = Convert.toJMEVector3f(wall.getNormal());

					AgentBuilder builder = AgentBuilder.start();
					builder.setObstructable(false);
					builder.setObstructor(true);
					builder.setLocation(doorCenter);
					builder.setExtents(new Vector3f(opening.width * 0.5f, opening.height * 0.5f, 1.0f));
					builder.setForward(forward);
					builder.setType(AgentTypes.INVISIBLE_OBSTRUCTOR);
					agents.add(builder.build());
				}
			}
		}
		return agents;
	}
}
