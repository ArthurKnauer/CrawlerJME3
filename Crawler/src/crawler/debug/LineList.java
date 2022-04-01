package crawler.debug;

import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Line;
import crawler.main.Globals;
import java.util.ArrayList;
import java.util.Optional;

/**
 *
 * @author VTPlusAKnauer
 */
public class LineList extends Node {

	private final ArrayList<Line> lines;

	public LineList(String name, int lineCount) {
		super(name);

		Material mat = new Material(Globals.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
		mat.setColor("Color", ColorRGBA.Green);

		lines = new ArrayList<>(lineCount);
		for (int l = 0; l < lineCount; l++) {
			Line line = new Line(Vector3f.ZERO, Vector3f.ZERO);
			lines.add(line);
			Geometry geometry = new Geometry(name + "_line_" + l, line);
			geometry.setMaterial(mat);
			attachChild(geometry);
		}
	}
	public Optional<Vector3f> getNodeLocation(int index) {
		int previous = index - 1;
		if (previous >= 0 && previous < lines.size())
			return Optional.of(lines.get(previous).getEnd());
		else
			return Optional.empty();					
	}

	public void setNodeLocation(int index, Vector3f nodePosition) {
		int previous = index - 1;
		if (previous >= 0 && previous < lines.size())
			lines.get(previous).updateEnd(nodePosition);

		if (index >= 0 && index < lines.size())
			lines.get(index).updateStart(nodePosition);
	}

	public void setFinalNode(int index) {
		Optional<Vector3f> nodePosition = getNodeLocation(index);
		if (nodePosition.isPresent()) {
			for (int n = index; n < lines.size(); n++) {
				lines.get(n).updatePoints(nodePosition.get(), nodePosition.get());
			}
		}
	}

}
