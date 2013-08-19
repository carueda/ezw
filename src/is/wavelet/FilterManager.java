package is.wavelet;

/**
 * Creates wavelet filters according to codes or class names.
 * 
 * @author Carlos Rueda
 * @version 0.1 Dec/12/1999
 */
public class FilterManager {
	/** The Haar filter. */
	public static final int HAAR = 0;

	/** The Daubechies-4 filter. */
	public static final int DAUB4 = 1;

	/** The Daubechies-12 filter. */
	public static final int DAUB12 = 2;

	/** The Antonini filter. PENDING */
	public static final int ANTONINI = 30;

	/** Short wavelet names. */
	final static String[] names = { "haar", "daub4", "daub12", "antonini" };


	/**
	 * Creates a wavelet filter according to a given class name.
	 */
	public static Filter createFilterFromClassName(String class_name) {
		try {
			return (Filter) Class.forName(class_name).newInstance();
		}
		catch (Exception ex) {
			return null;
		}
	}

	/**
	 * Creates a wavelet filter according to the code given.
	 */
	public static Filter createFilterFromCode(int code) {
		switch (code) {
		case HAAR:
			return new HaarFilter();

		case DAUB4:
			return new Daub4Filter();

		case DAUB12:
			return new Daub12Filter();

		case ANTONINI:
			return new AntoniniForwardFilter();
		}

		throw new RuntimeException("Unrecognized filter code: " + code);
	}

	/**
	 * Returns the code of the wavelet filter with the given short name. Case is
	 * ignored.
	 */
	public static short getFilterCodeFromName(String name) {
		for (short i = 0; i < names.length; i++) {
			if (names[i].equalsIgnoreCase(name))
				return i;
		}

		return -1;
	}
}
