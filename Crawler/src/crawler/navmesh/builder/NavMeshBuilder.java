/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package crawler.navmesh.builder;

import architect.floorplan.FloorPlan;
import architect.floorplan.FloorPlanAttribs;
import architect.math.Vector2D;
import architect.room.Room;
import architect.walls.Opening;
import architect.walls.WallSide;
import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector3f;
import convdecomp.MCD;
import convdecomp.geometry.DecompPoly;
import convdecomp.geometry.Point;
import crawler.navmesh.NavMesh;
import crawler.navmesh.NavMeshEntrance;
import crawler.navmesh.navpoly.NavPoly;
import java.util.*;

/**
 *
 * @author VTPlusAKnauer
 */
public class NavMeshBuilder {

	public static NavMesh build(FloorPlan fp, Vector3f positionOffset, BoundingBox bbox, String name, FloorPlanAttribs attribs) {
		ArrayList<NavPoly> navPolys = new ArrayList<>();
		ArrayList<Vector3f> vertices = new ArrayList<>();

		ArrayList<DoorLine> allDoorLines = new ArrayList<>();

		for (Room room : fp.rooms) {
			ArrayList<Point> points = new ArrayList<>(room.getWallSides().size() + 4); // +4 for min 2 doors
			int vertexBase = vertices.size();

			// one wall side (index of start point) can have multiple doors (arraylist of doors)
			TreeMap<Integer, ArrayList<DoorLine>> doorLines = new TreeMap<>();

			WallSide previousWallSide = room.getWallSides().getLast();
			for (WallSide wallSide : room.getWallSides()) {

				Vector2D intersection = previousWallSide.intersection(wallSide);
				float wth = attribs.wallThickness * 0.5f;
				Point wallStart = new Point(intersection.x + wth * (previousWallSide.getNormal().x + wallSide.getNormal().x),
											intersection.y + wth * (previousWallSide.getNormal().y + wallSide.getNormal().y));
				points.add(wallStart);

				// add any existing doors to a list for later
				for (Opening opening : wallSide.getOpenings().values()) {
					if (opening.type == Opening.Type.DOOR) {
						Point doorStart, doorEnd;
						if (wallSide.isHorizontal()) {
							doorStart = new Point(wallSide.min + opening.start + wth * wallSide.getNormal().x,
												  wallSide.pos + wth * wallSide.getNormal().y);
							doorEnd = new Point(wallSide.min + opening.end + wth * wallSide.getNormal().x,
												wallSide.pos + wth * wallSide.getNormal().y);

							// insert door points in correct order (counter clockwise)
							if (Math.abs(doorStart.x - wallStart.x) > Math.abs(doorEnd.x - wallStart.x)) {
								Point t = doorEnd;
								doorEnd = doorStart;
								doorStart = t;
							}

						}
						else { // vertical wall
							doorStart = new Point(wallSide.pos + wth * wallSide.getNormal().x,
												  wallSide.min + opening.start + wth * wallSide.getNormal().y);
							doorEnd = new Point(wallSide.pos + wth * wallSide.getNormal().x,
												wallSide.min + opening.end + wth * wallSide.getNormal().y);

							// insert door points in correct order (counter clockwise)
							if (Math.abs(doorStart.y - wallStart.y) > Math.abs(doorEnd.y - wallStart.y)) {
								Point t = doorEnd;
								doorEnd = doorStart;
								doorStart = t;
							}
						}

						// add the two door points to the map (indexed by the starting point index)
						ArrayList<DoorLine> doorList = doorLines.get(points.size() - 1);
						if (doorList == null)
							doorLines.put(points.size() - 1, doorList = new ArrayList<>());
						else {
							System.out.println("________________________________________________________________");
						}
						Room neighbor = opening.neighborRoom;
						if (!fp.rooms.contains(neighbor))
							neighbor = null;
						doorList.add(new DoorLine(room, neighbor, doorStart, doorEnd));
					}
				}

				previousWallSide = wallSide;
			}

			// decompose room polygon into convex subpolygons -> diagonals
			// do this without considering doors, since they are collinear to their wall side
			DecompPoly dp = MCD.compute(points);
			dp.recoverSolution(vertexBase, vertexBase);
			TreeMap<Integer, ArrayList<Integer>> diagonals = new TreeMap<>();
			PolyDecomposer.constructDiagonals(dp, diagonals, 0, dp.sp.size() - 1);

			LinkedList<LinkedList<Integer>> polygons = new LinkedList<>();

			int polyBase = navPolys.size();
			for (Point point : points) {
				vertices.add(new Vector3f(point.x + positionOffset.x, positionOffset.y, point.y + positionOffset.z));
			}

			createPolygonsFromDiagonals(polygons, vertices, points, diagonals, doorLines, positionOffset, vertexBase);
			createPolysFromPoints(polygons, navPolys, vertices, room);
			connectPolyNeighbors(polyBase, navPolys);

			for (ArrayList<DoorLine> doorLineList : doorLines.values()) {
				allDoorLines.addAll(doorLineList);
			}
		}

		ArrayList<NavMeshEntrance> entrances = createDoorPolys(attribs, navPolys, allDoorLines);

		NavMesh mesh = new NavMesh(name, navPolys, bbox);

		for (NavMeshEntrance entrance : entrances) {
			mesh.addEntrance(entrance);
		}

		return mesh;
	}

	private static void createPolygonsFromDiagonals(LinkedList<LinkedList<Integer>> polygons,
													ArrayList<Vector3f> vertices,
													ArrayList<Point> points,
													TreeMap<Integer, ArrayList<Integer>> diagonals,
													TreeMap<Integer, ArrayList<DoorLine>> doorLines,
													Vector3f positionOffset,
													int vertexBase) {

		boolean[] pointProcessed = new boolean[points.size()];
		for (int p = 0; p < points.size(); p++) {
			if (!pointProcessed[p]) {
				LinkedList<Integer> polyVertexList = new LinkedList<>();
				polygons.add(polyVertexList);
				int pi = p;
				polyVertexList.add(vertexBase + pi);

				Integer previous = null;
				while (polyVertexList.size() < 2 || !Objects.equals(polyVertexList.getFirst(), polyVertexList.getLast())) {
					int oldPi = pi;

					// end case where we are one edge away from first vertex in the list
					if ((p == 0 && pi == points.size() - 1) || vertexBase + pi == polyVertexList.getFirst() - 1) {
						ArrayList<DoorLine> doorList = doorLines.get(oldPi);
						if (doorList != null) {
							for (DoorLine door : doorList) {
								addDoorLine(door, positionOffset, vertices, polyVertexList);
							}
						}

						polyVertexList.add(polyVertexList.getFirst());
						break;
					}

					ArrayList<Integer> diagonalList = diagonals.get(pi);
					if (diagonalList == null || (diagonalList.size() == 1 && diagonalList.get(0).equals(previous))) {
						pi = (pi + 1) % points.size();

						// this is a wall (not diagonal) -> find and add doors
						ArrayList<DoorLine> doorList = doorLines.get(oldPi);
						if (doorList != null) {
							for (DoorLine door : doorList) {
								addDoorLine(door, positionOffset, vertices, polyVertexList);
							}
						}
					}
					else { // find correct next diagonal
						int closestDistanceToStart = Integer.MAX_VALUE;
						for (Integer diagonal : diagonalList) {
							if (!diagonal.equals(previous)) { // don't go back if previous edge was diagonal
								int dist = Math.abs(diagonal - p);
								dist = Math.min(dist, points.size() - dist); // ring distance (mod)
								if (dist < closestDistanceToStart) {
									pi = diagonal;
									closestDistanceToStart = dist;
								}
							}
						}
					}

					pointProcessed[pi] = true;

					polyVertexList.add(vertexBase + pi);
					previous = oldPi;
				}
			}
		}
	}

	private static void addDoorLine(DoorLine door, Vector3f positionOffset, ArrayList<Vector3f> vertices,
									LinkedList<Integer> polyVertexList) {
		door.setVertexIdx(vertices.size());
		polyVertexList.add(vertices.size());
		polyVertexList.add(vertices.size() + 1);
		Vector3f firstDoorVertex = new Vector3f(door.start.x + positionOffset.x,
												positionOffset.y, door.start.y + positionOffset.z);
		Vector3f secondDoorVertex = new Vector3f(door.end.x + positionOffset.x,
												 positionOffset.y, door.end.y + positionOffset.z);
		vertices.add(firstDoorVertex);
		vertices.add(secondDoorVertex);
		door.setStartVertex(firstDoorVertex);
		door.setEndVertex(secondDoorVertex);
	}

	private static void createPolysFromPoints(LinkedList<LinkedList<Integer>> polygons,
											  ArrayList<NavPoly> navPolys, ArrayList<Vector3f> vertices,
											  Room room) {
		for (LinkedList<Integer> poly : polygons) {
			poly.removeLast(); // last and first are the same point / index

			ArrayList<Vector3f> polyVertices = new ArrayList<>(poly.size());
			Iterator<Integer> it = poly.descendingIterator();  // y to z mapping reverses ccw vertex order
			while (it.hasNext()) {
				int v = it.next();
				polyVertices.add(vertices.get(v));
			}

			navPolys.add(new NavPoly(polyVertices));
		}
	}

	private static void connectPolyNeighbors(int polyBase, List<NavPoly> navPolys) {
		for (int pA = polyBase; pA < navPolys.size(); pA++) {
			for (int pB = pA + 1; pB < navPolys.size(); pB++) {
				NavPoly polyA = navPolys.get(pA);
				NavPoly polyB = navPolys.get(pB);

				for (int v = 0; v < polyA.getVertices().size(); v++) {
					Vector3f firstVertex = polyA.getVertex(v);
					Vector3f secondVertex = polyA.getVertex(v + 1);

					if (polyB.containsVertex(firstVertex) && polyB.containsVertex(secondVertex)) {
						polyA.setNeighbor(polyB, v);
						polyB.setNeighbor(polyA, secondVertex);
					}
				}
			}
		}
	}

	private static ArrayList<NavMeshEntrance> createDoorPolys(FloorPlanAttribs attribs,
															  ArrayList<NavPoly> navPolys,
															  ArrayList<DoorLine> doorLines) {
		ArrayList<NavMeshEntrance> entrances = new ArrayList<>();

		outerDoorLineLoop:
		for (int da = 0; da < doorLines.size(); da++) {
			DoorLine doorLineA = doorLines.get(da);

			if (doorLineA.hasNullRoom()) {
				Vector3f doorVertex = doorLineA.getStartVertex();
				for (NavPoly poly : navPolys) {
					if (poly.containsVertex(doorVertex)) {
						NavPoly entrancePoly = createEntranceNavPoly(attribs, doorLineA);
						navPolys.add(entrancePoly);
						entrances.add(new NavMeshEntrance("entrance_fp", entrancePoly, 2));
						connectPolyNeighbors(0, Arrays.asList(entrancePoly, poly));
						break;
					}
				}
				doorLineA.setNavPolyCreated(true);
			}
			else if (!doorLineA.isNavPolyCreated()) {
				for (int db = da + 1; db < doorLines.size(); db++) {
					DoorLine doorLineB = doorLines.get(db);
					if (!doorLineB.isNavPolyCreated() && doorLineA.isOppositeOf(doorLineB)) {

						NavPoly doorPoly = createDoorNavPoly(doorLineA, doorLineB);
						connectDoorNavPolyNeighbors(doorPoly, navPolys);

						navPolys.add(doorPoly);

						doorLineA.setNavPolyCreated(true);
						doorLineB.setNavPolyCreated(true);
						continue outerDoorLineLoop;
					}
				}
			}
		}

		return entrances;
	}

	private static NavPoly createEntranceNavPoly(FloorPlanAttribs attribs, DoorLine doorLine) {
		int v = doorLine.getVertexIdx();
		Vector3f v1 = doorLine.getStartVertex();
		Vector3f v2 = doorLine.getEndVertex();

		Vector3f normal = v2.subtract(v1).crossLocal(0, -1, 0).normalizeLocal().multLocal(attribs.wallThickness * 0.5f);
		Vector3f v3 = v2.add(normal);
		Vector3f v4 = v1.add(normal);

		return new NavPoly(Arrays.asList(v1, v2, v3, v4));
	}

	private static NavPoly createDoorNavPoly(DoorLine doorLineA, DoorLine doorLineB) {
		return new NavPoly(Arrays.asList(doorLineA.getStartVertex(), doorLineA.getEndVertex(),
										 doorLineB.getStartVertex(), doorLineB.getEndVertex()));
	}

	private static void connectDoorNavPolyNeighbors(NavPoly doorPoly, ArrayList<NavPoly> navPolys) {
		for (NavPoly poly : navPolys) {
			for (int v = 0; v < poly.getVertices().size(); v++) {
				if (doorPoly.getVertex(0) == poly.getVertex(v)) {
					doorPoly.setNeighbor(poly, 0);

					if (doorPoly.getVertex(1) == poly.getVertex(v + 1))
						poly.setNeighbor(doorPoly, v);
					else
						poly.setNeighbor(doorPoly, v - 1);

					break;
				}
				else if (doorPoly.getVertex(2) == poly.getVertex(v)) {
					doorPoly.setNeighbor(poly, 2);

					if (doorPoly.getVertex(3) == poly.getVertex(v + 1))
						poly.setNeighbor(doorPoly, v);
					else
						poly.setNeighbor(doorPoly, v - 1);

					break;
				}
			}
		}
	}

}
