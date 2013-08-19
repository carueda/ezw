package is.io;

/**
 * The header of a symbol stream.
 */
public class SAQHeader {
	/**
	 * Wavelet filter code.
	 * 
	 * @see is.wavelet.FilterManager
	 */
	public short filterCode;

	/** Number of wavelet scales. */
	public byte noScales;

	/** Dimension of the square matrix. */
	public short n;

	/** Image mean. */
	public float mean;

	/** Initial threshold. */
	public float T0;

	/**
	 * Constructor with default values.
	 */
	public SAQHeader() {
	}

	/**
	 * General constructor.
	 */
	public SAQHeader(short filterCode, byte noScales, short n, float mean,
			float T0) {
		this.filterCode = filterCode;
		this.noScales = noScales;
		this.n = n;
		this.mean = mean;
		this.T0 = T0;
	}
}
