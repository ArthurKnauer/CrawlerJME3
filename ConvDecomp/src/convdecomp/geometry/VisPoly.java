package convdecomp.geometry;

import java.util.List;

/**
 * Implements a linear time algorithm for the visiblity polygon of sp[0] in a given simple polyline.
 */
public class VisPoly {

	private List<Point> sp;	// simple polygon
	private Point org;		// origin of visibility polygon
	private int v[];		// stack holds indices of visibility polygon
	private int vtype[];	// types of vp vertices
	private int vp;		// stack pointer to top element

	public VisPoly(List<Point> pl) {
		int n = pl.size();
		sp = pl;
		v = new int[n];
		vtype = new int[n];
		vp = -1;
	}

	/**
	 * Build the visibility polygon for a given vertex
	 */
	public void build(int orgIndex) {
		HomoPoint edgej;		// during the loops, this is the line p(j-1)->p(j)
		org = sp.get(orgIndex);
		vp = -1;
		int j = orgIndex;
		push(j++, RWALL);	// org & p[1] on VP
		do {			// loop always pushes pj and increments j.
			push(j++, RWALL);
			if (j >= sp.size() + orgIndex)
				return; // we are done.
			edgej = p(j - 1).meet(p(j));
			if (edgej.left(org))
				continue; // easiest case: add edge to VP.
			// else pj backtracks, we must determine where
			if (!edgej.left(p(j - 2))) {// pj is above last VP edge
				j = exitRBay(j, top(), HomoPoint.INFINITY);
				push(j++, RLID);
				continue;  // exits bay; push next two
			}

			saveLid();		// else pj below top edge; becomes lid or pops
			do {		// pj hides some of VP; break loop when can push pj.
//System.out.print("do j:"+j+" lid:"+LLidIdx+" "+RLidIdx+toString());
				if (Point.left(org, top(), p(j))) {// saved lid ok so far...
					if (Point.right(p(j), p(j + 1), org))
						j++; // continue to hide
					else
						if (edgej.left(p(j + 1))) { // or turns up into bay
							j = exitLBay(j, p(j), p(LLidIdx).meet(p(LLidIdx - 1))) + 1;
						} else {	// or turns down; put saved lid back & add new VP edge 
							restoreLid();
							push(j++, LWALL);
							break;
						}
					edgej = p(j - 1).meet(p(j)); // loop continues with new j; update edgej
				} else		// lid is no longer visible
					if (!edgej.left(top())) { // entered RBay, must get out
						assertThat((RLidIdx != NOTSAVED),
								"no RLid saved " + LLidIdx + RLidIdx + toString());
						j = exitRBay(j, top(), edgej.neg()); // exits bay;
						push(j++, RLID);
						break;
					} // found new visible lid to add to VP.
					else
						saveLid(); // save new lid from VP; continue to hide VP.
			} while (true);
//System.out.print("exit j:"+j+" lid:"+LLidIdx+" "+RLidIdx+toString());
		} while (j < sp.size() + orgIndex); // don't push origin again.
	}

	public static void assertThat(boolean flag, String s) {
		if (!flag)
			System.out.println("ASSERT FAIL: " + s);
	}

	public boolean empty() {
		return vp < 0;
	}

	public int popVisible() {
		while ((vtype[vp] == RLID) || (vtype[vp] == LLID))
			vp--;
		return v[vp--];
	}

	/**
	 * helper functions
	 */
	private Point p(int j) {
		return sp.get(j % sp.size());
	}

	private Point top() {
		return p(v[vp]);
	}
	
	private Point ntop() {
		return p(v[vp - 1]);
	}

	private void push(int idx, int t) {
		v[++vp] = idx;
		vtype[vp] = t;
	}

	/**
	 * exit a bay: proceed from j, j++, .. until exiting the bay defined to the right (or left for exitLBay) of the line
	 * from org through point bot to line lid. Return j such that (j,j+1) forms new lid of this bay. Assumes that
	 * pl.get(j) is not left (right) of the line org->bot.
	 */
	int exitRBay(int j, Point bot, HomoPoint lid) {
		int wn = 0;		// winding number
		HomoPoint mouth = org.meet(bot);
		boolean lastLeft, currLeft = false;
		while (++j < 3 * sp.size()) {
			lastLeft = currLeft;
			currLeft = mouth.left(p(j));
			if ((currLeft != lastLeft) // If cross ray org->bot, update wn
					&& (Point.left(p(j - 1), p(j), org) == currLeft)) {
				if (!currLeft)
					wn--;
				else
					if (wn++ == 0) { // on 0->1 transitions, check window
						HomoPoint edge = p(j - 1).meet(p(j));
						if (edge.left(bot) && !HomoPoint.cw(mouth, edge, lid))
							return j - 1; // j exits window!
					}
			}
		}

		System.out.println("ERROR: We never exited RBay " + bot + lid + wn + "\n" + toString());
		return j;
	}

	int exitLBay(int j, Point bot, HomoPoint lid) {
		int wn = 0;		// winding number
		HomoPoint mouth = org.meet(bot);
		boolean lastRight, currRight = false; // called with !right(org,bot,pj)
		while (++j < 3 * sp.size()) {
			lastRight = currRight;
			currRight = mouth.right(p(j));
			if ((currRight != lastRight) // If cross ray org->bot, update wn
					&& (Point.right(p(j - 1), p(j), org) == currRight)) {
				if (!currRight)
					wn++;
				else
					if (wn-- == 0) { // on 0->-1 transitions, check window
						HomoPoint edge = p(j - 1).meet(p(j));
						if (edge.right(bot) && !HomoPoint.cw(mouth, edge, lid))
							return j - 1; // j exits window!
					}
			}
		}

		System.out.println("ERROR: We never exited LBay " + bot + lid + wn + "\n" + toString());
		return j;
	}
	/**
	 * polygon vertex types: LLID-------------------RLID | | | | --------LWALL RWALL---------
	 */
	static final int RLID = 0;
	static final int LLID = 1;
	static final int RWALL = 2;
	static final int LWALL = 3;
	static final String vtypeString[] = {"RL ", "LL ", "RW ", "LW "};
	/**
	 * Proceedures to keep the lid above the current vertex on top of the stack, leaving the top() as a vertex of the VP
	 * that is also a vertex of sp. These use global status variables to keep the code for build() cleaner.
	 */
	static final int NOTSAVED = -1; // flag for when we don't have a RLidIdx
	private int LLidIdx, RLidIdx;

	private void saveLid() {
		if (vtype[vp] == LWALL)
			vp--; // for LWALL, lid is previous two
		LLidIdx = v[vp--];
		if (vtype[vp] == RLID)
			RLidIdx = v[vp--]; // if not RLID, just leave on top().
		else
			RLidIdx = NOTSAVED;
	}

	private void restoreLid() {
		if (RLidIdx != NOTSAVED)
			push(RLidIdx, RLID);
		push(LLidIdx, LLID);
	}
}
