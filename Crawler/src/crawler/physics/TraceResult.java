/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package crawler.physics;

import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.math.Vector3f;
import lombok.Builder;
import lombok.Getter;

/**
 *
 * @author VTPlusAKnauer
 */
public class TraceResult {

	@Getter private final PhysicsCollisionObject collisionObject;
	@Getter private final Vector3f hitNormalLocal;
	@Getter private final boolean normalInWorldSpace;

	@Getter private final Vector3f traceStart;
	@Getter private final Vector3f traceEnd;

	@Getter private final float hitFraction;

	private Vector3f hitLocation;

	@Builder
	private TraceResult(PhysicsCollisionObject collisionObject,
						Vector3f hitNormalLocal,
						boolean normalInWorldSpace,
						Vector3f traceStart,
						Vector3f traceEnd,
						float hitFraction) {

		this.collisionObject = collisionObject;
		this.hitNormalLocal = hitNormalLocal;
		this.normalInWorldSpace = normalInWorldSpace;
		this.traceStart = traceStart;
		this.traceEnd = traceEnd;
		this.hitFraction = hitFraction;
	}

	public Vector3f getHitLocation() { // lombok lazy getter doesnt worik in netbeans :(
		if (hitLocation == null) {
			hitLocation = new Vector3f().interpolate(traceStart, traceEnd, hitFraction);
		}
		return hitLocation;
	}
}
