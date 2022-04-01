/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architect.floorplan;

/**
 *
 * @author VTPlusAKnauer
 */
public class FloorPlanAttribs {

	public final float wallThickness;
	public final float wallHeight;

	public final float windowWidth;
	public final float windowTop;
	public final float windowBottom;

	public final float doorWidth; // must be smaller than minOverlapForDoor
	public final float doorHeight; // must be smaller than wallHeight

	public final float minRoomSideSize;
	public final float minSubrectSplitLength;
	public final float minSubrectCut;

	public final float minWallFixLength;
	public final float minWallFixDepth;
	public final float maxWallOptimalPosFuseDist;

	public final float minSnapLineDist;
	public final float minOverLapForNeighbor; // must be smaller than minSnapLineDist

	public final float hallwayWidth;
	public final float minOverlapForDoor; // must be smaller than hallwayWidth
	public final float hallwayHalfWidth;
	public final float minHallwayArea;

	public FloorPlanAttribs(float wallThickness, float wallHeight,
							float windowWidth, float windowTop, float windowBottom,
							float doorWidth, float doorHeight, float minRoomSideSize,
							float minSubrectSplitLength, float minSubrectCut,
							float minWallFixLength, float minWallFixDepth, float maxWallOptimalPosFuseDist,
							float minSnapLineDist,
							float hallwayWidth, float minOverlapForDoor, float minHallwayArea) {
		this.wallThickness = wallThickness;
		this.wallHeight = wallHeight;
		this.windowWidth = windowWidth;
		this.windowTop = windowTop;
		this.windowBottom = windowBottom;
		this.doorWidth = doorWidth;
		this.doorHeight = doorHeight;
		this.minRoomSideSize = minRoomSideSize;
		this.minSubrectSplitLength = minSubrectSplitLength;
		this.minSubrectCut = minSubrectCut;
		this.minWallFixLength = minWallFixLength;
		this.minWallFixDepth = minWallFixDepth;
		this.maxWallOptimalPosFuseDist = maxWallOptimalPosFuseDist;
		this.minSnapLineDist = minSnapLineDist;
		this.minOverLapForNeighbor = minSnapLineDist * 0.5f;
		this.hallwayWidth = hallwayWidth;
		this.minOverlapForDoor = minOverlapForDoor;
		this.hallwayHalfWidth = hallwayWidth * 0.5f;
		this.minHallwayArea = minHallwayArea;
	}

	public static class Builder {

		private float wallThickness = 0.2f;
		private float wallHeight = 3.0f;

		private float windowWidth = 1.5f;
		private float windowTop = 2.8f;
		private float windowBottom = 1.1f;

		private float doorWidth = 1.2f;
		private float doorHeight = 2.2f;

		private float minRoomSideSize = 3;
		private float minSubrectSplitLength = 2;
		private float minSubrectCut = 0.1f;

		private float minWallFixLength = 2.0f;
		private float minWallFixDepth = 0.3f;
		private float maxWallOptimalPosFuseDist = 0.5f;

		private float minSnapLineDist = 0.2f;

		private float hallwayWidth = 1.5f;
		private float minOverlapForDoor = 1.4f;
		private float minHallwayArea = hallwayWidth * hallwayWidth * 2.0f;

		private Builder() {
		}

		public static Builder start() {
			return new Builder();
		}

		public Builder setWallThickness(float value) {
			wallThickness = value;
			return this;
		}

		public Builder setWallHeight(float value) {
			wallHeight = value;
			return this;
		}

		public Builder setWindowWidth(float value) {
			windowWidth = value;
			return this;
		}

		public Builder set(float value) {
			wallThickness = value;
			return this;
		}

		public Builder setWindowTop(float value) {
			windowTop = value;
			return this;
		}

		public Builder setWindowBottom(float value) {
			windowBottom = value;
			return this;
		}

		public Builder setDoorWidth(float value) {
			doorWidth = value;
			minOverlapForDoor = doorWidth + wallThickness;
			return this;
		}

		public Builder setDoorHeight(float value) {
			doorHeight = value;
			return this;
		}

		public Builder setMinRoomSideSize(float value) {
			minRoomSideSize = value;
			return this;
		}

		public Builder setMinSubrectSplitLength(float value) {
			minSubrectSplitLength = value;
			return this;
		}

		public Builder setMinSubrectCut(float value) {
			minSubrectCut = value;
			return this;
		}

		public Builder setMinWallFixLength(float value) {
			minWallFixLength = value;
			return this;
		}

		public Builder setMinWallFixDepth(float value) {
			minWallFixDepth = value;
			return this;
		}

		public Builder setMaxWallOptimalPosFuseDist(float value) {
			maxWallOptimalPosFuseDist = value;
			return this;
		}

		public Builder setMinSnapLineDist(float value) {
			minSnapLineDist = value;
			return this;
		}

		public Builder setHallwayWidth(float value) {
			hallwayWidth = value;
			return this;
		}

		public Builder setMinHallwayArea(float value) {
			minHallwayArea = value;
			return this;
		}

		public FloorPlanAttribs build() {
			return new FloorPlanAttribs(wallThickness, wallHeight,
										windowWidth, windowTop, windowBottom,
										doorWidth, doorHeight, minRoomSideSize,
										minSubrectSplitLength, minSubrectCut,
										minWallFixLength, minWallFixDepth, maxWallOptimalPosFuseDist,
										minSnapLineDist,
										hallwayWidth, minOverlapForDoor, minHallwayArea);
		}

	}
}
