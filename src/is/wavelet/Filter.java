package is.wavelet;

/**
 * A wavelet filter has to implement this interface. 
 * This is preliminary version inspired by Numerical Recipes.
 * 
 * @author Carlos Rueda
 * @version 0.1 March/25/1999
 */
public interface Filter {
	/**
	 * Applies this wavelet filter to data vector a[0..n-1] (for isign==1) or
	 * applies its transpose (for isign==-1), using workspace wksp[0..n-1];
	 */
	public void filter(float[] a, int n, int isign, float[] wksp);
}
