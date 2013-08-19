package is.wavelet;

/**
 * The Antonini inverse wavelet filter from 9/7 tap.
 * Coefficients provided by Jorge Pinzon.

 	P E N D I N G		How to apply this???
 *
 * @author Carlos Rueda
 * @version 0.1 Dec/02/1999
 */
public class AntoniniInverseFilter extends CoeffFilter {
	static private float[] coeffs =	{
		-0.045636f,
		-0.028772f,
		 0.295636f,
		 0.557543f,
		 0.295636f,
		-0.028772f,
		-0.045636f
	};

	public AntoniniInverseFilter() {
		super(coeffs);	
	}
}
