package is.image;

import is.ezw.IMatrixObserver;
import is.ezw.SAQDecoder;
import is.ezw.SAQUtil;
import is.io.SAQBitStream;
import is.io.SAQHeader;
import is.io.ISAQInputStream;
import is.wavelet.Filter;
import is.wavelet.FilterManager;
import is.wavelet.Transformer;

import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Image in bit format.
 * 
 * @author Carlos Rueda
 * @version 0.1 Mar/31/1999
 */
public class BitImage implements IMatrixImage, IMatrixObserver {
	SAQHeader header;

	Transformer wt;

	/** The image. */
	int[][] image;

	float[][] fimage;

	/** Number of values successfully read. */
	int numRead;

	public BitImage(String name, int maxBytes, String filter_name)
			throws Exception {
		ISAQInputStream is = createInputStream(name);
		is.setInputLimit(maxBytes);
		SAQDecoder dec = new SAQDecoder(is, this);
		dec.process();
	}

	/**
	 * Constructs an image given a file name. Normally this name ends with
	 * ''.bit''.
	 * 
	 * @param name
	 *            The name of the file containing the image.
	 * 
	 * @exception IOException
	 *                If any kind of I/O exception arises. This includes bad or
	 *                corrupted file format. Note that the impossibility to read
	 *                all the values defined in the header causes no exception.
	 *                Call getNumValuesRead to know how many values have been
	 *                successfully read.
	 */
	public BitImage(String name, String filter_name) throws Exception {
		this(name, 0, filter_name);
	}

	private static ISAQInputStream createInputStream(String stream_file)
			throws Exception {
		if (stream_file.endsWith(".bit")) {
			return new SAQBitStream(new DataInputStream(
					new BufferedInputStream(new FileInputStream(stream_file))),
					null);
		}
		else {
			throw new RuntimeException("stream format extension is not .bit: "
					+ stream_file);
		}
	}

	public int getNumValuesRead() {
		return numRead;
	}

	public int[] getPixelArray() {
		wt.inverse(fimage);
		SAQUtil.toIntMatrix(fimage, image);

		int width = image[0].length;
		int height = image.length;

		int pix[] = new int[width * height];
		int index = 0;
		for (int h = 0; h < height; h++) {
			for (int w = 0; w < width; w++) {
				int val = image[h][w] & 0xff;
				pix[index++] = new Color(val, val, val).getRGB();
			}
		}

		return pix;
	}

	public int[][] getValues() {
		return image;
	}

	public void takeHeader(SAQHeader h) {
		header = h;
		image = new int[header.n][header.n];
		fimage = new float[header.n][header.n];

		Filter filter = FilterManager.createFilterFromCode(h.filterCode);
		if (filter == null)
			throw new RuntimeException("Filter code " + h.filterCode
					+ " unknown");

		wt = new Transformer(filter);

	}

	public void takeMatrix(float[][] mat, int numBytesRead) {
		if (header == null)
			throw new RuntimeException("takeHeader not invoked yet!");

		for (int y = 0; y < header.n; y++) {
			for (int x = 0; x < header.n; x++) {
				fimage[y][x] = mat[y][x];
			}
		}
	}
}
