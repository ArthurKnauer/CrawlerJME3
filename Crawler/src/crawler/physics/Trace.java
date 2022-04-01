package crawler.physics;

import com.jme3.bullet.collision.PhysicsRayTestResult;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import crawler.debug.DebugGeometry;
import crawler.main.Globals;
import static crawler.properties.CrawlerProperties.PROPERTIES;
import java.util.List;
import java.util.Optional;
import lombok.experimental.UtilityClass;

/**
 *
 * @author VTPlusAKnauer
 */
@UtilityClass
public class Trace {

	private static final boolean DEBUG = PROPERTIES.getBoolean("Traces.debug");

	public static Optional<TraceResult> traceSpatialForward(Spatial spatial, float traceLength) {
		Vector3f traceStart = spatial.getWorldTranslation();
		Vector3f traceEnd = spatial.getWorldForward().multLocal(traceLength).addLocal(traceStart);
			
		return traceRay(traceStart, traceEnd);
	}

	public static Optional<TraceResult> traceRay(Vector3f start, Vector3f end) {
		List<PhysicsRayTestResult> hitResults = Globals.getPhysicsSpace().rayTest(start, end);

		if (hitResults.size() > 0) {
			// find hit closest to the traceRay start (first hit)
			float closestHitFraction = 1.0f;
			PhysicsRayTestResult closestHit = null;
			for (PhysicsRayTestResult hit : hitResults) {
				if (DEBUG) {
					Vector3f hitPos = new Vector3f().interpolate(start, end, hit.getHitFraction());
					DebugGeometry.addWireBox(hitPos, 0.02f, ColorRGBA.Blue);
				}

				if (hit.getHitFraction() < closestHitFraction) {
					closestHit = hit;
					closestHitFraction = hit.getHitFraction();
				}
			}
			
			TraceResult traceResult = TraceResult.builder()
					.collisionObject(closestHit.getCollisionObject())
					.hitNormalLocal(closestHit.getHitNormalLocal())
					.normalInWorldSpace(closestHit.isNormalInWorldSpace())
					.hitFraction(closestHit.getHitFraction())
					.traceStart(start)
					.traceEnd(end).build();
			
			return Optional.ofNullable(traceResult);
		}

		return Optional.empty();
	}

}
