package is.image;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;

/**
 * PGM format to Raw format converter.
 * 
 * @author Carlos Rueda
 * @version 0.1 Mar/26/1999
 */
public class PGM2Raw {
	public static void main(String[] args) throws Exception {
		final String usage = "\n" + "USAGE: PGM2Raw file.pgm file.raw\n" + "\n"
				+ "	PGM format to Raw format converter.\n" + "\n"
				+ "	Note that the maximum gray level must be 255.";

		if (args.length != 2) {
			System.out.println(usage);
			return;
		}

		String name_in = args[0];
		String name_out = args[1];

		PNMImage pgm = new PNMImage(name_in);

		int[][] values = pgm.getValues();
		DataOutputStream out = new DataOutputStream(new BufferedOutputStream(
				new FileOutputStream(name_out)));

		for (int h = 0; h < values.length; h++)
			for (int w = 0; w < values[0].length; w++)
				out.writeByte(values[h][w]);

		out.flush();
	}
}
