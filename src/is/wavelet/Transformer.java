package is.wavelet;
	
/**
 * A wavelet transformer that operates with a given filter. This is a
 * preliminary version inspired by Numerical Recipes.
 * 
 * @author Carlos Rueda
 * @version 0.1 March/25/1999
 */
public class Transformer {
	/** The wavelet filter to work with. */
	private Filter filt;

	/**
	 * Creates a transformer with a given wavelet filter.
	 */
	public Transformer(Filter filt) {
		this.filt = filt;
	}

	/**
	 * Applies the inverse tranform on an image.
	 */
	public void inverse(float[][] a) {
		//
		// This is surely not an efficient algorithm, but works.
		//
		int n = a.length;
		int nn;

		if (n < 4)
			return;

		float[] wksp = new float[n];
		float[] column_copy = new float[n];

		for (nn = 4; nn <= n; nn <<= 1) {
			// horizontally
			for (int row = 0; row < nn; row++) {
				filt.filter(a[row], nn, -1, wksp);
			}

			// vertically
			for (int col = 0; col < nn; col++) {
				// copy column:
				for (int k = 0; k < nn; k++)
					column_copy[k] = a[k][col];

				// filter column
				filt.filter(column_copy, nn, -1, wksp);

				// update column:
				for (int k = 0; k < nn; k++)
					a[k][col] = column_copy[k];
			}
		}

	}

	/**
	 * Applies the inverse tranform on a signal.
	 */
	public void inverse(float[] a) {
		int n = a.length;
		int nn;

		if (n < 4)
			return;

		float[] wksp = new float[n + 1];

		for (nn = 4; nn <= n; nn <<= 1)
			filt.filter(a, nn, -1, wksp);
	}

	/**
	 * Applies the direct tranform on an image.
	 */
	public void transform(float[][] a) {
		//
		// This is surely not an efficient algorithm, but works.
		//
		int n = a.length;
		int nn;

		if (n < 4)
			return;

		float[] wksp = new float[n];
		float[] column_copy = new float[n];

		for (nn = n; nn >= 4; nn >>>= 1) {
			// horizontally
			for (int row = 0; row < nn; row++) {
				filt.filter(a[row], nn, 1, wksp);
			}

			// vertically
			for (int col = 0; col < nn; col++) {
				// copy column:
				for (int k = 0; k < nn; k++)
					column_copy[k] = a[k][col];

				// filter column
				filt.filter(column_copy, nn, 1, wksp);

				// update column:
				for (int k = 0; k < nn; k++)
					a[k][col] = column_copy[k];
			}
		}
	}

	/**
	 * Applies the direct transform on a signal.
	 */
	public void transform(float[] a) {
		int n = a.length;
		int nn;

		if (n < 4)
			return;

		float[] wksp = new float[n];

		for (nn = n; nn >= 4; nn >>>= 1)
			filt.filter(a, nn, 1, wksp);
	}
}
