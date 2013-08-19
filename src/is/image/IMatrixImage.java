package is.image;

/**
 * A "matrix" image (a better name?).
 * 
 * @author Carlos Rueda
 * @version 0.1 Mar/30/1999
 */
public interface IMatrixImage {
	/**
	 * Returns the number of values successfully read.
	 */
	public int getNumValuesRead();

	/**
	 * Returns the pixel array suitable to create a java.awt.Image.
	 */
	public int[] getPixelArray();

	/**
	 * Returns the matrix of values of this image.
	 */
	public int[][] getValues();
}
