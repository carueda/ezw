package is.io;

import java.io.EOFException;

/**
 * A symbol input stream used by a SAQDecoder.
 */
public interface ISAQInputStream {
	/**
	 * Called when a pass with only n symbols begins.
	 */
	public void beginPass(int n);

	/**
	 * Returns how many bytes have been read.
	 */
	public int getNumBytesRead();

	/**
	 * Called by a SAQDecoder to get a symbol.
	 */
	public int input() throws EOFException, SAQLimitReachedException;

	/**
	 * Reads the header of this stream.
	 */
	public SAQHeader readHeader();

	/**
	 * Sets the maximum number of bytes that can be read.
	 */
	public void setInputLimit(int maxBytes);
}
