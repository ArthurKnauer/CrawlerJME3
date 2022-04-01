package cityplanner.test;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author VTPlusAKnauer
 */
public class Statistics {

	private final float graphEntriesPerUnit;
	private final HashMap<Integer, Integer> graph = new HashMap<>();

	public Statistics(float graphEntriesPerUnit) {
		this.graphEntriesPerUnit = graphEntriesPerUnit;
	}

	public void clear() {
		graph.clear();
	}

	public void addFloat(float value) {
		Integer index = floatToGraphIndex(value);
		incrementGraphValueAt(index);
	}

	private int floatToGraphIndex(float value) {
		return (int) (value * graphEntriesPerUnit);
	}

	private void incrementGraphValueAt(int index) {
		Integer amount = graph.get(index);
		if (amount == null)
			graph.put(index, 1);
		else
			graph.put(index, amount + 1);
	}

	@Override
	public String toString() {
		StringBuilder xValues = new StringBuilder();
		StringBuilder yValues = new StringBuilder();
		for (Map.Entry<Integer, Integer> entry : graph.entrySet()) {
			xValues.append(entry.getKey() / graphEntriesPerUnit).append(", ");
			yValues.append(entry.getValue()).append(", ");
		}
		return xValues.append("\n").append(yValues).toString();
	}

}
