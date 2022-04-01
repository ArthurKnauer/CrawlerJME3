/*
 * To change this license header, choose License Headers in Project ModelCompilerProperties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelcompiler.modifiers.shelffinder;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.Triangle;
import com.jme3.math.Vector3f;
import static java.lang.Math.abs;
import java.util.Collections;
import java.util.Optional;
import java.util.TreeMap;
import lombok.experimental.UtilityClass;
import static modelcompiler.properties.ModelCompilerProperties.PROPERTIES;
import org.apache.commons.lang3.ArrayUtils;
import static java.lang.Math.abs;

/**
 *
 * @author VTPlusAKnauer
 */
@UtilityClass
class ShelfBuilder {

	public final static float MIN_VALID_WIDTH = PROPERTIES.getFloat("ShelfBuilder.minValidWidth");
	public final static float MIN_VALID_HEIGHT = PROPERTIES.getFloat("ShelfBuilder.minValidHeight");
	public final static float MAX_SHELF_HEIGHT = PROPERTIES.getFloat("ShelfBuilder.maxShelfHeight");
	public final static float TRIANGLE_HALF_INFLUENCE_DIST = PROPERTIES.getFloat("ShelfBuilder.triangleHalfInfluenceDist");

	static Optional<BoundingBox> fromTriangleCluster(TriangleCluster cluster) {
		BoundingBox clusterBBox = triangleClusterBBox(cluster);

		if (isBBoxWideEnoughForShelf(clusterBBox)) {
			return Optional.of(clusterBBoxToShelfBBox(cluster, clusterBBox));
		}

		return Optional.empty();
	}

	private static BoundingBox triangleClusterBBox(TriangleCluster cluster) {
		int[] trindices = ArrayUtils.toPrimitive(cluster.getIndices().toArray(new Integer[cluster.getIndices().size()]));

		BoundingBox bbox = new BoundingBox();
		bbox.computeFromTris(trindices, cluster.getMesh(), 0, trindices.length);

		return bbox;
	}

	private static boolean isBBoxWideEnoughForShelf(BoundingBox bbox) {
		return bbox.getXExtent() > MIN_VALID_WIDTH
			   && bbox.getZExtent() > MIN_VALID_WIDTH;
	}

	private static BoundingBox clusterBBoxToShelfBBox(TriangleCluster cluster, BoundingBox clusterBBox) {
		float bottom = findShelfBottom(cluster);
		float top = bottom + MAX_SHELF_HEIGHT;

		Vector3f shelfMin = clusterBBox.getMin();
		Vector3f shelfMax = clusterBBox.getMax();
		shelfMin.y = bottom;
		shelfMax.y = top;
		
		return new BoundingBox(shelfMin, shelfMax);
	}

	private static float findShelfBottom(TriangleCluster cluster) {
		TreeMap<Float, Float> heightsByAreaWeight = new TreeMap<>();
		Triangle triangle = new Triangle();
		for (Integer trindex : cluster.getIndices()) {
			cluster.getMesh().getTriangle(trindex, triangle);

			float height = triangle.getCenter().y;
			float area = triangle.getArea();

			heightsByAreaWeight.put(height, area);
		}

		for (Float height : heightsByAreaWeight.keySet()) {
			float areaWeight = heightsByAreaWeight.get(height);
			for (Float heightInfluenced : heightsByAreaWeight.keySet()) {
				if (!heightInfluenced.equals(height)) {
					float distance = abs(heightInfluenced - height);
					float influence = 1.0f / (1 + distance / TRIANGLE_HALF_INFLUENCE_DIST);

					float oldWeight = heightsByAreaWeight.get(heightInfluenced);
					float newWeight = oldWeight + influence * areaWeight;
					heightsByAreaWeight.put(heightInfluenced, newWeight);
				}

			}
		}

		return Collections.max(heightsByAreaWeight.entrySet(),
							   (entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1).getKey();

	}
}
