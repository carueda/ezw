package is.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;

/**
 * A stream of symbols in byte format.
 * 
 * @author Carlos Rueda
 * @version 0.1 March/18/1999
 */
public class SAQByteStream implements ISAQStream {
	/** Used to read bytes when acting as a ISAQInputStream. */
	DataInputStream in;

	/** Used to write bytes when acting as a ISAQOutputStream. */
	DataOutputStream out;

	/** The maximum number of bytes that can be written or read. */
	int maxBytes;

	/** Counter of bytes written or read. */
	int counterBytes;

	/**
	 * General constructor to act both as a ISAQInputStream and ISAQOutputStream.
	 */
	public SAQByteStream(DataInputStream in, DataOutputStream out) {
		this.in = in;
		this.out = out;
	}

	public void beginPass(int n) {
		// ignored for now
	}

	public void flush() {
		try {
			out.flush();
		}
		catch (Exception e) {
			System.err.println(e);
		}
	}

	public int getNumBytesRead() {
		return counterBytes;
	}

	public int input() throws EOFException, SAQLimitReachedException {
		if (maxBytes > 0 && counterBytes >= maxBytes)
			throw new SAQLimitReachedException();

		int s = -1;
		try {
			s = in.readUnsignedByte();
			counterBytes++;
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
		try {
			out.writeByte(symbol);
			counterBytes++;
		}
		catch (Exception e) {
			System.err.println(e);
		}
	}

	public SAQHeader readHeader() {
		SAQHeader h = new SAQHeader();
		try {
			h.filterCode = (short) in.readUnsignedShort();
			h.noScales = (byte) in.readUnsignedByte();
			h.n = (short) in.readUnsignedShort();
			h.mean = in.readFloat();
			h.T0 = in.readFloat();
			counterBytes = 2 + 1 + 2 + 4 + 4;
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
		try {
			out.writeShort(h.filterCode);
			out.writeByte(h.noScales);
			out.writeShort(h.n);
			out.writeFloat(h.mean);
			out.writeFloat(h.T0);
			counterBytes = 2 + 1 + 2 + 4 + 4;
		}
		catch (Exception e) {
			System.err.println(e);
		}
	}
}
