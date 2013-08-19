package is.image;

import java.awt.Color;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Panel;
import java.awt.Toolkit;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;

/**
 * Image in standard format.
 * 
 * @author Carlos Rueda
 * @version 0.1 Dec/12/1999
 */
public class StandardImage implements IMatrixImage, ImageObserver {
	Image img;

	int w;

	int h;

	int[] pix;

	/** The image. */
	int[][] image;

	/** Number of values successfully read. */
	int numRead;

	/**
	 */
	public StandardImage(String name) throws Exception {
		img = Toolkit.getDefaultToolkit().getImage(name);
		if (img == null)
			return;

		// wait for the image to be entirely loaded
		MediaTracker tracker = new MediaTracker(new Panel());
		tracker.addImage(img, 0);
		try {
			tracker.waitForID(0);
		}
		catch (InterruptedException e) {
			;
		}
		if (tracker.statusID(0, true) != MediaTracker.COMPLETE) {
			throw new Exception("Could not load: " + name + " "
					+ tracker.statusID(0, true));
		}

		w = img.getWidth(this);
		h = img.getHeight(this);
		if (w >= 0 && h >= 0)
			grabImage();
	}

	public int getNumValuesRead() {
		return numRead;
	}

	public int[] getPixelArray() {
		return pix;
	}

	public int[][] getValues() {
		return image;
	}

	void grabImage() {
		if (w < 0 || h < 0)
			return;

		pix = new int[w * h];
		PixelGrabber pg = new PixelGrabber(img, 0, 0, w, h, pix, 0, w);
		try {
			if (!pg.grabPixels()) {
				System.out.println("! pg.grabPixels()");
				return;
			}
		}
		catch (InterruptedException ex) {
			return;
		}

		image = new int[w][h];

		int k = 0;
		// convert to gray level
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++, k++) {
				int c = new Color(pix[k]).getGreen();
				image[x][y] = c;
			}
		}
	}

	public boolean imageUpdate(Image img, int infoflags, int x, int y,
			int width, int height) {
		if ((infoflags & WIDTH) == WIDTH) {
			w = width;
		}
		if ((infoflags & HEIGHT) == HEIGHT) {
			h = height;
		}
		if ((infoflags & ALLBITS) == ALLBITS) {
			grabImage();
			return false;
		}

		return true;
	}
}
