package modelcompiler.modifiers.shelffinder;

import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import common.attributes.Shelves;
import common.material.DefaultMaterial;
import java.util.ArrayList;
import java.util.List;
import lombok.experimental.UtilityClass;

/**
 *
 * @author VTPlusAKnauer
 */
@UtilityClass
public class ShelfFinder {

	public static void find(AssetManager assetManager, Spatial spatial, Node clusterNode) {
		Node node = (Node) spatial;

		ArrayList<BoundingBox> shelves = new ArrayList<>();
		for (Spatial child : node.getChildren()) {
			Geometry geometry = (Geometry) child;

			List<TriangleCluster> listOfClusters = FlatTriangleClusterFinder.find(geometry.getMesh());
			for (TriangleCluster cluster : listOfClusters) {
				
				ShelfBuilder.fromTriangleCluster(cluster).ifPresent(shelf -> {
					Mesh mesh = TriangleClusterMesher.buildMesh(cluster);
					Geometry geom = new Geometry("cluster", mesh);
					geom.setMaterial(DefaultMaterial.withColor(ColorRGBA.randomColor().toVector3f()));
					clusterNode.attachChild(geom);
					shelves.add(shelf);
				});
			}
		}
		
		spatial.updateGeometricState();

		Shelves shelvesAttrib = new Shelves(shelves);
		node.addAttribute(shelvesAttrib);
	}
}
