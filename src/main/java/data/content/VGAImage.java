package data.content;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;

import common.ByteBufferWrapper;

public class VGAImage extends DAXImageContent {

	public VGAImage(ByteBufferWrapper data) {
		// 10 byte Header
		int height = data.getUnsigned(0);
		int width = 8 * data.getUnsigned(1);
		// data[2] & 0xFF;
		// data[3] & 0xFF;
		// data[4] & 0xFF;
		// data[5] & 0xFF;
		int imageCount = data.getUnsignedShort(6);
		int colorBase = data.getUnsigned(8);
		int colorCount = 1 + data.getUnsigned(9);

		int imageSize = width * height;
		int imageOffset = data.capacity() - (imageCount * imageSize);

		IndexColorModel cm = DAXPalette.createGameColorModel(data, 10, colorCount, colorBase);

		data.position(imageOffset);
		for (int i = 0; i < imageCount; i++, imageOffset += imageSize) {
			byte[] imageData = new byte[imageSize];
			data.get(imageData);

			DataBufferByte db = new DataBufferByte(imageData, imageSize);
			WritableRaster r = WritableRaster.createInterleavedRaster(db, width, height, width, 1, new int[] { 0 }, null);

			images.add(new BufferedImage(cm, r, false, null));
		}
	}
}
