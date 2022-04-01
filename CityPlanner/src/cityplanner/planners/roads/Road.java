package cityplanner.planners.roads;

import cityplanner.math.BoundingRect;
import cityplanner.math.LineSegment2D;
import cityplanner.math.Rectangle2D;
import cityplanner.quadtree.QuadtreeItem;
import com.jme3.math.Vector2f;

/**
 *
 * @author VTPlusAKnauer
 */
public class Road extends LineSegment2D implements QuadtreeItem {

	private float width;
	private boolean startConnected = false;
	private boolean highway = false;

	private Road proposer;

	private Rectangle2D rectangle;
	private boolean updateRectangle;

	public static int idCounter = 0;
	public final int id;

	public Road(Vector2f start, Vector2f end, float width) {
		super(start, end);
		this.width = width;
		this.id = idCounter++;
		updateRectangle = true;
	}

	@Override
	public void setEnd(Vector2f end) {
		super.setEnd(end);
		updateRectangle = true;
	}

	@Override
	public void setStart(Vector2f start) {
		super.setStart(start);
		updateRectangle = true;
	}

	public Road getProposer() {
		return proposer;
	}

	public void setProposer(Road proposer) {
		this.proposer = proposer;
	}

	public float getWidth() {
		return width;
	}

	public void setWidth(float width) {
		this.width = width;
		updateRectangle = true;
	}

	public Rectangle2D getRectangle() {
		if (updateRectangle) {
			if (rectangle == null) {
				rectangle = new Rectangle2D(getMiddle(), getDirection(), getLength() * 0.5f, width);
			}
			else {
				rectangle.setCenter(getMiddle());
				rectangle.setLenghtDirection(getDirection());
				rectangle.setLength(getLength() * 0.5f);
				rectangle.setWidth(width);
			}
		}
		updateRectangle = false;
		return rectangle;
	}

	@Override
	public BoundingRect getBoundingRect() {
		return getRectangle().getBoundingRect();
	}

	public BoundingRect getIncreasedBoundingRect(float increase) {
		return getBoundingRect().increased(increase);
	}

	public boolean isStartConnected() {
		return startConnected;
	}

	public void setStartConnected(boolean startConnected) {
		this.startConnected = startConnected;
	}

	public boolean isHighway() {
		return highway;
	}

	public void setHighway(boolean highway) {
		this.highway = highway;
	}

	@Override
	public String toString() {
		return "Road_" + id;
	}
}
