package is.ezw;

import is.io.SAQHeader;
import is.io.SAQLimitReachedException;
import is.io.ISAQOutputStream;

import java.awt.Point;

/**
 * A direct implementation of the SAQ encoder. References: "Embedded Image
 * Coding Using Zerotrees of Wavelet Coefficients." Shapiro. IEEE Trans. on
 * signal proc. Vol. 41, No 12, December, 1993.
 * 
 * @author Carlos Rueda
 * @version 0.1 March/14/1999
 */
public class SAQEncoder {
	private ISAQOutputStream os;

	private int passes;

	private int n;

	private int n2; // == n/2;

	private float[][] mat;

	private float T0;

	private SAQList /* Point */dominant;

	private SAQList /* Point */new_dominant;

	private SAQList /* SAQFloatPair */subordinate;

	private int previousSubordinateSize;

	private SAQList /* SAQFloatPair */subordinateSegment0;

	private SAQList /* SAQFloatPair */subordinateSegment1;

	private SAQZerotree ztroots;

	/** 3-symbol pass has been initiated? */
	private boolean pass3Initiated;

	public SAQEncoder(short filterCode, float[][] mat, float T0, int passes,
			ISAQOutputStream os) {
		create(filterCode, mat, T0, passes, os);
	}

	public SAQEncoder(short filterCode, float[][] mat, int passes,
			ISAQOutputStream os) {
		create(filterCode, mat, calculateInitialThreshold(mat), passes, os);
		this.mat = mat;
	}

	private static float calculateInitialThreshold(float[][] mat) {
		float[] stats = SAQUtil.getStats(mat);
		float abs_max = Math.max(Math.abs(stats[1]), Math.abs(stats[2]));
		return abs_max / 2f + 1f;
	}

	private void create(short filterCode, float[][] mat, float T0, int passes,
			ISAQOutputStream os) {
		this.mat = mat;
		this.T0 = T0;
		this.passes = passes;
		this.os = os;
		n = mat.length;
		n2 = n >>> 1;
		int N = n * n;
		ztroots = new SAQZerotree(n);
		dominant = new SAQList(N);
		new_dominant = new SAQList(N);
		subordinate = new SAQList(N);
		previousSubordinateSize = 0;
		subordinateSegment0 = new SAQList(N);
		subordinateSegment1 = new SAQList(N);

		dominant.initDominantList(0, 0, n);

		os.writeHeader(new SAQHeader(filterCode, (byte) passes, (short) n, 0f,
				T0));
	}

	private void dominantPass(float T) throws SAQLimitReachedException {
		SAQUtil.debug("---------------DOMINANT PASS");

		pass3Initiated = false;

		os.beginPass(4);

		for (int i = 0; i < dominant.size(); i++) {
			Point p = (Point) dominant.elementAt(i);
			visitPosition(p, T);
		}
	}

	public void process() {
		float T = T0;
		for (int k = 0; k < passes; k++, T /= 2) {
			System.err.println("Starting pass " + k + " with "
					+ dominant.size() + " elements and T= " + T + " ...");
			ztroots.reset();

			try {
				dominantPass(T);
				subordinatePass(T);
			} 
			catch (SAQLimitReachedException e) {
				break;
			}

			SAQList tmp = dominant;
			dominant = new_dominant;
			new_dominant = tmp;
			new_dominant.setSize(0);

		}

		os.flush();
	}

	/**
	 * Updates elements [previousSubordinateSize..subordinate.size()-1] in
	 * subordinate list such that elements coded with 1 appear before elements
	 * with 0.
	 */
	private void resortSubordinateList() {
		subordinate.setSize(previousSubordinateSize);

		// add elements coded with 1
		for (int i = 0; i < subordinateSegment1.size(); i++) {
			SAQFloatPair z = (SAQFloatPair) subordinateSegment1.elementAt(i);
			subordinate.addElement(z);
		}
		// add elements coded with 0
		for (int i = 0; i < subordinateSegment0.size(); i++) {
			SAQFloatPair z = (SAQFloatPair) subordinateSegment0.elementAt(i);
			subordinate.addElement(z);
		}

		previousSubordinateSize = subordinate.size();
		subordinateSegment0.setSize(0);
		subordinateSegment1.setSize(0);
	}

	private void subordinatePass(float T) throws SAQLimitReachedException {
		SAQUtil.debug("---------------SUBORDINATE PASS");

		os.beginPass(2);

		for (int i = 0; i < subordinate.size(); i++) {
			SAQFloatPair z = (SAQFloatPair) subordinate.elementAt(i);
			float q = z.t + T / 2;
			boolean one = z.v >= q;
			if (one) {
				z.t = q;
				os.output(1);
			}
			else
				os.output(0);

			// prepare subsequent resort of subordinate:
			if (i >= previousSubordinateSize) {
				if (one)
					subordinateSegment1.addElement(z);
				else
					subordinateSegment0.addElement(z);
			}
		}

		resortSubordinateList();

		SAQUtil.printPoints("sub", subordinate);

	}

	private void visitPosition(Point p, float T)
			throws SAQLimitReachedException {
		int x = p.x;
		int y = p.y;
		float c = mat[y][x];

		if (!pass3Initiated && (x >= n2 || y >= n2)) {
			pass3Initiated = true;
			os.beginPass(3);
		}

		if (Math.abs(c) < T) {
			new_dominant.addElement(p);

			// insignificant.
			if (ztroots.descends(x, y)) {
				// Predictable insignificant. Don't code.
			} 
			else {
				if (withSignificantDescendant(x, y, T)) {
					os.output(SAQSymbol.IZ);
				} 
				else {
					os.output(SAQSymbol.ZTR);

					if (!pass3Initiated) {
						ztroots.add(p.x, p.y);
					}
				}
			}
		} 
		else { // significant
			if (c > 0)
				os.output(SAQSymbol.POS);
			else
				os.output(SAQSymbol.NEG);

			subordinate.addElement(new SAQFloatPair(Math.abs(c), T));

			mat[y][x] = 0;

		}
	}

	/**
	 * Determines if coefficient mat[y][x] has significant descendants with
	 * respect to threshold T.
	 */
	private boolean withSignificantDescendant(int x, int y, float T) {
		if ((x > 0 || y > 0) && _isSignificant(mat, 2 * x, 2 * y, T))
			return true;

		return _isSignificant(mat, 2 * x + 1, 2 * y, T)
				|| _isSignificant(mat, 2 * x, 2 * y + 1, T)
				|| _isSignificant(mat, 2 * x + 1, 2 * y + 1, T);
	}
	
	/**
	 * Determines if coefficient mat[y][x] has significant 
	 * descendants with respect to threshold T.
	 */
	private static boolean _isSignificant(float[][] mat, int x, int y, float T)
	{
		int n = mat.length;
		if ( x >= n || y >= n )
			return false;
			
		if ( Math.abs(mat[y][x]) >= T )
			return true;
			
		return _isSignificant(mat, 2*x, 2*y, T)
		    || _isSignificant(mat, 2*x + 1, 2*y, T)
		    || _isSignificant(mat, 2*x, 2*y + 1, T)
		    || _isSignificant(mat, 2*x + 1, 2*y + 1, T)
		;
	}

}
