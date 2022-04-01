/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package architect.room.optimalrect;

import architect.Constants;
import architect.math.Vector2D;

/**
 *
 * @author Arthur
 */
class CollisionResolve {

	private final Vector2D moveSum = new Vector2D();
	private final Vector2D weightSum = new Vector2D();

	CollisionResolve() {
	}

	void addResolve(Vector2D resolve) {
		if (resolve.lengthSquared() > Constants.EPSILON) {
			moveSum.addLocal(resolve);
			weightSum.addLocal(resolve.x != 0 ? 1 : 0, resolve.y != 0 ? 1 : 0);
		}
	}

	Vector2D getAverage() {
		// avoid division by zero
		if (weightSum.x == 0)
			weightSum.x = 1;
		if (weightSum.y == 0)
			weightSum.y = 1;
		return new Vector2D(moveSum.x / weightSum.x, moveSum.y / weightSum.y);
	}

	boolean isZero() {
		return weightSum.isZero();
	}
}
