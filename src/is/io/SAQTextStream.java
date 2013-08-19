package is.io;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.PrintWriter;
import java.util.StringTokenizer;

/**
 * A stream of symbols in textual format for easy visual inspection.
 * 
 * @author Carlos Rueda
 * @version 0.1 March/18/1999
 */
public class SAQTextStream implements ISAQStream {
	/** Used to read lines when acting as a ISAQInputStream. */
	BufferedReader in;

	/** Used to print lines when acting as a ISAQOutputStream. */
	PrintWriter out;

	/**
	 * The maximum number of bytes that can be written or read. Here this is
	 * simple the amount of data.
	 */
	int maxBytes;

	/** Counter of bytes written or read. */
	int counterBytes;

	/**
	 * General constructor to act both as a ISAQInputStream and
	 * ISAQOutputStream.
	 */
	public SAQTextStream(BufferedReader in, PrintWriter out) {
		this.in = in;
		this.out = out;
	}

	public void beginPass(int n) {
		// ignored by now
	}

	private String firstToken(String line) {
		StringTokenizer st = new StringTokenizer(line);
		return st.nextToken();
	}

	public void flush() {
		out.flush();
	}

	public int getNumBytesRead() {
		return counterBytes;
	}

	public int input() throws EOFException, SAQLimitReachedException {
		if (maxBytes > 0 && counterBytes >= maxBytes)
			throw new SAQLimitReachedException();
		counterBytes++;

		int s = -1;
		try {
			String line = in.readLine();
			if (line == null)
				throw new EOFException();

			s = Integer.parseInt(line);
		}
		catch (EOFException e) {
			throw e;
		}
		catch (Exception e) {
			System.err.println(e);
		}

		return s;

	}

	public void output(int symbol) throws SAQLimitReachedException {
		if (maxBytes > 0 && counterBytes >= maxBytes)
			throw new SAQLimitReachedException();

		counterBytes++;
		out.println(symbol);
	}

	public SAQHeader readHeader() {
		SAQHeader h = new SAQHeader();
		try {
			h.filterCode = (short) Integer.parseInt(firstToken(in.readLine()));
			h.noScales = (byte) Integer.parseInt(firstToken(in.readLine()));
			h.n = (short) Integer.parseInt(firstToken(in.readLine()));
			h.mean = new Float(firstToken(in.readLine())).floatValue();
			h.T0 = new Float(firstToken(in.readLine())).floatValue();
		}
		catch (Exception e) {
			System.err.println(e);
		}

		return h;
	}

	public void setInputLimit(int maxBytes) {
		this.maxBytes = maxBytes;
	}

	public void setOutputLimit(int maxBytes) {
		this.maxBytes = maxBytes;
	}

	public void writeHeader(SAQHeader h) {
		out.println(h.filterCode + "\t// wavelet filter code");
		out.println(h.noScales + "\t// number of wavelet scales");
		out.println(h.n + "\t// dimension of the square matrix");
		out.println(h.mean + "\t// image mean");
		out.println(h.T0 + "\t// initial threshold");

		// very roughly (but doesn't matter):
		counterBytes = 2 + 1 + 2 + 4 + 4;
	}
}
