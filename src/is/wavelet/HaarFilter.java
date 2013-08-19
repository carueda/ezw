package is.wavelet;

/**
 * The Haar filter.
 * 
 * @author Carlos Rueda
 * @version 0.1 Dec/28/1999
 */
public class HaarFilter extends CoeffFilter {
	static private float[] coeffs = { 
		0.707106781186547524400844362104849f,
		0.707106781186547524400844362104849f 
	};

	public HaarFilter() {
		super(coeffs);
	}
}
