package is.image;

import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Image in portable graymap file format.
 * 
 * @author Carlos Rueda
 * @version 0.1 Feb/26/1999
 * @version 0.2 Mar/30/1999
 */
public class PNMImage implements IMatrixImage {
	/** The magic number: P2, P5, P3, or P6. */
	String magic;

	/** Is PPM? */
	boolean isPPM;

	/** The maximum component value. */
	int max;

	/** The image. */
	int[][] image;

	/** Number of values successfully read. */
	int numRead;

	private DataInputStream in;

	private char lookahead;

	/**
	 * Constructs a PNM image given a file name. Normally this name ends with
	 * .pgm or .ppm .
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
	public PNMImage(String name) throws Exception {
		FileInputStream fis = new FileInputStream(name);
		in = new DataInputStream(new BufferedInputStream(fis));
		// InputStreamReader isr = new InputStreamReader(in);
		byte[] mag = new byte[2];
		in.read(mag);
		magic = new String(mag);
		if (magic.equals("P2") || magic.equals("P5"))
			isPPM = false;
		else if (magic.equals("P3") || magic.equals("P6"))
			isPPM = true;
		else
			throw new IOException("Invalid PNM file");

		boolean isAscii = magic.equals("P2") || magic.equals("P3");

		// init lookahead to read tokens
		lookahead = (char) in.readUnsignedByte();

		// read header
		int width = nextValue();
		int height = nextValue();
		max = nextValue();

		// prepare image and read values according to value format
		image = new int[height][width];
		if (isAscii)
			readAsciiValues();
		else
			readBinaryValues();

		in.close();
	}

	public int getNumValuesRead() {
		return numRead;
	}

	public int[] getPixelArray() {
		int width = image[0].length;
		int height = image.length;

		int pix[] = new int[width * height];
		int index = 0;
		for (int h = 0; h < height; h++) {
			for (int w = 0; w < width; w++) {
				int val = image[h][w];
				Color col;
				if (isPPM) {
					int b = val & 0xff;
					val >>>= 8;
					int g = val & 0xff;
					val >>>= 8;
					int r = val & 0xff;
					col = new Color(r, g, b);
				}
				else
					col = new Color(val, val, val);

				pix[index++] = col.getRGB();
			}
		}

		return pix;
	}

	public int[][] getValues() {
		return image;
	}

	/**
	 * Returns the next integer value read from ASCII format.
	 */
	private int nextValue() throws IOException {
		while (Character.isWhitespace(lookahead) || lookahead == '#') {
			// skip whitespaces
			while (Character.isWhitespace(lookahead))
				lookahead = (char) in.readUnsignedByte();

			// skip comment
			if (lookahead == '#') {
				do {
					lookahead = (char) in.readUnsignedByte();
				} while (lookahead != '\n');
				lookahead = (char) in.readUnsignedByte();
			}
		}

		if (!Character.isDigit(lookahead))
			throw new IOException("Invalid PNM file");

		int value = 0;
		while (Character.isDigit(lookahead)) {
			value = value * 10 + lookahead - '0';
			lookahead = (char) in.readUnsignedByte();
		}
		return value;
	}

	private void readAsciiValues() throws IOException {
		numRead = 0;
		try {
			for (int h = 0; h < image.length; h++) {
				for (int w = 0; w < image[0].length; w++) {
					int val;
					if (isPPM) {
						int r = nextValue();
						int g = nextValue();
						int b = nextValue();
						val = r << 16 | g << 8 | b;
					}
					else
						val = nextValue();

					image[h][w] = val;
					numRead++;
				}
			}
		}
		catch (IOException e) {
			// ignored
		}
	}

	private void readBinaryValues() throws IOException {
		numRead = 0;
		try {
			for (int h = 0; h < image.length; h++) {
				for (int w = 0; w < image[0].length; w++) {
					int val;
					if (isPPM) {
						int r = in.readUnsignedByte();
						int g = in.readUnsignedByte();
						int b = in.readUnsignedByte();
						val = r << 16 | g << 8 | b;
					}
					else
						val = in.readUnsignedByte();

					image[h][w] = val;
					numRead++;
				}
			}
		}
		catch (IOException e) {
			// ignored
		}
	}
}
