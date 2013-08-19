package is.ezw;

import is.io.SAQBitStream;
import is.io.SAQByteStream;
import is.io.SAQHeader;
import is.io.ISAQInputStream;
import is.io.SAQTextStream;
import is.wavelet.Filter;
import is.wavelet.FilterManager;
import is.wavelet.Transformer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.MemoryImageSource;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;

/**
 * Progressive Viewer of an image in bit format.
 * 
 * @author Carlos Rueda
 * @version 0.1 Mar/24/1999
 * @version 0.2 Dec/12/1999
 * @version 0.3 Dec/28/1999
 */
public class ProgressiveViewer extends Panel {
	private static final long serialVersionUID = 1L;

	private static final String TITLE = "Progressive Bit Viewer";
	private static final String DESCRIPTION = "Progressive viewer of an image in SAQ format.";
	private static final String VERSION = "Version 0.3 (Dec/28/1999)";

	private static final String USAGE = 
			  "ProgressiveViewer [-delay ms] [-expand factor] image\n"
			+ "		image  = i.bit | i.byte | i.txt\n" + "\n"
			+ "	Pauses ms millisecs between images; shows the bytes consumed,\n"
			+ "	bpp (bits-per-pixel), and percentage of original image size.\n";

	private Label label;

	private Image img;


	private int numBytesRead;

	private Transformer wt;

	private SAQHeader header;

	private float[][] dmat;


	private static int expand = 1;
	
	private int pix[];

	// for wavelet image
	private Image wimg;
	private float[][] wmat;  
	private int wpix[];

	private int delay;

//	private static BufferedReader in;

	private static PrintWriter out;

	private static boolean fromMain;

	public ProgressiveViewer(int delay, Label label) throws Exception {
		this.label = label;
		header = null;
		dmat = null;
		pix = null;

		this.delay = delay;
	}

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
			throw new RuntimeException(
					"Stream format extension is not .bit, .byte, or .txt: "
							+ stream_file);
		}
	}

	public Dimension getPreferredSize() {
		Dimension d;
		if (header != null)
			d = new Dimension(2 * expand*header.n + 4, expand*header.n + 12);
		else
			d = new Dimension(400, 400);
		return d;

	}

	public static void main(String[] args) throws Exception {
		fromMain = true;
//		in = new BufferedReader(new InputStreamReader(System.in));
		out = new PrintWriter(System.out, true);

		service(args);
	}

	public void paint(Graphics g) {
		update(g);
	}

	private static void service(String[] args) throws Exception {
		out.println(TITLE + " - " + DESCRIPTION + "\n" + VERSION);

		int delay = 500;
		int arg = 0;
		
		for (; arg < args.length; arg++) {
			if (args[arg].equals("-delay")) {
				delay = Integer.parseInt(args[++arg]);				
			}
			else if (args[arg].equals("-expand")) {
				expand = Integer.parseInt(args[++arg]);				
			}
			else {
				break;
			}
		}
		if (arg >= args.length) {
			out.println("USAGE: " + USAGE);
			return;
		}

		String stream_file = args[arg];
		
		Label label = new Label();
		ProgressiveViewer pv = new ProgressiveViewer(delay, label);
		ISAQInputStream is = createInputStream(stream_file);
		SAQDecoder dec = new SAQDecoder(is, pv.matrixObserver);

		Frame f = new Frame(TITLE + " - " + stream_file);
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				((Frame) e.getSource()).dispose();
				if (fromMain)
					System.exit(0);
			}
		});
		f.add(pv);
		f.add(label, "South");
		f.pack();
		f.setLocation(200, 40);
		f.setVisible(true);

		dec.process();
	}

	public static void service(String[] args, Reader in_, Writer out_)
			throws Exception {
		fromMain = false;
//		in = new BufferedReader(in_);
		out = new PrintWriter(out_, true);

		service(args);
	}

	private Image createImage(float[][] mat, int[] pix) {
		int index = 0;
		for (int y = 0; y < header.n; y++) {
			for (int x = 0; x < header.n; x++) {
				int v = ((int) mat[y][x]) & 0xff;
				int rgb = new Color(v, v, v).getRGB();
				pix[index++] = rgb;
			}
		}
		return createImage(new MemoryImageSource(header.n, header.n, pix, 0,
				header.n));
	}

	private IMatrixObserver matrixObserver = new IMatrixObserver() {
		public void takeHeader(SAQHeader h) {
			header = h;
	
			dmat = new float[header.n][header.n];
			wmat = new float[header.n][header.n];
			pix = new int[header.n * header.n];
			wpix = new int[header.n * header.n];
	
			Filter filter = FilterManager.createFilterFromCode(h.filterCode);
			if (filter == null)
				throw new RuntimeException("Filter code " + h.filterCode
						+ " unknown");
	
			wt = new Transformer(filter);
	
			doLayout();
		}
		
		
		public void takeMatrix(float[][] mat, int numBytesRead) {
			if (header == null)
				throw new RuntimeException("takeHeader not invoked yet!");
	
			for (int y = 0; y < header.n; y++) {
				for (int x = 0; x < header.n; x++) {
					dmat[y][x] = mat[y][x];
					wmat[y][x] = mat[y][x];
				}
			}
	
			SAQUtil.stretch(wmat, wmat.length);
			wimg = createImage(wmat, wpix);
			
			wt.inverse(dmat);
			img = createImage(dmat, pix);
	
			ProgressiveViewer.this.numBytesRead = numBytesRead;
			repaint();
	
			int size = header.n * header.n;
			double bpp = 8. * numBytesRead / size;
			int perc = (int) (100. * numBytesRead / size);
			label.setText("Bytes read = " + numBytesRead + ", " + bpp + " bpp, "
					+ perc + "%");
	
			try {
				Thread.sleep(delay);
			} 
			catch (Exception e) {
			}
		}
	};
	
	public void update(Graphics g) {
		if ( header != null ) {
			if ( img != null ) {
				g.drawImage(img, 0, 0, expand*header.n, expand*header.n, null);
			}
			if ( wimg != null ) {
				g.drawImage(wimg, expand*header.n + 1, 0, expand*header.n, expand*header.n, null);
			}
			double width = (double) numBytesRead / header.n;
			g.setColor(Color.green);
			g.fillRect(0, expand*header.n + 1, (int) width, 10);
		}
	}
}
