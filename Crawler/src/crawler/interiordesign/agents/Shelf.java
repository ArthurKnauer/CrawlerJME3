package crawler.interiordesign.agents;


import java.util.LinkedList;

public class Shelf {

	float bottom;
	float height;
	OrientedBoundingBox shelfBBox;
	LinkedList<Agent> children = new LinkedList<>();
	private final Agent parent;

	Shelf(float bottom, float height, final Agent parent) {
		this.parent = parent;
		this.bottom = bottom;
		this.height = height;
		shelfBBox = new OrientedBoundingBox(parent);
		shelfBBox.setYExtent(height * 0.5f);
		shelfBBox.move(0, bottom - shelfBBox.getYExtent() + shelfBBox.getYExtent(), 0);
	}
}