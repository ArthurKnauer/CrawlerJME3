/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package architect.processors;

import architect.room.Room;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;

/**
 *
 * @author VTPlusAKnauer
 */
public class StatisticsAnalyzer extends FloorPlanProcessor {

	private final static Logger LOGGER = getLogger(StatisticsAnalyzer.class.getName());

	private static final int graphEntriesPerUnit = 20;
	public static final HashMap<Integer, Integer> bigRectRatioGraph = new HashMap<>();

	@Override
	protected void process() {
		addBigRectRatiosToGraph(fp.rooms);

		LOGGER.log(Level.INFO, "bigRectRatios: {0}", bigRectRatioGraph);
	}

	private void addBigRectRatiosToGraph(Collection<Room> rooms) {
		rooms.stream().filter(room -> room.biggestRect() != null)
				.forEach(room -> {
					float bigRectRatio = room.biggestRect().inverseAspectRatio();
					addFloatToGraph(bigRectRatioGraph, bigRectRatio);
				});
	}

	private void addFloatToGraph(HashMap<Integer, Integer> graph, float value) {
		Integer index = floatToGraphIndex(value);
		incrementGraphValueAt(graph, index);
	}

	private int floatToGraphIndex(float value) {
		return (int) (value * graphEntriesPerUnit);
	}

	private void incrementGraphValueAt(HashMap<Integer, Integer> graph, int index) {
		Integer amountOfEqualValues = graph.get(index);
		if (amountOfEqualValues == null)
			graph.put(index, 1);
		else
			graph.put(index, amountOfEqualValues + 1);
	}
}
