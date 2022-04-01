package crawler.util;

import com.jme3.math.Vector3f;
import java.util.Optional;

/**
 *
 * @author VTPlusAKnauer
 */
public class Intersection {

	public static Optional<Vector3f> lineWithSphere(Vector3f lineOrigin, Vector3f lineDir,
													Vector3f sphereCenter, float sphereRadius) {
		Vector3f d = lineOrigin.subtract(sphereCenter);
		float a = lineDir.dot(lineDir);
		float b = 2 * d.dot(lineDir);
		float c = d.dot(d) - sphereRadius * sphereRadius;

		// intersection = lineOrigin + lambda * lineDir
		// d°d + lambda²*(lineOrigin°lineOrigin) + 2*lambda*(d°lineOrigin) = radius²
		// a*lambda² + b*lambda + c = 0
		float D = b * b - 4 * a * c;
		if (D < 0)
			return Optional.empty(); // no solution -> no intersection

		float rootOfD = (float) Math.sqrt(D);

		float lambda1 = (-b + rootOfD) / (2 * a);
		float lambda2 = (-b - rootOfD) / (2 * a);

		// the smallest positive is the correct lambda
		float lambda = (lambda1 < 0 || lambda2 < 0)
					   ? Math.max(lambda1, lambda2)
					   : Math.min(lambda1, lambda2);
		if (lambda < 0)
			return Optional.empty(); // intersection(s) "behind" lineOrigin

		return Optional.of(lineDir.mult(lambda).addLocal(lineOrigin));
	}

}
