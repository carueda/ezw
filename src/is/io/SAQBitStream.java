package is.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;

/**
 * A stream of symbols in bit format.
 * 
 * @author Carlos Rueda
 * @version 0.1 March/18/1999
 */
public class SAQBitStream extends SAQByteStream {
	/** Word length in bits. */
	private static final int wordLength = 8;

	/** bit repository. */
	private int word;

	/** Number of bits cummulated. */
	private int cum;

	/** Number of symbols handled. */
	private int noSymbols;

	/** Number of bits to represent the symbols handled. */
	private int noBits;

	/**
	 * General constructor to act both as a ISAQInputStream and ISAQOutputStream.
	 */
	public SAQBitStream(DataInputStream in, DataOutputStream out) {
		super(in, out);

		word = 0;
		cum = 0;

		noSymbols = -1; // no beginPass called yet.
	}

	public void beginPass(int n) {
		noSymbols = n;
		if (noSymbols == 2) {
			noBits = 1;
		}
		else if (noSymbols == 3 || noSymbols == 4) {
			noBits = 2;
		}
		else
			throw new IllegalArgumentException("Unexpected number of symbols: "
					+ n);
	}

	public void flush() {
		if (cum > 0) {
			try {
				super.output(word);
			}
			catch (SAQLimitReachedException e) { // ignore this last byte
			}
			word = 0;
			cum = 0;
		}
		super.flush();
	}

	public int input() throws EOFException, SAQLimitReachedException {
		if (noSymbols == -1) {
			return super.input();
		}

		int r = 0;

		if (cum - noBits < 0) {
			word = super.input();
			cum = wordLength;
		}

		if (noBits == 1)
			r = 1 & word;
		else
			r = 3 & word;

		word >>>= noBits;
		cum -= noBits;

		return r;
	}

	public void output(int symbol) throws SAQLimitReachedException {
		if (noSymbols == -1) {
			super.output(symbol);
			return;
		}

		if (cum + noBits > wordLength) {
			super.output(word);
			word = 0;
			cum = 0;
		}

		word |= (symbol << cum);
		cum += noBits;

	}
}
