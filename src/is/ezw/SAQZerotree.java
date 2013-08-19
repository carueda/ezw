package is.ezw;

/**
 * zerotree operations
 * @author Carlos Rueda 
 */
public final class SAQZerotree {
	private boolean[][] ztr;

	/**
	 * Constructor with default values.
	 */
	public SAQZerotree(int n) {
		ztr = new boolean[n][n];
	}

	public final void add(int x, int y) {
		ztr[y][x] = true;
	}

	public final boolean descends(int x, int y) {
		do {
			x >>>= 1;
			y >>>= 1;

			if (x == 0 && y == 0)
				break;

			if (is(x, y))
				return true;

		} while (x > 0 && y > 0);

		return false;
	}

	public final boolean is(int x, int y) {
		return ztr[y][x];
	}

	public final void reset() {
		for (int i = 0; i < ztr.length; i++)
			for (int j = 0; j < ztr.length; j++)
				ztr[i][j] = false;
	}
}
