package convdecomp;

import convdecomp.geometry.DecompPoly;
import convdecomp.geometry.Point;
import java.util.List;

/**
 * Implements the dynamic programming algorithm for minimum convex decomposition of a simple polygon.
 */
public class MCD {

	/**
	 * Assumes that the point list contains at least three points
	 */
	public static DecompPoly compute(List<Point> pl) {
		int i, k, n = pl.size();
		DecompPoly dp = new DecompPoly(pl);
		dp.init();

		for (int l = 3; l < n; l++) {
			for (i = dp.reflexIter(); i + l < n; i = dp.reflexNext(i))
				if (dp.visible(i, k = i + l)) {
					dp.initPairs(i, k);
					if (dp.reflex(k))
						for (int j = i + 1; j < k; j++)
							dp.typeA(i, j, k);
					else {
						for (int j = dp.reflexIter(i + 1); j < k - 1; j = dp.reflexNext(j))
							dp.typeA(i, j, k);
						dp.typeA(i, k - 1, k); // do this, reflex or not.
					}
				}

			for (k = dp.reflexIter(l); k < n; k = dp.reflexNext(k))
				if ((!dp.reflex(i = k - l)) && dp.visible(i, k)) {
					dp.initPairs(i, k);
					dp.typeB(i, i + 1, k); // do this, reflex or not.
					for (int j = dp.reflexIter(i + 2); j < k; j = dp.reflexNext(j))
						dp.typeB(i, j, k);
				}
		}
	
		dp.recoverSolution(0, n - 1);
		return dp;
	}
}
