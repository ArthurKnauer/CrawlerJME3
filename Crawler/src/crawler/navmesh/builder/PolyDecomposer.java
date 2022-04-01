package crawler.navmesh.builder;

import convdecomp.geometry.DecompPoly;
import convdecomp.geometry.PairDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeMap;

/**
 *
 * @author VTPlusAKnauer
 */


public class PolyDecomposer {
	
	public static void constructDiagonals(DecompPoly dp, TreeMap<Integer, ArrayList<Integer>> diagonals, int i, int k) {
		int j;
		boolean ijreal = true, jkreal = true;
		if (k - i <= 1) return;

		PairDeque pair = dp.subD.pairs(i, k);
		if (dp.reflex(i)) {
			j = pair.bB();
			ijreal = (pair.aB() == pair.bB());
		}
		else {
			j = pair.aF();
			jkreal = (pair.bF() == pair.aF());
		}

		if (ijreal && (j - i > 1) && (j - i != dp.n - 1)) {
			ArrayList<Integer> list = diagonals.get(i);
			if (list == null) diagonals.put(i, new ArrayList<>(Arrays.asList(new Integer[]{j})));
			else list.add(j);

			list = diagonals.get(j);
			if (list == null) diagonals.put(j, new ArrayList<>(Arrays.asList(new Integer[]{i})));
			else list.add(i);
		}
		if (jkreal && (k - j > 1) && (k - j != dp.n - 1)) {
			ArrayList<Integer> list = diagonals.get(j);
			if (list == null) diagonals.put(j, new ArrayList<>(Arrays.asList(new Integer[]{k})));
			else list.add(k);

			list = diagonals.get(k);
			if (list == null) diagonals.put(k, new ArrayList<>(Arrays.asList(new Integer[]{j})));
			else list.add(j);
		}

		constructDiagonals(dp, diagonals, i, j);
		constructDiagonals(dp, diagonals, j, k);
	}
	
}
