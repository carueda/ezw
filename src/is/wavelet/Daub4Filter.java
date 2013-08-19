package is.wavelet;

/**
 * The Daub-4 wavelet filter. Adapted from Numerical Recipes.
 * 
 * @author NR & Carlos Rueda
 * @version 0.1 March/25/1999
 */
public class Daub4Filter implements Filter {
	static final float C0 = 0.4829629131445341f;
	static final float C1 = 0.8365163037378079f;
	static final float C2 = 0.2241438680420134f;
	static final float C3 = -0.1294095225512604f;

	/**
	 * Creates a Daubechies-4 filter.
	 */
	public Daub4Filter() {
		super();
	}

	/**
	 * From Numerical Recipes, but arrays are zero-based here.
	 */
	public void filter(float[] a, int n, int isign, float[] wksp) {
		//
		// Note the trailing ''-1'' in all subindexing expressions.
		//
		int nh, nh1, i, j;

		if (n < 4)
			return;

		nh1 = (nh = n >> 1) + 1;
		if (isign >= 0) {
			for (i = 1, j = 1; j <= n - 3; j += 2, i++) {
				wksp[i - 1] = C0 * a[j - 1] + C1 * a[j + 1 - 1] + C2
						* a[j + 2 - 1] + C3 * a[j + 3 - 1];
				wksp[i + nh - 1] = C3 * a[j - 1] - C2 * a[j + 1 - 1] + C1
						* a[j + 2 - 1] - C0 * a[j + 3 - 1];
			}
			wksp[i - 1] = C0 * a[n - 1 - 1] + C1 * a[n - 1] + C2 * a[1 - 1]
					+ C3 * a[2 - 1];
			wksp[i + nh - 1] = C3 * a[n - 1 - 1] - C2 * a[n - 1] + C1
					* a[1 - 1] - C0 * a[2 - 1];
		} 
		else {
			wksp[1 - 1] = C2 * a[nh - 1] + C1 * a[n - 1] + C0 * a[1 - 1] + C3
					* a[nh1 - 1];
			wksp[2 - 1] = C3 * a[nh - 1] - C0 * a[n - 1] + C1 * a[1 - 1] - C2
					* a[nh1 - 1];
			for (i = 1, j = 3; i < nh; i++) {
				wksp[j++ - 1] = C2 * a[i - 1] + C1 * a[i + nh - 1] + C0
						* a[i + 1 - 1] + C3 * a[i + nh1 - 1];
				wksp[j++ - 1] = C3 * a[i - 1] - C0 * a[i + nh - 1] + C1
						* a[i + 1 - 1] - C2 * a[i + nh1 - 1];
			}
		}
		for (i = 1; i <= n; i++)
			a[i - 1] = wksp[i - 1];

	}
}
