/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package crawler.map.rooms;

import architect.floorplan.FloorPlanAttribs;
import architect.math.segments.Interval;
import architect.math.Vector2D;
import architect.room.Room;
import architect.walls.Opening;
import architect.walls.WallSide;
import com.jme3.math.Vector3f;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import org.lwjgl.util.vector.Vector2f;
import org.poly2tri.Poly2Tri;
import org.poly2tri.geometry.polygon.PolygonPoint;
import org.poly2tri.triangulation.delaunay.DelaunayTriangle;

/**
 *
 * @author VTPlusAKnauer
 */
public class RoomMesh {

	public static final int WALL_MESH_IDX = 0;
	public static final int FLOOR_MESH_IDX = 1;
	public static final int WINDOW_LIGHT_POINTS = 2;
	public static final int MAX_MESH_IDX = 3;
	private final Interval wallVerticalInterval;

	public ArrayList<Polygon> wallPolys = new ArrayList<>();
	public ArrayList<Polygon> floorPolys = new ArrayList<>();
	public ArrayList<ArrayList<Polygon>> polySuperSet = new ArrayList<>(MAX_MESH_IDX);
	public ArrayList<Vector3f> windowPoints = new ArrayList<>();
	public ArrayList<Vector3f> windowPointNormals = new ArrayList<>();

	private static final Vector2D SOUTH_WEST = new Vector2D(0.7071067812f, 0.7071067812f);
	private static final Vector2D NORTH_WEST = new Vector2D(0.7071067812f, -0.7071067812f);

	public RoomMesh(Room room, List<WallSegment> wallSegments, FloorPlanAttribs attribs) {
		polySuperSet.add(wallPolys);
		polySuperSet.add(floorPolys);
		wallVerticalInterval = new Interval(0, attribs.wallHeight);
		createPolys(room, wallSegments, attribs);
	}

	private void createPolys(Room room, List<WallSegment> wallSegments, FloorPlanAttribs attribs) {
		wallPolys.clear();
		floorPolys.clear();

		windowPoints.clear();
		windowPointNormals.clear();

		// create wall polys and prepare polygon for floor triangulation (same as ceiling)
		PolygonPoint points[] = new PolygonPoint[room.getWallSides().size()];
		for (int ws = 0; ws < room.getWallSides().size(); ws++) {
			ArrayList<WallSide> wallSides = new ArrayList<>(room.getWallSides());
			wallPolys.addAll(createWallPolys(ws, wallSides, attribs, wallSegments));

			float wth = attribs.wallThickness * 0.5f;

			WallSide wall = wallSides.get(ws);
			WallSide nextWall = wallSides.get((ws + 1) % wallSides.size());
			Vector2D intersection = wall.intersection(nextWall);
			PolygonPoint point = new PolygonPoint(intersection.x + wth * (wall.getNormal().x + nextWall.getNormal().x),
												  intersection.y + wth * (wall.getNormal().y + nextWall.getNormal().y));
			points[ws] = point;
		}

		// triangulate floor poly
		org.poly2tri.geometry.polygon.Polygon floorPoly = new org.poly2tri.geometry.polygon.Polygon(points);
		Poly2Tri.triangulate(floorPoly);
		List<DelaunayTriangle> floorTriangles = floorPoly.getTriangles();

		// create floor/ceiling triangles
		for (DelaunayTriangle tri : floorTriangles) {
			// floor
			Vector3f a = new Vector3f(tri.points[0].getXf(), 0, tri.points[0].getYf());
			Vector3f b = new Vector3f(tri.points[1].getXf(), 0, tri.points[1].getYf());
			Vector3f c = new Vector3f(tri.points[2].getXf(), 0, tri.points[2].getYf());
			Vector3f n = new Vector3f(0, 1, 0);
			floorPolys.add(new Polygon(a, b, c, n, false));

			// ceiling
			c = new Vector3f(tri.points[0].getXf(), attribs.wallHeight, tri.points[0].getYf());
			b = new Vector3f(tri.points[1].getXf(), attribs.wallHeight, tri.points[1].getYf());
			a = new Vector3f(tri.points[2].getXf(), attribs.wallHeight, tri.points[2].getYf());
			n = new Vector3f(0, -1, 0);
			wallPolys.add(new Polygon(a, b, c, n, false));
		}
	}

	private ArrayList<Polygon> createWallPolys(int wallSideIdx, ArrayList<WallSide> walls,
											   FloorPlanAttribs attribs,
											   List<WallSegment> wallSegments) {
		final float wallThicknessHalf = attribs.wallThickness * 0.5f;

		WallSide wall = walls.get(wallSideIdx);
		int nextWallIdx = (wallSideIdx + 1) % walls.size();
		int prevWallIdx = wallSideIdx - 1;
		if (prevWallIdx < 0)
			prevWallIdx += walls.size();
		WallSide nextWall = walls.get(nextWallIdx);
		WallSide prevWall = walls.get(prevWallIdx);

		if (prevWall.pos > nextWall.pos) { // prevWall must be left / below, nextWall right / above
			WallSide temp = prevWall;
			prevWall = nextWall;
			nextWall = temp;
		}

		if (prevWall.orientation == wall.orientation || nextWall.orientation == wall.orientation) {
			throw new IllegalArgumentException("Consecutive walls have same orientation! Why not one long wall?");
		}
		Vector2f dir = wall.isVertical() ? new Vector2f(0, 1) : new Vector2f(1, 0);

		ArrayList<Polygon> polys = new ArrayList<>();

		Vector2D normal2D = wall.getNormal();
		Vector3f normal = new Vector3f(normal2D.x, 0, normal2D.y);
		Vector2f wallOffset = new Vector2f(normal2D.x * wallThicknessHalf, normal2D.y * wallThicknessHalf);

		Vector2f wallSideStart = Vector2f.add(wall.minPoint().toVector2f(), wallOffset, null);
		Vector2f wallSideEnd = Vector2f.add(wall.maxPoint().toVector2f(), wallOffset, null);
		Vector2f wallSegmentStart = new Vector2f(wallSideStart);
		Vector2f wallCornerOffset = (Vector2f) new Vector2f(dir).scale(wallThicknessHalf);

		if (wall.convexCorner(prevWall))
			Vector2f.add(wallSegmentStart, wallCornerOffset, wallSegmentStart);
		else
			Vector2f.sub(wallSegmentStart, wallCornerOffset, wallSegmentStart);
		if (wall.convexCorner(nextWall))
			Vector2f.sub(wallSideEnd, wallCornerOffset, wallSideEnd);
		else
			Vector2f.add(wallSideEnd, wallCornerOffset, wallSideEnd);

		boolean flipPolys = normal2D.dot(NORTH_WEST) < 0;

		// now create correct side polygons, leaving openenings for doors and windows
		TreeMap<Float, Opening> openings = wall.getOpenings();
		for (Float key : openings.keySet()) {
			Opening opening = openings.get(key);
			Vector2f openingStart = new Vector2f(opening.start * dir.x + wallSideStart.x,
												 opening.start * dir.y + wallSideStart.y);
			Vector2f openingEnd = new Vector2f(opening.end * dir.x + wallSideStart.x,
											   opening.end * dir.y + wallSideStart.y);

			// wall before the door/window
			addWallQuad(wallSegmentStart, openingStart, wallVerticalInterval, normal, flipPolys);
			addWallSegment(wallSegments, WallType.Wall, wallSegmentStart, openingStart, wallVerticalInterval, normal);

			addOpeningWallPolys(openingStart, openingEnd, opening, dir, wallOffset, normal2D, normal, flipPolys);
			addOpeningWallSegments(wallSegments, openingStart, openingEnd, opening, normal);

			if (opening.type == Opening.Type.WINDOW)
				addOpeningVirtualPointLights(openingStart, opening, dir, normal);

			wallSegmentStart.set(openingEnd);
		}

		// this should be the whole or the end piece (wall after last door)
		addWallQuad(wallSegmentStart, wallSideEnd, wallVerticalInterval, normal, flipPolys);
		addWallSegment(wallSegments, WallType.Wall, wallSegmentStart, wallSideEnd, wallVerticalInterval, normal);

		return polys;
	}

	private void addOpeningWallPolys(Vector2f openingStart, Vector2f openingEnd,
									 Opening opening, Vector2f dir, Vector2f wallOffset, Vector2D normal2D,
									 Vector3f normal, boolean flipPolys) {
		// wall above opening
		addWallQuad(openingStart, openingEnd, new Interval(opening.top, wallVerticalInterval.max), normal, flipPolys);

		if (opening.type == Opening.Type.WINDOW) {
			addWallQuad(openingStart, openingEnd, new Interval(0, opening.bottom), normal, flipPolys);
			addWindowPolys(openingStart, openingEnd, opening, dir, wallOffset, flipPolys);
		}

		// bottom (only if door)
		if (opening.type == Opening.Type.DOOR && normal2D.dot(SOUTH_WEST) > 0)
			addDoorPolys(openingStart, openingEnd, opening, dir, wallOffset, flipPolys);
	}

	private void addOpeningWallSegments(List<WallSegment> wallSegments, Vector2f openingStart, Vector2f openingEnd,
										Opening opening, Vector3f normal) {
		addWallSegment(wallSegments, opening.type == Opening.Type.WINDOW
									 ? WallType.WallAboveWindow : WallType.WallAboveDoor,
					   openingStart, openingEnd, new Interval(opening.top, wallVerticalInterval.max), normal);

		addWallSegment(wallSegments, WallType.WallBelowWindow, openingStart, openingEnd, new Interval(0, opening.bottom), normal);
	}

	private void addOpeningVirtualPointLights(Vector2f openingStart, Opening opening, Vector2f dir, Vector3f normal) {
		for (float vplw = 0; vplw < opening.width; vplw += 0.1f) {
			for (float vplh = opening.bottom; vplh < opening.top; vplh += 0.1f) {
				windowPoints.add(new Vector3f(openingStart.x + vplw * dir.x, vplh, openingStart.y + vplw * dir.y));
				windowPointNormals.add(normal);
			}
		}
	}

	private void addDoorPolys(Vector2f openingStart, Vector2f openingEnd, Opening opening, Vector2f dir, Vector2f wallOffset, boolean flip) {
		addWallQuad(new Vector3f(openingStart.x, opening.bottom, openingStart.y),
					new Vector3f(openingEnd.x, opening.bottom, openingEnd.y),
					new Vector3f(openingEnd.x - wallOffset.x * 2, opening.bottom, openingEnd.y - wallOffset.y * 2),
					new Vector3f(openingStart.x - wallOffset.x * 2, opening.bottom, openingStart.y - wallOffset.y * 2),
					new Vector3f(0, 1, 0), flip);
	}

	private void addWindowPolys(Vector2f openingStart, Vector2f openingEnd, Opening opening, Vector2f dir, Vector2f wallOffset, boolean flip) {
		// layout:		d  c
		//				 []
		//				a  b
		// ai, bi, ci, di are same but inset towards outside
		Vector3f a = new Vector3f(openingStart.x, opening.bottom, openingStart.y);
		Vector3f b = new Vector3f(openingEnd.x, opening.bottom, openingEnd.y);
		Vector3f c = new Vector3f(openingEnd.x, opening.top, openingEnd.y);
		Vector3f d = new Vector3f(openingStart.x, opening.top, openingStart.y);

		float xInset = wallOffset.x * -3;
		float yInset = wallOffset.y * -3;

		Vector3f ai = new Vector3f(openingStart.x + xInset, opening.bottom, openingStart.y + yInset);
		Vector3f bi = new Vector3f(openingEnd.x + xInset, opening.bottom, openingEnd.y + yInset);
		Vector3f ci = new Vector3f(openingEnd.x + xInset, opening.top, openingEnd.y + yInset);
		Vector3f di = new Vector3f(openingStart.x + xInset, opening.top, openingStart.y + yInset);

		addWallQuad(c, d, di, ci, new Vector3f(0, -1, 0), flip); // top	
		addWallQuad(a, b, bi, ai, new Vector3f(0, 1, 0), flip); // bottom
		addWallQuad(a, ai, di, d, new Vector3f(dir.x, 0, dir.y), flip); // start vertical
		addWallQuad(b, c, ci, bi, new Vector3f(-dir.x, 0, -dir.y), flip); // end vertical
	}

	private void addWallQuad(Vector2f start, Vector2f end, Interval vertical, Vector3f normal, boolean flip) {
		Vector3f lowStart = new Vector3f(start.x, vertical.min, start.y);
		Vector3f highStart = new Vector3f(start.x, vertical.max, start.y);
		Vector3f lowEnd = new Vector3f(end.x, vertical.min, end.y);
		Vector3f highEnd = new Vector3f(end.x, vertical.max, end.y);

		addWallQuad(lowStart, lowEnd, highEnd, highStart, normal, flip);
	}

	private void addWallQuad(Vector3f a, Vector3f b, Vector3f c, Vector3f d, Vector3f normal, boolean flip) {
		wallPolys.add(new Polygon(a, b, c, normal, flip));
		wallPolys.add(new Polygon(c, d, a, normal, flip));
	}

	private void addWallSegment(List<WallSegment> wallSegments, WallType type, Vector2f start, Vector2f end,
								Interval vertical, Vector3f normal) {
		wallSegments.add(new WallSegment(type,
										 new Vector3f((start.x + end.x) * 0.5f, vertical.mid(), (start.y + end.y) * 0.5f),
										 normal,
										 new Vector3f((Math.abs(end.x - start.x) + Math.abs(end.y - start.y)) * 0.5f,
													  vertical.length() * 0.5f, 0)));
	}
}
