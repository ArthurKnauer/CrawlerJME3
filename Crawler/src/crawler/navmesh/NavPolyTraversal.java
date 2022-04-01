package crawler.navmesh;

import com.jme3.math.Vector3f;
import crawler.navmesh.navpoly.NavPoly;
import crawler.navmesh.navpoly.NavPolyNeighbor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author VTPlusAKnauer
 */
public class NavPolyTraversal {

	private enum VectorVisibility {

		OutsideLeft, OutsideRight, Inside
	};

	static List<Vector3f> plan(List<NavPoly> path, Vector3f start, Vector3f dest, float radius) {
		if (path == null || path.isEmpty())
			return Collections.<Vector3f>emptyList();
		else if (path.size() == 1)
			return Arrays.asList(start, dest);

		ArrayList<Vector3f> plannedPath = new ArrayList<>(2);
		plannedPath.add(start);
		Vector3f nextCorner = start.clone();

		for (int poly = 0; poly < path.size() - 1;) {
			int nextPoly = nextCorner(path, poly, nextCorner, nextCorner, dest, radius);
			if (nextPoly > 0) {
				plannedPath.add(nextCorner.clone());
				poly = nextPoly;
			}
			else {
				break;
			}
		}

		plannedPath.add(dest);

		return fluidPath(plannedPath, radius);
	}

	private static List<Vector3f> fluidPath(ArrayList<Vector3f> path, float radius) {
		ArrayList<Vector3f> fluidPath = new ArrayList<>(path.size());

		Vector3f previousNode = null;
		for (Vector3f node : path) {
			if (previousNode != null) {
				if (previousNode.distanceSquared(node) > 0.01) {
					Vector3f offset = node.subtract(previousNode).normalizeLocal().multLocal(radius);
					//fluidPath.add(node);
					fluidPath.add(node.add(offset));
				}
				else {
					fluidPath.add(node);
				}
			}
			previousNode = node;
		}

		return fluidPath;
	}

	private static int nextCorner(List<NavPoly> path, int startPoly, Vector3f start, Vector3f nextCorner, Vector3f dest, float radius) {
		Vector3f currentPos = start;
		Vector3f lastVisRight = null, lastVisLeft = null;
		int lastVisLeftPoly = startPoly, lastVisRightPoly = startPoly;
		VectorVisibility pos;

		for (int poly = startPoly; poly < path.size() - 1; poly++) {
			NavPoly currentPoly = path.get(poly);

			NavPolyNeighbor neighbor = currentPoly.getNeighbor(path.get(poly + 1));
			Vector3f firstPortalVertex = currentPoly.getVertex(neighbor.getEdge());
			Vector3f secondPortalVertex = currentPoly.getVertex(neighbor.getEdge() + 1);
			Vector3f portalDirRadius = firstPortalVertex.subtract(secondPortalVertex).normalizeLocal().multLocal(radius);
			Vector3f visRight = firstPortalVertex.subtract(portalDirRadius);
			Vector3f visLeft = secondPortalVertex.add(portalDirRadius);

			if (poly == startPoly) {
				lastVisLeft = visLeft;
				lastVisRight = visRight;
			}
			else {
				pos = getVectorVisibility(currentPos, lastVisLeft, lastVisRight, visLeft);

				if (pos == VectorVisibility.Inside) {
					lastVisLeft = visLeft;
					lastVisLeftPoly = poly;
				}
				else if (pos == VectorVisibility.OutsideRight) {
					nextCorner.set(lastVisRight);
					return lastVisRightPoly + 1;
				}

				pos = getVectorVisibility(currentPos, lastVisLeft, lastVisRight, visRight);

				if (pos == VectorVisibility.Inside) {
					lastVisRight = visRight;
					lastVisRightPoly = poly;
				}
				else if (pos == VectorVisibility.OutsideLeft) {
					nextCorner.set(lastVisLeft);
					return lastVisLeftPoly + 1;
				}
			}
		}

		pos = getVectorVisibility(start, lastVisLeft, lastVisRight, dest);
		if (pos == VectorVisibility.OutsideLeft) {
			nextCorner.set(lastVisLeft);
			return lastVisLeftPoly + 1;
		}
		else if (pos == VectorVisibility.OutsideRight) {
			nextCorner.set(lastVisRight);
			return lastVisRightPoly + 1;
		}

		return -1;
	}

	private static VectorVisibility getVectorVisibility(Vector3f start, Vector3f left, Vector3f right, Vector3f destPos) {
		float dirRayX = destPos.x - start.x;
		float dirRayZ = destPos.z - start.z;
		float rightCrossY = (((right.z - start.z) * dirRayX) - ((right.x - start.x) * dirRayZ));
		float leftCrossY = (((left.z - start.z) * dirRayX) - ((left.x - start.x) * dirRayZ));

		if (rightCrossY > 0 && leftCrossY < 0)
			return VectorVisibility.Inside;
		else if (rightCrossY < 0)
			return VectorVisibility.OutsideRight;
		else
			return VectorVisibility.OutsideLeft;
	}

}
