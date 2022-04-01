package convdecomp.test;

import convdecomp.MCD;
import convdecomp.geometry.DecompPoly;
import convdecomp.geometry.PairDeque;
import convdecomp.geometry.Point;
import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Event;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Panel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

public class MCDTestApplet extends Applet {

	static ArrayList<Point> pts = new ArrayList<Point>();
	static DecompPoly result = null;
	private Button clear_button = new Button("Clear All");
	private Button delete_button = new Button("Delete Last");
	private Button update_button = new Button("Update");

	public static void main(String args[]) {
		makeStandalone("Min convex decomp of simple polygon", new MCDTestApplet(), 800, 600);
	}

	@Override
	public void init() {
		Panel p = new Panel();
		setLayout(new BorderLayout());
		setBackground(Color.white);
		setForeground(Color.black);
		addbuttons(p);
		add("South", p);
		
pts.add(new Point(-4.5070405f, 3.098814f));
pts.add(new Point(-4.5070405f, 6.9414325f));
pts.add(new Point(-0.48917684f, 6.9414325f));
pts.add(new Point(-0.48917684f, 3.098814f));
		
//		
//		pts.add(new Point(0, 0));
//		pts.add(new Point(1, 0));
//		pts.add(new Point(1, 1));
//		pts.add(new Point(0, 1));
		
		for (Point pt : pts) {
			pt.y -= 7.0f;
			pt.x += 5.0f;
			pt.x *= 100.0f;
			pt.y *= 100.0f;
		}
	}

	void addbuttons(Panel p) {
		p.add(clear_button);
		p.add(delete_button);
		p.add(update_button);
	}

	boolean handlebuttons(Object target) {
		if (target == clear_button) {
			pts.clear();
			result = null;
		} else
			if (target == delete_button) {
				pts.remove(pts.size() - 1);
				result = null;
			} else
				if (target == update_button) {
					if (!pts.isEmpty()) {
						//convdecomp.CompGeom.Point.reset();
						result = null;
						result = MCD.compute(pts);
						//System.out.println(convdecomp.CompGeom.Point.stats());
					}
				} else
					return false;
		update(getGraphics());
		return true;
	}

	@Override
	public boolean action(Event e, Object obj) {
		if (e.target instanceof Button)
			return handlebuttons(e.target);
		return true;
	}

	@Override
	public boolean mouseDown(Event e, int x, int y) {
		Graphics g = getGraphics();
		g.setColor(Color.black);
		g.fillOval(x - 3, y - 3, 6, 6);
		pts.add(new Point(x, -y));
		result = null;
		repaint();
		return true;
	}

	@Override
	public String getAppletInfo() {
		return "Computational Geometry demonstration applet\n"
				+ "Jack Snoeyink\nversion 1.0 January 1996";
	}

	static public void makeStandalone(String title, MCDTestApplet ap,
			int width, int height) {
		Frame f = new Frame(title);
		ap.init();
		ap.start();
		f.add("Center", ap);
		f.setSize(width, height);
		f.setVisible(true);

		f.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				System.exit(0);
			}
		});
	}

	@Override
	public void paint(Graphics g) {
		g.setColor(Color.red);
		drawPolyline(pts, g, pts.size());
		g.setColor(Color.blue);
		if (result != null) {
			drawDecompPoly(result, g);
		}
	}

	final public void drawPolygon(List<Point> pts, Graphics g, int n) {
		if (n > 2)
			g.drawLine((int) pts.get(n - 1).x, -(int) pts.get(n - 1).y, (int) pts.get(0).x, -(int) pts.get(0).y);
		drawPolyline(pts, g, n);
	}

	private void drawPolyline(List<Point> pts, Graphics g, int n) {
		if (n > 1) {
			g.drawString("0", (int) pts.get(0).x, -(int) pts.get(0).y);
			g.drawLine((int) pts.get(0).x, -(int) pts.get(0).y, (int) pts.get(n-1).x, -(int) pts.get(n-1).y);
			for (int i = 1; i < n; i++) {
				g.drawLine((int) pts.get(i - 1).x, -(int) pts.get(i - 1).y, (int) pts.get(i).x, -(int) pts.get(i).y);
				g.drawString(i+"", (int) pts.get(i).x, -(int) pts.get(i).y);
			}
		}
	}

	private void drawDiagonal(DecompPoly dp, Graphics g, int i, int k) {
		g.drawLine((int) dp.sp.get(i).x, -(int) dp.sp.get(i).y, (int) dp.sp.get(k).x, -(int) dp.sp.get(k).y);
	}

	private void drawHelper(DecompPoly dp, Graphics g, int i, int k) {
		int j;
		boolean ijreal = true, jkreal = true;
		if (k - i <= 1)
			return;
		PairDeque pair = dp.subD.pairs(i, k);
		if (dp.reflex(i)) {
			j = pair.bB();
			ijreal = (pair.aB() == pair.bB());
		} else {
			j = pair.aF();
			jkreal = (pair.bF() == pair.aF());
		}

		ijreal = ijreal && (j - i > 1) && (j - i != dp.n-1);
		jkreal = jkreal && jkreal && (k - j > 1) && (k - j != dp.n-1);
		
		if (ijreal) { drawDiagonal(dp, g, i, j); System.out.println(i+" "+j); }
		if (jkreal) { drawDiagonal(dp, g, j, k); System.out.println(j+" "+k); }		
		
		drawHelper(dp, g, i, j);
		drawHelper(dp, g, j, k);
	}

	public void drawDecompPoly(DecompPoly dp, Graphics g) {
		drawHelper(dp, g, 0, dp.sp.size() - 1);
	}
}
