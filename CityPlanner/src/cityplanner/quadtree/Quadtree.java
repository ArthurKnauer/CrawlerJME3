package cityplanner.quadtree;

import cityplanner.math.BoundingRect;
import com.jme3.math.Vector2f;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author VTPlusAKnauer
 */
public class Quadtree {

	private final int MAX_OBJECTS = 10;
	private final int MAX_LEVELS = 6;

	private final int level;
	private final List<QuadtreeItem> items;
	private final BoundingRect bounds;
	private final Quadtree[] nodes;

	public Quadtree(BoundingRect bounds) {
		this(0, bounds);
	}

	private Quadtree(int level, BoundingRect bounds) {
		this.level = level;
		this.items = new ArrayList<>();
		this.bounds = bounds;
		this.nodes = new Quadtree[4];
	}

	public void clear() {
		items.clear();

		for (int i = 0; i < nodes.length; i++) {
			if (nodes[i] != null) {
				nodes[i].clear();
				nodes[i] = null;
			}
		}
	}

	private void split() {
		Vector2f subExtent = new Vector2f(bounds.getExtents().mult(0.5f));
		int subLevel = level + 1;

		nodes[0] = new Quadtree(subLevel, new BoundingRect(bounds.getCenter().add(subExtent.x, -subExtent.y), subExtent));
		nodes[1] = new Quadtree(subLevel, new BoundingRect(bounds.getCenter().add(-subExtent.x, -subExtent.y), subExtent));
		nodes[2] = new Quadtree(subLevel, new BoundingRect(bounds.getCenter().add(-subExtent.x, subExtent.y), subExtent));
		nodes[3] = new Quadtree(subLevel, new BoundingRect(bounds.getCenter().add(subExtent.x, subExtent.y), subExtent));
	}

	/*
	 * Insert the object into the quadtree. If the node
	 * exceeds the capacity, it will split and add all
	 * objects to their corresponding nodes.
	 */
	public void insert(QuadtreeItem item) {
		if (nodes[0] != null) {
			for (Quadtree node : nodes) {
				if (node.bounds.overlap(item.getBoundingRect()))
					node.insert(item);
			}
		}
		else {
			items.add(item);

			if (items.size() > MAX_OBJECTS && level < MAX_LEVELS) {
				redistributeItems();
			}
		}
	}

	private void redistributeItems() {
		if (nodes[0] == null) {
			split();
		}

		for (QuadtreeItem item : items) {
			insert(item);
		}

		items.clear();
	}

	public ArrayList<QuadtreeItem> retrieve(BoundingRect queryBounds) {
		ArrayList<QuadtreeItem> itemList = new ArrayList<>();
		retrieve(itemList, queryBounds);
		return itemList;
	}

	private void retrieve(List<QuadtreeItem> itemList, BoundingRect queryBounds) {
		if (nodes[0] != null) {
			for (Quadtree node : nodes) {
				if (node != null && node.bounds.overlap(queryBounds)) {
					node.retrieve(itemList, queryBounds);
				}
			}
		}
		else {
			itemList.addAll(items);
		}
	}

}
