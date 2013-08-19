package is.ezw;

import java.awt.Point;
import java.io.*;

import is.wavelet.Daub4Filter;
import is.wavelet.Transformer;

/**
 * various utilities.
 *  
 * @author Carlos Rueda
 * @version 0.2 Dec/12/1999
 * @version 0.1 March/14/1999
 */
public class SAQUtil {
	static final boolean debug = false;

	public static float _ReadFloat(DataInputStream in) {
		float value = 0f;
		try {
			int accum = 0;
			for (int shiftBy = 0; shiftBy < 32; shiftBy += 8) {
				accum |= (in.readUnsignedByte() & 0xff) << shiftBy;
			}
			value = Float.intBitsToFloat(accum);
		}
		catch (Exception e) {
			System.err.println(e);
		}

		return value;
	}

	static void _WriteFloat(DataOutputStream out, float value) {
		try {
			int n = Float.floatToIntBits(value);

			int[] b = new int[4];

			for (int k = 0; k < b.length; k++) {
				b[k] = n & 0xff;
				n >>>= 8;
			}
			for (int k = b.length - 1; k >= 0; k--) {
				out.writeByte(b[k]);
			}

		}
		catch (Exception e) {
			System.err.println(e);
		}
	}

	public static short _ReadShort(DataInputStream in) {
		short value = 0;
		try {
			int low = in.readUnsignedByte() & 0xff;
			int high = in.readUnsignedByte();
			value = (short) (high << 8 | low);
		}
		catch (Exception e) {
			System.err.println(e);
		}

		return value;
	}

	/**
	 */
	public static void byte2float(DataInputStream in, int n,
			DataOutputStream out) throws Exception {
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				int value = in.readUnsignedByte();
				out.writeFloat((float) value);
			}
		}
		out.flush();
	}

	static void debug(String line) {
		if (debug)
			System.err.println(line);
	}

	/**
	 */
	public static void float2byte(DataInputStream in, int n,
			DataOutputStream out) throws Exception {
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				float value = in.readFloat();
				out.writeByte((byte) value);
			}
		}
		out.flush();
	}

	/**
	 * Gets the mean square error
	 */
	public static double getMSE(float[][] mat1, float[][] mat2) {
		int n = mat1.length;
		double sum = 0.;

		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				double dif = mat1[i][j] - mat2[i][j];
				sum += dif * dif;
			}
		}

		return sum / (double) (n * n);
	}

	/**
	 * Calculates mean, max, and min of a matrix.
	 */
	public static float[] getStats(float[][] mat) {
		float sum = 0f, mean = 0f, max = Float.MIN_VALUE, min = Float.MAX_VALUE;
		int n = mat.length;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				float value = mat[i][j];
				if (min > value)
					min = value;
				if (max < value)
					max = value;
				sum += value;
			}
		}

		if (sum != 0f)
			mean = sum / (n * n);

		return new float[] { mean, max, min };
	}

	// <stretch>

	/** The actual value used to compute the stretch */
	private static float fun(float val) {
		return Math.abs(val);
	}

	/** Aux to update extremes in a quadrant */
	private static void stretchArea1(float[] maxmin, float[][] values, int x,
			int y, int n) {
		float max = maxmin[0];
		float min = maxmin[1];
		for (int h = 0; h < n; h++) {
			for (int w = 0; w < n; w++) {
				float v = fun(values[y + h][x + w]);
				if (min > v) {
					min = v;
				}
				if (max < v) {
					max = v;
				}
			}
		}
		maxmin[0] = max;
		maxmin[1] = min;
	}

	/** Aux to stretch on a quadrant given the extremes */
	private static void stretchArea2(float[] maxmin, float[][] values, int x,
			int y, int n) {
		float max = maxmin[0];
		float min = maxmin[1];
		float dist = max - min;

		for (int h = 0; h < n; h++) {
			for (int w = 0; w < n; w++) {
				if (dist < 10e-5) {
					values[y + h][x + w] = 0;
				}
				else {
					float val = fun(values[y + h][x + w]) - min;
					values[y + h][x + w] = val * 255 / dist;
				}
			}
		}
	}

	/** stretches the (0,0) quadrant of size n x n. */
	public static void stretch(float[][] values, int n) {
		if (n <= 1) {
			values[0][0] = 0;
		}
		else {
			int n2 = n / 2;
			stretch(values, n2);
			float[] maxmin = { Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY };
			stretchArea1(maxmin, values, n2, 0, n2);
			stretchArea1(maxmin, values, 0, n2, n2);
			stretchArea1(maxmin, values, n2, n2, n2);

			stretchArea2(maxmin, values, n2, 0, n2);
			stretchArea2(maxmin, values, 0, n2, n2);
			stretchArea2(maxmin, values, n2, n2, n2);
		}
	}

	// </stretch>

	/**
	 * 
	 */
	public static void main(String[] args) throws Exception {
		final String usage = "SAQUtil - Utilities\n"
				+ "USAGE:\n"
				+ "	SAQUtil -show n matrix.raw\n"
				+ "	SAQUtil -stats n matrix.raw\n"
				+ "	SAQUtil -mse n matrix1.raw matrix2.raw\n"
				+ "	SAQUtil -f2b n float_matrix.raw byte_matrix.raw\n"
				+ "	SAQUtil -b2f n byte_matrix.raw float_matrix.raw\n"
				+ "	SAQUtil -reverse n matrix.raw\n"
				+ "	SAQUtil -wtdaub4 n matrix.raw wmatrix.raw\n"
				+ "	SAQUtil -iwtdaub4 n wmatrix.raw matrix.raw\n"
				+ "	SAQUtil -f2pgm n matrix.raw outfile.pgm\n"
				+ "\n"
				+ "	n is the dimension of the matrix, n by n.\n"
				+ "	-reverse reverses the bytes of floats and generates rmatrix.raw.\n";

		try {
			int n = Integer.parseInt(args[1]);
			DataInputStream in = new DataInputStream(new BufferedInputStream(
					new FileInputStream(args[2])));

			if (args[0].equals("-show")) {
				float[][] mat = readMatrix(in, n);
				printMatrix(mat);
			}
			else if (args[0].equals("-stats")) {
				float[][] mat = readMatrix(in, n);
				float[] stats = getStats(mat);
				System.out.println("mean =\t" + stats[0] + "\n" + "max =\t"
						+ stats[1] + "\n" + "min =\t" + stats[2]);
			}
			else if (args[0].equals("-mse")) {
				System.out.println("Calculating MSE for " + args[2] + " y "
						+ args[3] + "...");
				float[][] mat = readMatrix(in, n);
				float[][] mat2 = readMatrix(new DataInputStream(
						new BufferedInputStream(new FileInputStream(args[3]))),
						n);

				double mse = getMSE(mat, mat2);
				System.out.println("MSE =\t" + mse);
			}
			else if (args[0].equals("-reverse")) {
				DataOutputStream out = new DataOutputStream(
						new BufferedOutputStream(new FileOutputStream("r-"
								+ args[2])));
				reverseFloats(in, n, out);
				out.close();
			}
			else if (args[0].equals("-f2b")) {
				DataOutputStream out = new DataOutputStream(
						new BufferedOutputStream(new FileOutputStream(args[3])));
				float2byte(in, n, out);
				out.close();
			}
			else if (args[0].equals("-b2f")) {
				DataOutputStream out = new DataOutputStream(
						new BufferedOutputStream(new FileOutputStream(args[3])));
				byte2float(in, n, out);
				out.close();
			}
			else if (args[0].equals("-wtdaub4")) {
				System.out.println("Reading matrix... ");
				float[][] mat = readMatrix(in, n);
				System.out.println("Calculating Daub4 wavelet transform... ");
				Transformer wt = new Transformer(new Daub4Filter());
				wt.transform(mat);
				System.out.println("Writing transform in " + args[3] + "... ");
				writeMatrix(args[3], mat);
				System.out.println("Ready.");
			}
			else if (args[0].equals("-iwtdaub4")) {
				System.out.println("Reading matrix transform... ");
				float[][] mat = readMatrix(in, n);
				System.out
						.println("Calculating Daub4 inverse wavelet transform... ");
				Transformer wt = new Transformer(new Daub4Filter());
				wt.inverse(mat);
				System.out.println("Writing matrix in " + args[3] + "... ");
				writeMatrix(args[3], mat);
				System.out.println("Ready.");
			}
			else if (args[0].equals("-f2pgm")) {
				System.out.println("Converting " + args[2] + " to PGM: "
						+ args[3] + "...");
				float[][] values = readMatrix(in, n);
				SAQUtil.stretch(values, values.length);
				PrintWriter out = new PrintWriter(new BufferedOutputStream(
						new FileOutputStream(args[3])));

				out.println("P2\n256 256 255\n");
				for (int h = 0; h < values.length; h++) {
					for (int w = 0; w < values[0].length; w++) {
						float val = values[h][w];
						int ival = (int) val;
						out.print(ival + " ");
					}
					out.println();
				}
				out.flush();
			}
			else {
				System.err.println("Unrecognized operation option: " + args[0]);
			}

			in.close();
		}
		catch (RuntimeException e) {
			System.out.println(usage);
		}

	}

	static void output(PrintWriter pw, int coeff, String code) {
		if (debug)
			pw.println(coeff + "\t" + code);
		else
			// pw.println(code);
			System.out.println(code);
	}

	static void output(PrintWriter pw, String code) {
		if (debug)
			pw.println("\t" + code);
		else
			// pw.println(code);
			System.out.println(code);
	}

	static void printMatrix(float[][] mat) {
		System.err.println("mat=");
		for (int y = 0; y < mat.length; y++) {
			System.err.print("[");
			for (int x = 0; x < mat.length; x++)
				System.err.print(mat[y][x] + "  ");
			System.err.println("]");
		}
	}

	static void printPoints(String name, SAQList v) {
		if (!debug)
			return;

		System.err.print(name + "=[");
		for (int i = 0; i < v.size(); i++) {
			Point p = (Point) v.elementAt(i);
			if (i > 0)
				System.err.print(",");
			System.err.print("(" + p.x + "," + p.y + ")");
		}
		System.err.println("]");
	}

	static void printValues(String name, SAQList v, float[][] mat) {
		if (!debug)
			return;

		System.err.print(name + "=[");
		for (int i = 0; i < v.size(); i++) {
			Point p = (Point) v.elementAt(i);
			if (i > 0)
				System.err.print(",");
			System.err.print(mat[p.y][p.x]);
		}
		System.err.println("]");
	}

	/**
	 * Reads an n by n matrix in byte format from a DataInputStream.
	 */
	public static int[][] readByteMatrix(DataInputStream in, int n)
			throws Exception {
		int mat[][] = new int[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				mat[i][j] = in.readUnsignedByte();
			}
		}
		return mat;
	}

	/**
	 * Reads an n by n matrix from a DataInputStream.
	 */
	public static float[][] readMatrix(DataInputStream in, int n)
			throws Exception {
		float mat[][] = new float[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				mat[i][j] = in.readFloat();
			}
		}
		return mat;
	}

	/**
	 */
	public static void reverseFloats(DataInputStream in, int n,
			DataOutputStream out) throws Exception {
		int[] b = new int[4];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				for (int k = 0; k < b.length; k++) {
					b[k] = in.readUnsignedByte();
				}
				for (int k = b.length - 1; k >= 0; k--) {
					out.writeByte(b[k]);
				}
			}
		}
		out.flush();
	}

	/**
	 * Converts a matrix in byte format to float format.
	 */
	public static float[][] toFloatMatrix(int[][] bmat) {
		int n = bmat.length;
		float mat[][] = new float[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				mat[i][j] = (float) bmat[i][j];
			}
		}
		return mat;
	}

	/**
	 * Converts a matrix in byte format to float format.
	 */
	public static void toFloatMatrix(int[][] bmat, float[][] fmat) {
		int n = bmat.length;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				fmat[i][j] = (float) bmat[i][j];
			}
		}
	}

	/**
	 * Converts a matrix in float format to byte format.
	 */
	public static void toIntMatrix(float[][] fmat, int[][] bmat) {
		int n = fmat.length;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				bmat[i][j] = (int) fmat[i][j];
			}
		}
	}

	static void writeMatrix(String filename, float[][] mat) {
		try {
			DataOutputStream out = new DataOutputStream(
					new BufferedOutputStream(new FileOutputStream(filename)));

			for (int y = 0; y < mat.length; y++) {
				for (int x = 0; x < mat.length; x++)
					out.writeFloat(mat[y][x]);
			}
			out.close();
		}
		catch (Exception e) {
			System.err.println(e);
		}
	}
}
