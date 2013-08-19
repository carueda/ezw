package is.ezw;

import java.awt.Point;

/**
 * A list of elements with a maximum capacity.
 */
public final class SAQList {
	private Object[] list;

	private int size;

	/**
	 */
	public SAQList(int capacity) {
		list = new Object[capacity];
		size = 0;
	}

	/**
	 */
	public void addElement(Object element) {
		list[size++] = element;
	}

	/**
	 */
	public Object elementAt(int p) {
		if (p < size)
			return list[p];

		throw new IndexOutOfBoundsException("p=" + p);
	}

	/**
	 */
	public void setSize(int s) {
		size = s;
	}

	/**
	 */
	public int size() {
		return size;
	}

	public void initDominantList(int x, int y, int n) {
		if ( n <= 1 ) {
			addElement(new Point(x, y));
		}
		else {
			n >>>= 1;
			initDominantList(x, y, n);		
			initDominantList(x + n, y, n);		
			initDominantList(x, y + n, n);		
			initDominantList(x + n, y + n, n);		
		}
	}
}
