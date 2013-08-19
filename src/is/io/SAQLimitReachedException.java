package is.io;

/**
 * To control a given limit criterion in a symbol stream.
 */
public class SAQLimitReachedException extends Exception {
	private static final long serialVersionUID = 1L;

	public SAQLimitReachedException() {
		super();
	}

	public SAQLimitReachedException(String m) {
		super(m);
	}
}
