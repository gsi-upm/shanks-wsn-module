/**
 * Copyright 2014 Álvaro Carrera Barroso
 * Grupo de Sistemas Inteligentes - Universidad Politécnica de Madrid
 *  
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package es.upm.dit.gsi.shanks.wsn.utils;

import java.awt.Color;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

/**
 * Project: shanks-han-module File:
 * es.upm.dit.gsi.shanks.han.model.element.device
 * .portrayal.ImageLoaderHelper.java
 * 
 * Grupo de Sistemas Inteligentes Departamento de Ingeniería de Sistemas
 * Telemáticos Universidad Politécnica de Madrid (UPM)
 * 
 * @author Álvaro Carrera Barroso
 * @email a.carrera@gsi.dit.upm.es
 * @twitter @alvarocarrera
 * @date 26/06/2014
 * @version 0.1
 * 
 */
public class ImageLoaderHelper {

	/**
	 * 
	 */
	private static ImageObserver io = null;
	/**
     * 
     */
	private static ImageReader reader = null;

	/**
	 * @return
	 */
	public static ImageObserver getImageObserver() {
		if (io == null) {
			io = new ImageObserver() {

				@Override
				public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
					return false;
				}
			};
		}

		return io;
	}

	/**
	 * @param filePath
	 * @return
	 * @throws IOException
	 */
	public static Image getImage(String filePath) throws IOException {
		if (filePath.endsWith("jpg") || filePath.endsWith("jpeg")) {
			return ImageLoaderHelper.getImage(filePath, "JPEG");
		} else if (filePath.endsWith("png")) {
			Image image = ImageIO.read(new File(filePath));
			return image;
		} else {
			return null;
		}
	}

	/**
	 * @param filePath
	 * @return
	 * @throws IOException
	 */
	public static Image getImage(String filePath, String format) throws IOException {
		File file = new File(filePath);

		if (reader == null) {
			// Find a suitable ImageReader
			Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName(format);
			while (readers.hasNext()) {
				reader = (ImageReader) readers.next();
				if (reader.canReadRaster()) {
					break;
				}
			}
		}

		// Stream the image file (the original CMYK image)
		ImageInputStream input = ImageIO.createImageInputStream(file);
		reader.setInput(input);
		// Read the image raster
		Raster raster = reader.readRaster(0, null);

		// Create a new RGB image
		Image bi = new BufferedImage(raster.getWidth(), raster.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);

		// Fill the new image with the old raster
		((BufferedImage) bi).getRaster().setRect(raster);

		return bi;
	}

	/**
	 * Make provided image transparent wherever color matches the provided
	 * color.
	 * 
	 * @param im
	 *            BufferedImage whose color will be made transparent.
	 * @param color
	 *            Color in provided image which will be made transparent.
	 * @return Image with transparency applied.
	 */
	public static Image makeColorTransparent(final BufferedImage im, final Color color) {
		final ImageFilter filter = new RGBImageFilter() {
			// the color we are looking for (white)... Alpha bits are set to
			// opaque
			public int markerRGB = color.getRGB() | 0xFFFFFFFF;

			public final int filterRGB(final int x, final int y, final int rgb) {
				if ((rgb | 0xFF000000) == markerRGB) {
					// Mark the alpha bits as zero - transparent
					return 0x00FFFFFF & rgb;
				} else {
					// nothing to do
					return rgb;
				}
			}
		};

		final ImageProducer ip = new FilteredImageSource(im.getSource(), filter);
		return Toolkit.getDefaultToolkit().createImage(ip);
	}

}
