package crawler.navmesh.builder;

/**
 *
 * @author VTPlusAKnauer
 */


public class NavMeshBuilderStairCase {
	
//	public static NavMesh build(Vector3f positionOffset, BoundingBox bbox, String name) {
//		ArrayList<NavPoly> navPolys = new ArrayList<>(6);
//		ArrayList<Vector3f> vertices = new ArrayList<>(18);
//		
//		Plane lowerPlane = new Plane(new Vector3f(0, 1, 0), positionOffset.y - 1.5f);
//		Plane middlePlane = new Plane(new Vector3f(0, 1, 0), positionOffset.y);
//		Plane upperPlane = new Plane(new Vector3f(0, 1, 0), positionOffset.y + 1.5f);
//
//		vertices.add(new Vector3f(6.5f, -1.5f, 1.5f)); // lower platform
//		vertices.add(new Vector3f(5.0f, -1.5f, 1.5f));
//		vertices.add(new Vector3f(5.0f, -1.5f, 0));
//		vertices.add(new Vector3f(6.5f, -1.5f, 0));
//
//		vertices.add(new Vector3f(2.0f, 0, 1.5f)); // middle -y platform
//		vertices.add(new Vector3f(1.5f, 0, 1.5f)); // door start
//		vertices.add(new Vector3f(0.2f, 0, 1.5f));	// door end
//		vertices.add(new Vector3f(0.0f, 0, 1.5f));
//
//		vertices.add(new Vector3f(0, 0, 0));
//		vertices.add(new Vector3f(2, 0, 0));
//
//		vertices.add(new Vector3f(2.0f, 0, -1.5f)); // middle +y platform
//		vertices.add(new Vector3f(1.5f, 0, -1.5f)); // door start
//		vertices.add(new Vector3f(0.2f, 0, -1.5f));	// door end
//		vertices.add(new Vector3f(0.0f, 0, -1.5f));
//
//		vertices.add(new Vector3f(6.5f, 1.5f, 0)); // upper platform
//		vertices.add(new Vector3f(5.0f, 1.5f, 0));
//		vertices.add(new Vector3f(5.0f, 1.5f, -1.5f));
//		vertices.add(new Vector3f(6.5f, 1.5f, -1.5f));
//		
//		for (Vector3f vertex : vertices) {
//			vertex.addLocal(positionOffset);
//		}
//		
//		Plane lowerStairsPlane = new Plane();
//		lowerStairsPlane.setPlanePoints(vertices.get(2), vertices.get(9), vertices.get(4));
//		Plane upperStairsPlane = new Plane();
//		upperStairsPlane.setPlanePoints(vertices.get(15), vertices.get(16), vertices.get(10));
//
//		NavPoly lowerPF = new NavPoly(0, new int[]{3, 2, 1, 0}, vertices);
//		NavPoly middleTPF = new NavPoly(1, new int[]{9, 8, 7, 6, 5, 4}, vertices);
//		NavPoly middleLPF = new NavPoly(2, new int[]{10, 11, 12, 13, 8, 9}, vertices);
//		NavPoly upperPF = new NavPoly(3, new int[]{17, 16, 15, 14}, vertices);
//		NavPoly lowerStairs = new NavPoly(4, new int[]{2, 9, 4, 1}, vertices);
//		NavPoly upperStairs = new NavPoly(5, new int[]{15, 16, 10, 9}, vertices);		
//
//		middleTPF.setNeighbor(middleLPF, 0);
//		middleLPF.setNeighbor(middleTPF, 4);
//
//		lowerPF.setNeighbor(lowerStairs, 1);
//		lowerStairs.setNeighbor(lowerPF, 3);
//
//		middleTPF.setNeighbor(lowerStairs, 5);
//		lowerStairs.setNeighbor(middleTPF, 1);
//
//		middleLPF.setNeighbor(upperStairs, 5);
//		upperStairs.setNeighbor(middleLPF, 2);
//
//		upperPF.setNeighbor(upperStairs, 1);
//		upperStairs.setNeighbor(upperPF, 0);
//
//		navPolys.add(lowerPF);
//		navPolys.add(middleTPF);
//		navPolys.add(middleLPF);
//		navPolys.add(upperPF);
//		navPolys.add(lowerStairs);
//		navPolys.add(upperStairs);
//
//		for (NavPoly navPoly : navPolys) {
//			navPoly.getCenter().addLocal(positionOffset);
//		}
//
//		NavMesh mesh = new NavMesh(vertices, navPolys, bbox, name);
//		
//		mesh.addEntrance(new NavMeshEntrance("entranceTop", 6, 5, 1));
//		mesh.addEntrance(new NavMeshEntrance("entranceBottom", 6, 5, 2));
//		mesh.addEntrance(new NavMeshEntrance("bottom", 12, 12, 0));
//		mesh.addEntrance(new NavMeshEntrance("top", 15, 14, 3));
//		
//		return mesh;
//	}
}
