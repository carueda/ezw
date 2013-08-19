package is.ezw;

import is.io.SAQHeader;
import is.io.ISAQInputStream;
import is.io.SAQLimitReachedException;

import java.awt.Point;
import java.io.EOFException;

/**
 * A direct implementation of the SAQ decoder. References: "Embedded Image
 * Coding Using Zerotrees of Wavelet Coefficients." Shapiro. IEEE Trans. on
 * signal proc. Vol. 41, No 12, December, 1993.
 * 
 * @author Carlos Rueda
 * @version 0.1 March/14/1999
 */
public class SAQDecoder {
	private ISAQInputStream is;

	private int passes;

	private int n;

	private int n2; // == n/2;

	private float[][] mat;

	private float T0;

	private SAQList /* Point */dominant;

	private SAQList /* Point */new_dominant;

	private SAQList /* Point */subordinate;

	private int previousSubordinateSize;

	private SAQList /* Poit */subordinateSegment0;

	private SAQList /* Poit */subordinateSegment1;

	private SAQZerotree ztroots;

	/** 3-symbol pass has been initiated? */
	private boolean pass3Initiated;

	/** To make matrix files progressively */
	private String basename;

	/** To notify matrices progressively */
	private IMatrixObserver mo;

	public SAQDecoder(ISAQInputStream is, IMatrixObserver mo) throws Exception {
		this(is, null, mo);
	}

	public SAQDecoder(ISAQInputStream is, String basename) throws Exception {
		this(is, basename, null);
		;
	}

	private SAQDecoder(ISAQInputStream is, String basename, IMatrixObserver mo)
			throws Exception {
		this.is = is;
		this.basename = basename;
		this.mo = mo;

		SAQHeader header = is.readHeader();
		passes = header.noScales;
		n = header.n;
		T0 = header.T0;
		n2 = n >>> 1;
		mat = new float[n][n];
		int N = n * n;
		ztroots = new SAQZerotree(n);
		dominant = new SAQList(N);
		new_dominant = new SAQList(N);
		subordinate = new SAQList(N);
		previousSubordinateSize = 0;
		subordinateSegment0 = new SAQList(N);
		subordinateSegment1 = new SAQList(N);

		dominant.initDominantList(0, 0, n);

		if (mo != null)
			mo.takeHeader(header);
	}

	private void dominantPass(float T) throws EOFException,
			SAQLimitReachedException {
		pass3Initiated = false;

		is.beginPass(4);

		try {
			for (int i = 0; i < dominant.size(); i++) {
				Point p = (Point) dominant.elementAt(i);
				visitPosition(p, T);
			}

		}
		catch (RuntimeException e) {
			System.err.println(e);
		}

	}

	public void process() {
		boolean done = false;
		float T = T0;
		for (int k = 0; k < passes && !done; k++, T /= 2) {
			ztroots.reset();

			try {
				dominantPass(T);
				subordinatePass(T);
			} 
			catch (SAQLimitReachedException e) {
				done = true;
			}
			catch (EOFException e) {
				done = true;
			}

			SAQList tmp = dominant;
			dominant = new_dominant;
			new_dominant = tmp;
			new_dominant.setSize(0);

			if (basename != null) {
				String kk = k < 10 ? ("00" +k) : k < 100 ? ("0" +k) : ("" +k);
				String filename = basename + kk + ".raw";
				System.err.println("Writing " + filename + " ...");
				SAQUtil.writeMatrix(filename, mat);
//				System.err.println("Ready.");
			}
			else if (mo != null) {
				mo.takeMatrix(mat, is.getNumBytesRead());
			}
			else
				throw new RuntimeException("Impossible");

		}
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
			Point p = (Point) subordinateSegment1.elementAt(i);
			subordinate.addElement(p);
		}
		// add elements coded with 0
		for (int i = 0; i < subordinateSegment0.size(); i++) {
			Point p = (Point) subordinateSegment0.elementAt(i);
			subordinate.addElement(p);
		}

		previousSubordinateSize = subordinate.size();
		subordinateSegment0.setSize(0);
		subordinateSegment1.setSize(0);
	}

	private void subordinatePass(float T) throws EOFException,
			SAQLimitReachedException {
		SAQUtil.printPoints("sub", subordinate);
		SAQUtil.printValues("sub", subordinate, mat);

		is.beginPass(2);

		try {
			for (int i = 0; i < subordinate.size(); i++) {
				Point p = (Point) subordinate.elementAt(i);
				float val = mat[p.y][p.x];
				boolean neg = val < 0;
				float new_val = Math.abs(val);

				int code = is.input();
				boolean one = code == 1;
				if (one) {
					new_val += T / 2;
				} 
				else if (code == 0) {
					// ok. Nothing to do.
				}
				else
					throw new RuntimeException(
							"Impossible. subordinatePass code=" + code);

				if (neg)
					new_val *= -1;

				mat[p.y][p.x] = new_val;

				// prepare subsequent resort of subordinate:
				if (i >= previousSubordinateSize) {
					if (one)
						subordinateSegment1.addElement(p);
					else
						subordinateSegment0.addElement(p);
				}
			}

			resortSubordinateList();
		} 
		catch (RuntimeException e) {
			System.out.println(e);
		}

	}

	private void visitPosition(Point p, float T) throws EOFException,
			SAQLimitReachedException {
		int x = p.x;
		int y = p.y;

		if (!pass3Initiated && (x >= n2 || y >= n2)) {
			pass3Initiated = true;
			is.beginPass(3);
		}

		if (!ztroots.descends(x, y)) {
			int code = is.input();
			if (code == SAQSymbol.IZ) {
				// hasSignificantDescendant(x, y, T) )
				new_dominant.addElement(p);
			}
			else if (code == SAQSymbol.ZTR) {
				new_dominant.addElement(p);
				if (!pass3Initiated)
					ztroots.add(p.x, p.y);
			}
			else if (code == SAQSymbol.POS || code == SAQSymbol.NEG) {
				if (code == SAQSymbol.POS)
					mat[p.y][p.x] = T;
				else
					mat[p.y][p.x] = -T;
				subordinate.addElement(p);

			}
			else
				throw new RuntimeException("Impossible. dominantPass code="
						+ code);
		} 
		else
			new_dominant.addElement(p);
	}
}
