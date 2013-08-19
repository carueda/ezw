package is.wavelet;

/**
 * A wavelet filter that can be constructed with given coefficients. Adapted
 * from Numerical Recipes.
 * 
 * @author Carlos Rueda
 * @version 0.1 March/25/1999
 */
public class CoeffFilter implements Filter {
	int ncof;

	int ioff;

	int joff;

	float[] cc;

	float[] cr;

	// Test: Daub-4:

	static float[] daub4 = { 0.4829629131445341f, 0.8365163037378079f,
			0.2241438680420134f, -0.1294095225512604f };

	public CoeffFilter() {
		this(daub4);
	}

	/**
	 * Creates a wavelet filter with the coefficients given.
	 */
	public CoeffFilter(float[] coefficients) {
		super();

		ncof = coefficients.length;

		cc = new float[ncof];

		System.arraycopy(coefficients, 0, cc, 0, ncof);

		cr = new float[ncof];
		float sig = -1.0f;
		for (int k = 1; k <= ncof; k++) {
			cr[ncof + 1 - k - 1] = sig * cc[k - 1];
			sig = -sig;
		}
		ioff = joff = -(ncof >> 1);
	}

	/**
	 * From Numerical Recipes, but arrays are zero-based here.
	 */
	public void filter(float[] a, int n, int isign, float[] wksp) {
		//
		// Note the trailing ''-1'' in all subindexing expressions.
		//
		float ai, ai1;
		int i, ii, j, jf, jr, k, n1, ni, nj, nh, nmod;

		if (n < 4)
			return;

		nmod = ncof * n;
		n1 = n - 1;
		nh = n >> 1;
		for (j = 1; j <= n; j++)
			wksp[j - 1] = 0f;
		if (isign >= 0) {
			for (ii = 1, i = 1; i <= n; i += 2, ii++) {
				ni = i + nmod + ioff;
				nj = i + nmod + joff;
				for (k = 1; k <= ncof; k++) {
					jf = n1 & (ni + k);
					jr = n1 & (nj + k);
					wksp[ii - 1] += cc[k - 1] * a[jf + 1 - 1];
					wksp[ii + nh - 1] += cr[k - 1] * a[jr + 1 - 1];
				}
			}
		} 
		else {
			for (ii = 1, i = 1; i <= n; i += 2, ii++) {
				ai = a[ii - 1];
				ai1 = a[ii + nh - 1];
				ni = i + nmod + ioff;
				nj = i + nmod + joff;
				for (k = 1; k <= ncof; k++) {
					jf = (n1 & (ni + k)) + 1;
					jr = (n1 & (nj + k)) + 1;
					wksp[jf - 1] += cc[k - 1] * ai;
					wksp[jr - 1] += cr[k - 1] * ai1;
				}
			}
		}
		for (j = 1; j <= n; j++)
			a[j - 1] = wksp[j - 1];
	}
}
