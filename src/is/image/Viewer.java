package is.image;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Panel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.MemoryImageSource;
import java.io.File;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;

/**
 * Image Viewer. This program can display images with formats: .bit, .pgm, .ppm,
 * .raw, or other recognized by Java (.jpg, .gif).
 * 
 * @author Carlos Rueda
 * @version 0.3 Dec/12/1999
 * @version 0.2 Mar/31/1999
 */
public class Viewer extends Panel {
	private static final long serialVersionUID = 1L;

	private static final String TITLE = "Image Viewer";
	private static final String DESCRIPTION = "Viewer of images in diferent formats";
	private static final String VERSION = "Version 0.3 (Dec/12/1999)";
	private static final String USAGE = 
			"	is.image.Viewer image image ...\n"
			+ "\n" 
			+ "	Each image must be specified as follows:\n" + "\n"
			+ "		image.bit [-b maxBytesToRead]\n" 
			+ "		image.pgm\n"
			+ "		image.ppm\n" 
			+ "		[-raw] image.raw [-d width height offset]\n"
			+ "		image.xxx (some format recognized by Java)\n";

	/** The image to draw in the graphics. */
	private Image img;

	private static PrintWriter out;

	private static boolean fromMain;

	private static int expand = 1;

	public Viewer(IMatrixImage mimage) {
		super();

		int[][] values = mimage.getValues();
		int w = values[0].length;
		int h = values.length;
		int pix[] = mimage.getPixelArray();
		img = createImage(new MemoryImageSource(w, h, pix, 0, w));
	}

	public void paint(Graphics g) {
		g.drawImage(img, 0, 0, expand*img.getWidth(null), expand*img.getHeight(null), null);
	}

	public Dimension getPreferredSize() {
		return new Dimension(expand*img.getWidth(null), expand*img.getHeight(null));
	}

	public static void main(String[] args) throws Exception {
		fromMain = true;
		out = new PrintWriter(System.out, true);

		service(args);
	}

	private static void service(String[] args) throws Exception {
		out.println(TITLE + " - " + DESCRIPTION + "\n" + VERSION);

		if (args.length == 0) {
			out.println("USAGE: " + USAGE);
			return;
		}

		WindowListener wl = new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				((Frame) e.getSource()).dispose();
				if (fromMain)
					System.exit(0);
			}
		};

		String filename;
		IMatrixImage mimage = null;

		try {
			int x = 0, y = 0; // To locate frames
			int arg = 0; // to scan arguments
			while (arg < args.length) {
				filename = args[arg++];

				if (filename.equals("-expand")) {
					if (arg < args.length)
						expand = Integer.parseInt(args[arg++]);
					System.out.println("expand = " + expand);
					continue;
				}

				if (filename.endsWith(".bit")) {
					int maxBytes = Integer.MAX_VALUE;
					if (arg < args.length && args[arg].equals("-b")) {
						arg++;
						maxBytes = Integer.parseInt(args[arg++]);
					}
					String filter_name = "daub12";
					mimage = new BitImage(filename, maxBytes, filter_name);
				} 
				else if (filename.endsWith(".pgm")
						|| filename.endsWith(".ppm")) {
					mimage = new PNMImage(filename);
				} 
				else if (filename.endsWith(".raw") || filename.equals("-raw")) {
					if (filename.equals("-raw"))
						filename = args[arg++];

					int width;
					int height;
					int offset;

					if (arg < args.length && args[arg].equals("-d")) {
						arg++;
						width = Integer.parseInt(args[arg++]);
						height = Integer.parseInt(args[arg++]);
						offset = Integer.parseInt(args[arg++]);
					} else {
						File file = new File(filename);
						long size = file.length();
						width = height = (int) Math.sqrt(size);
						offset = 0;
					}
					mimage = new RawImage(width, height, offset, filename);
				} 
				else {
					File file = new File(filename);
					if (!file.exists() || !file.canRead()) {
						out.println("Cannot open " + filename);
						continue;
					}
					mimage = new StandardImage(filename);

					if (mimage == null)
						throw new RuntimeException(
								"Unrecognized file extension: " + filename);
				}

				Viewer v = new Viewer(mimage);
				Frame f = new Frame("Image Viewer - " + filename);
				if (wl != null)
					f.addWindowListener(wl);
				f.add(v);
				f.pack();
				f.setLocation(x, y);
				x += 30;
				y += 20;
				f.setVisible(true);
			}
		} 
		catch (ArrayIndexOutOfBoundsException e) {
			out.println("USAGE" + USAGE);
			return;
		}

	}

	public static void service(String[] args, Reader in_, Writer out_)
			throws Exception {
		fromMain = false;
		out = new PrintWriter(out_, true);

		service(args);
	}
}
