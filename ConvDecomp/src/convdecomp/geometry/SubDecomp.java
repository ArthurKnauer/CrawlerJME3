/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package convdecomp.geometry;



/* this class stores all subproblems for a decomposition by dynamic 
 programming.  
 It uses an indirect addressing into arrays that have all the
 reflex vertices first, so that I can allocate only O(nr) space.
 */
final public class SubDecomp {
	private int wt[][];
	private PairDeque pd[][];
	private int rx[];		// indirect index so reflex come first

	SubDecomp(boolean[] reflex) {
		int n = reflex.length, r = 0;

		rx = new int[n];

		for (int i = 0; i < n; i++)
			if (reflex[i])
				rx[i] = r++;

		int j = r;
		for (int i = 0; i < n; i++)
			if (!reflex[i])
				rx[i] = j++;

		wt = new int[n][];
		pd = new PairDeque[n][];
		for (int i = 0; i < r; i++) {
			wt[i] = new int[n];
			for (j = 0; j < wt[i].length; j++)
				wt[i][j] = DecompPoly.BAD;
			pd[i] = new PairDeque[n];
		}
		for (int i = r; i < n; i++) {
			wt[i] = new int[r];
			for (j = 0; j < wt[i].length; j++)
				wt[i][j] = DecompPoly.BAD;
			pd[i] = new PairDeque[r];
		}
	}

	public void setWeight(int i, int j, int w) {
		wt[rx[i]][rx[j]] = w;
	}

	public int weight(int i, int j) {
		return wt[rx[i]][rx[j]];
	}

	public PairDeque pairs(int i, int j) {
		return pd[rx[i]][rx[j]];
	}

	public PairDeque init(int i, int j) {
		return pd[rx[i]][rx[j]] = new PairDeque();
	}

	public void init(int i, int j, int w, int a, int b) {
		setWeight(i, j, w);
		init(i, j).push(a, b);
	}
}