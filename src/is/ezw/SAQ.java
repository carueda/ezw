package is.ezw;

import is.image.IMatrixImage;
import is.image.PNMImage;
import is.image.RawImage;
import is.image.StandardImage;
import is.io.SAQBitStream;
import is.io.SAQByteStream;
import is.io.ISAQInputStream;
import is.io.ISAQOutputStream;
import is.io.SAQTextStream;
import is.wavelet.Filter;
import is.wavelet.FilterManager;
import is.wavelet.Transformer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 * SAQ Encoder/Decoder. This is a "main" class that dispatches encoding and
 * decoding services.
 * 
 * @author Carlos Rueda
 * @version 0.1 March/18/1999
 * @version 0.2 Dec/02/1999
 */
public class SAQ {

	private static final String TITLE = "SAQ";
	private static final String DESCRIPTION = "SAQ Encoder/Decoder";
	private static final String VERSION = "Version 0.2 (Dec/02/1999)";

	private static ISAQInputStream createInputStream(String stream_file)
			throws Exception {
		if (stream_file.endsWith(".bit")) {
			return new SAQBitStream(new DataInputStream(
					new BufferedInputStream(new FileInputStream(stream_file))),
					null);
		} 
		else if (stream_file.endsWith(".byte")) {
			return new SAQByteStream(new DataInputStream(
					new BufferedInputStream(new FileInputStream(stream_file))),
					null);
		} 
		else if (stream_file.endsWith(".txt")) {
			return new SAQTextStream(new BufferedReader(new InputStreamReader(
					new FileInputStream(stream_file))), null);
		} 
		else {
			throw new RuntimeException("Unrecognized stream format extension: "
					+ stream_file);
		}
	}

	private static ISAQOutputStream createOutputStream(String stream_file)
			throws Exception {
		if (stream_file.endsWith(".bit")) {
			return new SAQBitStream(null,
					new DataOutputStream(new BufferedOutputStream(
							new FileOutputStream(stream_file))));
		} 
		else if (stream_file.endsWith(".byte")) {
			return new SAQByteStream(null,
					new DataOutputStream(new BufferedOutputStream(
							new FileOutputStream(stream_file))));
		} 
		else if (stream_file.endsWith(".txt")) {
			return new SAQTextStream(null, new PrintWriter(
					new FileOutputStream(stream_file)));
		} 
		else {
			throw new RuntimeException("Unrecognized stream format extension: "
					+ stream_file);
		}
	}

	/**
	 * SAQ Encoder/Decoder.
	 */
	public static void main(String[] args) throws Exception {
		System.out.println(TITLE + " - " + DESCRIPTION + "\n" + VERSION);

		final String usage = "USAGE:\n"
				+ "	SAQ -encodeImage image filter_name no_passes stream maxBytes\n"
				+ "		image  = i.pgm | i.raw [-d width height] | i.jpg | i.gif\n"
				+ "		stream = s.bit | s.byte | s.txt\n"
				+ "	SAQ -encodeMatrix n T0 passes matrix.raw stream\n"
				+ "	SAQ -decode stream base_name maxBytes\n"
				+ "\n"
				+ "	The stream format is taken according to the extension (.bit, .byte, or .txt).\n"
				+ "\n"
				+ "	-encodeImage takes an image (.pgm, .raw, .jpg, .gif), a filter name,\n"
				+ "	a number of passes, a number of bytes, and generates the symbol stream\n"
				+ "	with name stream.\n"
				+ "\n"
				+ "	-encodeMatrix takes n, T0, passes, an n by n matrix.raw, \n"
				+ "	and generates the symbol stream with name stream.\n"
				+ "\n"
				+ "	-decode reads the symbol stream, and makes raw matrices progressively\n"
				+ "	with the base_name.";

		try {
			if (args[0].equals("-encodeImage")) {
				int[] argn = { 1 };
				IMatrixImage mimage = readImage(args, argn);
				String filter_name = args[argn[0]++];
				int passes = Integer.parseInt(args[argn[0]++]);
				String stream_file = args[argn[0]++];
				int maxBytes = Integer.parseInt(args[argn[0]++]);

				ISAQOutputStream os = createOutputStream(stream_file);
				os.setOutputLimit(maxBytes);

				int[][] bmat = mimage.getValues();
				float[][] mat = SAQUtil.toFloatMatrix(bmat);

				short filter_code = FilterManager
						.getFilterCodeFromName(filter_name);
				if (filter_code < 0)
					throw new RuntimeException("Filter \"" + filter_name
							+ "\" unknown");

				Filter filter = FilterManager.createFilterFromCode(filter_code);

				new Transformer(filter).transform(mat);

				new SAQEncoder(filter_code, mat, passes, os).process();
			}
			else if (args[0].equals("-encodeMatrix")) {
				int n = Integer.parseInt(args[1]);
				int T0 = Integer.parseInt(args[2]);
				int passes = Integer.parseInt(args[3]);
				String matrix_file = args[4];
				String stream_file = args[5];
				float[][] mat = SAQUtil.readMatrix(new DataInputStream(
						new BufferedInputStream(
								new FileInputStream(matrix_file))), n);

				ISAQOutputStream os = createOutputStream(stream_file);

				// TODO: who knows which filter was applied?
				short filter_code = FilterManager.DAUB4;

				new SAQEncoder(filter_code, mat, T0, passes, os).process();
			} 
			else if (args[0].equals("-decode")) {
				String stream_file = args[1];
				String basename = args[2];
				int maxBytes = Integer.parseInt(args[3]);
				ISAQInputStream is = createInputStream(stream_file);
				is.setInputLimit(maxBytes);
				new SAQDecoder(is, basename).process();
			}
			else {
				System.err.println("Unrecognized operation option: " + args[0]);
			}
		} 
		catch (RuntimeException e) {
			System.out.println(usage);
		}

		System.exit(0);
	}

	private static IMatrixImage readImage(String[] args, int[] argn) throws Exception {
		String filename = args[argn[0]++];
		IMatrixImage mimage;

		if (filename.endsWith(".pgm")) {
			mimage = new PNMImage(filename);
		} 
		else if (filename.endsWith(".raw")) {
			int width;
			int height;

			if (argn[0] < args.length && args[argn[0]].equals("-d")) {
				argn[0]++;
				width = Integer.parseInt(args[argn[0]++]);
				height = Integer.parseInt(args[argn[0]++]);
			}
			else {
				File file = new File(filename);
				long size = file.length();
				width = height = (int) Math.sqrt(size);
			}
			mimage = new RawImage(width, height, 0, filename);

			if (width != height)
				throw new RuntimeException("Sorry, image must be square: "
						+ filename);

			mimage = new RawImage(width, height, 0, filename);
		} 
		else {
			File file = new File(filename);
			if (!file.exists() || !file.canRead()) {
				System.out.println("Cannot open " + filename);
				return null;
			}
			mimage = new StandardImage(filename);

			if (mimage == null)
				throw new RuntimeException("Unrecognized file extension: "
						+ filename);
		}

		return mimage;
	}
}
