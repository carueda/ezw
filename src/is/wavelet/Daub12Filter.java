package is.wavelet;

/**
 * The Daubechies-12 filter. Coefficients taken from Numerical Recipes.
 * 
 * @author Carlos Rueda
 * @version 0.1 Dec/12/1999
 */
public class Daub12Filter extends CoeffFilter {

	static private float[] coeffs = { 
		0.111540743350f, 0.494623890398f,
		0.751133908021f, 0.315250351709f, -0.226264693965f,
		-0.129766867567f, 0.097501605587f, 0.027522865530f,
		-0.031582039318f, 0.000553842201f, 0.004777257511f,
		-0.001077301085f 
	};

	public Daub12Filter() {
		super(coeffs);
	}
}
