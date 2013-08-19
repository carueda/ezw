package is.ezw;

import is.io.SAQHeader;

/**
 * An object that wants to be notified of new matrices progressively made by a
 * decoder must implement this interface.
 * 
 * @author Carlos Rueda
 * @version 0.1 March/24/1999
 */
public interface IMatrixObserver {
	/**
	 * Called when the header of symbol stream is known.
	 */
	public void takeHeader(SAQHeader h);

	/**
	 * Called when a pass (dominant and subordinate) has been finished and an
	 * updated matrix has been calculated. Also the number of bytes read from
	 * the symbol stream according to the ISAQInputStream used is notified.
	 */
	public void takeMatrix(float[][] mat, int numBytesRead);
}
