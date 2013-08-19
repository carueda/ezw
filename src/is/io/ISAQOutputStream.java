package is.io;

/**
 * A symbol output stream used by a SAQEncoder.
 */
public interface ISAQOutputStream {
	/**
	 * Called when a pass with only n symbols begins.
	 */
	public void beginPass(int n);

	/**
	 * Flushes this stream.
	 */
	public void flush();

	/**
	 * Called to put a symbol.
	 */
	public void output(int symbol) throws SAQLimitReachedException;

	/**
	 * Sets the maximum number of bytes that can be written.
	 */
	public void setOutputLimit(int maxBytes);

	/**
	 * Writes a header for this stream.
	 */
	public void writeHeader(SAQHeader h);
}
