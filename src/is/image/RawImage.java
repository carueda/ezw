package is.image;

import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Image in byte raw format.
 * 
 * @author Carlos Rueda
 * @version 0.2 Mar/30/1999
 * @version 0.1 Feb/26/1999
 */
public class RawImage implements IMatrixImage {
	/** The image. */
	private int[][] image;

	/** The data. */
	private float[][] data;

	/** Number of values successfully read. */
	private int numRead;

	/**
	 * Constructs a byte raw image given a file name. Normally this name ends
	 * with ''.raw''.
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
	public RawImage(int width, int height, int offset, String name)
			throws Exception {
		FileInputStream fis = new FileInputStream(name);
		DataInputStream in = new DataInputStream(new BufferedInputStream(fis));
		image = new int[height][width];
		data = new float[height][width];
		numRead = 0;

		fis.skip(offset);
		File file = new File(name);
		long size = file.length() - offset;

		try {
			if (size == 1 * width * height) // byte format
			{
				System.out.println("format: byte");
				for (int h = 0; h < image.length; h++) {
					for (int w = 0; w < image[0].length; w++) {
						int val = in.readUnsignedByte();
						image[h][w] = val;
						data[h][w] = (float) val;
						numRead++;
					}
				}
			}
			else if (size == 2 * width * height) // short format
			{
				System.out.println("format: short");
				int max = Integer.MIN_VALUE;
				int min = Integer.MAX_VALUE;
				// int[][] dd = new int[height][width];
				for (int h = 0; h < image.length; h++) {
					for (int w = 0; w < image[0].length; w++) {
						int val = (int) is.ezw.SAQUtil._ReadShort(in);
						if (max < val)
							max = val;
						if (min > val)
							min = val;
						data[h][w] = val;
						numRead++;
					}
				}

				max -= min;
				for (int h = 0; h < image.length; h++) {
					for (int w = 0; w < image[0].length; w++) {
						double val = data[h][w] - min;
						val = val * 255 / max;
						image[h][w] = (int) val;
					}
				}
			}
			else if (size == 4 * width * height) // float format
			{
				System.out.println("format: float");
				float max = Float.MIN_VALUE;
				float min = Float.MAX_VALUE;
				for (int h = 0; h < image.length; h++) {
					for (int w = 0; w < image[0].length; w++) {
						float val = (float) is.ezw.SAQUtil._ReadFloat(in);
						data[h][w] = val;

						if (max < val)
							max = val;
						if (min > val)
							min = val;
						numRead++;
					}
				}
				if (false) {
					max -= min;
					// DataOutputStream out = new DataOutputStream(new
					// FileOutputStream("out"));
					for (int h = 0; h < image.length; h++) {
						for (int w = 0; w < image[0].length; w++) {
							float val = data[h][w];
							// out.writeFloat(val);
							val = val - min;
							val = val * 255 / max;
							image[h][w] = (int) val;
						}
					}
				}
				// out.close();
			}
			else if (size == 4 * width * height) // float format
			{
				System.out.println("format: ??");
			}
		}
		catch (IOException e) {
			// ignored
		}
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
				try {
					pix[index++] = new Color(val, val, val).getRGB();
				}
				catch (IllegalArgumentException ex) {
					System.out.println("val = " + val);
					throw ex;
				}
			}
		}

		return pix;
	}

	public int[][] getValues() {
		return image;
	}
}
