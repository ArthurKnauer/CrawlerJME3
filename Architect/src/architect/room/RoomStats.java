package architect.room;

public class RoomStats {

	/**
	 * Room area will be (relativeArea / sum of all relativeAreas) * mainRectArea
	 */
	public final float relativeArea;
	/**
	 * Computed area from mainRect and relativeArea see above
	 */
	public float needsArea;
	/**
	 * window-able wall preference
	 */
	public final boolean needsWindow;
	/**
	 * Example: DiningRoom needs to be near Kitchen
	 */
	public final RoomType obligatoryNeighbor;
	/**
	 * Example: Kitchen can fuse with LivingRoom
	 */
	public final RoomType canFuseWith;
	/**
	 * Private rooms can only have a door with a public room
	 */
	public final boolean isPublic;
	/**
	 * Max acceptable protrusion side (bathroom may accept smaller protrusions than a living room)
	 */
	public float maxProtrusionSize;

	public RoomStats(float relativeArea, boolean needsWindow, RoomType obligatoryNeighbor, RoomType canFuseWith,
					 boolean isPublic, float maxProtrusionSize) {
		this.relativeArea = relativeArea;
		this.needsWindow = needsWindow;
		this.obligatoryNeighbor = obligatoryNeighbor;
		this.canFuseWith = canFuseWith;
		this.isPublic = isPublic;
		this.maxProtrusionSize = maxProtrusionSize;
	}
};
