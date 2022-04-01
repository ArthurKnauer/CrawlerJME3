package convdecomp.geometry;

import java.util.List;

final public class DecompPoly {

	public List<Point> sp;		// the polygon
	public int n;		// number of vertices
	public SubDecomp subD;	// the subproblems in  n x r space
	private int reflexFirst;	// for reflexIter
	private int reflexNext[];
	private boolean reflexFlag[];
	
	public DecompPoly(List<Point> pl) {
		sp = pl;
		n = sp.size();
	}

	public void init() {
		initReflex();
		subD = new SubDecomp(reflexFlag);
		initVisibility();
		initSubproblems();
	}

	public boolean reflex(int i) {
		return reflexFlag[i];
	}

	private void initReflex() {
		reflexFlag = new boolean[n];
		reflexNext = new int[n];

		int wrap = 0;	/* find reflex vertices */
		reflexFlag[wrap] = true;	// by convention
		for (int i = n - 1; i > 0; i--) {
			reflexFlag[i] = Point.right(sp.get(i - 1), sp.get(i), sp.get(wrap));
			wrap = i;
		}

		reflexFirst = n;		// for reflexIter
		for (int i = n - 1; i >= 0; i--) {
			reflexNext[i] = reflexFirst;
			if (reflex(i)) {
				reflexFirst = i;
			}
		}
	}

	/* a cheap iterator through reflex vertices; each vertex knows the
	 index of the next reflex vertex. */
	public int reflexIter() {
		return reflexFirst;
	}

	public int reflexIter(int i) { // start w/ i or 1st reflex after...
		if (i <= 0)
			return reflexFirst;
		if (i > reflexNext.length)
			return reflexNext.length;
		return reflexNext[i - 1];
	}

	public int reflexNext(int i) {
		return reflexNext[i];
	}
	public final static int INFINITY = 100000;
	public final static int BAD = 999990;
	public final static int NONE = 0;

	public boolean visible(int i, int j) {
		return subD.weight(i, j) < BAD;
	}

	public void initVisibility() { // initReflex() first
		VisPoly vp = new VisPoly(sp);
		for (int i = reflexIter(); i < n; i = reflexNext(i)) {
			vp.build(i);
			while (!vp.empty()) {
				int j = vp.popVisible() % n;
				if (j < i)
					subD.setWeight(j, i, INFINITY);
				else
					subD.setWeight(i, j, INFINITY);
			}
		}
	}

	private void setAfter(int i) { // i reflex
		assertThat(reflex(i), "non reflex i in setAfter(" + i + ")");
		subD.setWeight(i, i + 1, 0);
		if (visible(i, i + 2))
			subD.init(i, i + 2, 0, i + 1, i + 1);
	}

	private void setBefore(int i) { // i reflex
		assertThat(reflex(i), "non reflex i in setAfter(" + i + ")");
		subD.setWeight(i - 1, i, 0);
		if (visible(i - 2, i))
			subD.init(i - 2, i, 0, i - 1, i - 1);
	}

	public void initSubproblems() { // initVisibility first
		int i;

		i = reflexIter();
		if (i == 0) {
			setAfter(i);
			i = reflexNext(i);
		}
		if (i == 1) {
			subD.setWeight(0, 1, 0);
			setAfter(i);
			i = reflexNext(i);
		}
		while (i < n - 2) {
			setBefore(i);
			setAfter(i);
			i = reflexNext(i);
		}
		if (i == n - 2) {
			setBefore(i);
			subD.setWeight(i, i + 1, 0);
			i = reflexNext(i);
		}
		if (i == n - 1) {
			setBefore(i);
		}
	}

	public void initPairs(int i, int k) {
		subD.init(i, k);
	}
	
	public void recoverSolution(int i, int k) {
		int j;
		
		if (k - i <= 1)
			return;
		PairDeque pair = subD.pairs(i, k);
		if (reflex(i)) {
			j = pair.bB();
			recoverSolution(j, k);
			if (j - i > 1) {
				if (pair.aB() != pair.bB()) {
					PairDeque pd = subD.pairs(i, j);
					pd.restore();
					while ((!pd.emptyB()) && pair.aB() != pd.aB())
						pd.popB();
					assertThat(!pd.emptyB(), "emptied pd " + i + "," + j + "," + k + " " + pair.toString());
				}
				recoverSolution(i, j);
			}
		} else {
			j = pair.aF();
			recoverSolution(i, j);
			if (k - j > 1) {
				if (pair.aF() != pair.bF()) {
					PairDeque pd = subD.pairs(j, k);
					pd.restore();
					while ((!pd.empty()) && pair.bF() != pd.bF())
						pd.pop();
					assertThat(!pd.empty(), "emptied pd " + i + "," + j + "," + k + " " + pair.toString());
				}
				recoverSolution(j, k);
			}
		}
	}

	public void typeA(int i, int j, int k) { /* i reflex; use jk */
		//    System.out.print("\nA "+i+","+j+","+k+":");
		//    assert(reflex(i), "non reflex i in typeA("+i+","+j+","+k+")");
		//    assert(k-i > 1, "too small in typeA("+i+","+j+","+k+")");
		if (!visible(i, j))
			return;
		int top = j;
		int w = subD.weight(i, j);
		if (k - j > 1) {
			if (!visible(j, k))
				return;
			w += subD.weight(j, k) + 1;
		}
		if (j - i > 1) {		// check if must use ij, too.
			PairDeque pair = subD.pairs(i, j);
			if (!Point.left(sp.get(k), sp.get(j), sp.get(pair.bB()))) {
				while (pair.more1B()
						&& !Point.left(sp.get(k), sp.get(j), sp.get(pair.underbB())))
					pair.popB();
				if ((!pair.emptyB())
						&& !Point.right(sp.get(k), sp.get(i), sp.get(pair.aB())))
					top = pair.aB();
				else
					w++;		// yes, need ij. top = j already
			} else
				w++;		// yes, need ij. top = j already
		}
		update(i, k, w, top, j);
	}

	public void typeB(int i, int j, int k) { /* k reflex, i not. */
		//    System.out.print("\nB "+i+","+j+","+k+":");
		if (!visible(j, k))
			return;
		int top = j;
		int w = subD.weight(j, k);
		if (j - i > 1) {
			if (!visible(i, j))
				return;
			w += subD.weight(i, j) + 1;
		}
		if (k - j > 1) {		// check if must use jk, too.
			PairDeque pair = subD.pairs(j, k);
			if (!Point.right(sp.get(i), sp.get(j), sp.get(pair.aF()))) {
				while (pair.more1()
						&& !Point.right(sp.get(i), sp.get(j), sp.get(pair.underaF())))
					pair.pop();
				if ((!pair.empty())
						&& !Point.left(sp.get(i), sp.get(k), sp.get(pair.bF())))
					top = pair.bF();
				else
					w++;			// yes, use jk. top=j already
			} else
				w++;			// yes, use jk. top=j already
		}
		update(i, k, w, j, top);
	}


	/* We have a new solution for subprob a,b with weight w, using
	 i,j.  If it is better than previous solutions, we update. 
	 We assume that a<b and i < j.
	 */
	public void update(int a, int b, int w, int i, int j) {
		//    System.out.print("update("+a+","+b+" w:"+w+" "+i+","+j+")");
		int ow = subD.weight(a, b);
		if (w <= ow) {
			PairDeque pair = subD.pairs(a, b);
			if (w < ow) {
				pair.flush();
				subD.setWeight(a, b, w);
			}
			pair.pushNarrow(i, j);
		}
	}

	public void assertThat(boolean flag, String s) {
		if (!flag)
			System.out.println("ASSERT FAIL: " + s);
	}
}

