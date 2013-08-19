package is.wavelet;

/**
 * The Antonini forward wavelet filter from 9/7 tap.
 * Coefficients provided by Jorge Pinzon.

 	P E N D I N G		How to apply this???
 *
 * @author Carlos Rueda
 * @version 0.1 Dec/02/1999
 */
public class AntoniniForwardFilter extends CoeffFilter {
	static private float[] coeffs =	{
		 0.026749f,
		-0.016864f,
		-0.078223f,
		 0.266864f,
		 0.602949f,
		 0.266864f,
		-0.078223f,
		-0.016864f,
		 0.026749f
	};

	public AntoniniForwardFilter() {
		super(coeffs);	
	}
}
