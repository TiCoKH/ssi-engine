package data.content;

import static data.content.ImageContentProperties.X_OFFSET;
import static data.content.ImageContentProperties.Y_OFFSET;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.util.Hashtable;

import javax.annotation.Nonnull;

import common.ByteBufferWrapper;

public class VGAImage extends DAXImageContent {

	public VGAImage(@Nonnull ByteBufferWrapper data, @Nonnull DAXContentType type) {
		// 10 byte Header
		int height = data.getUnsigned(0);
		int width = 8 * data.getUnsigned(1);
		int xStart = data.getUnsignedShort(2);
		int yStart = data.getUnsignedShort(4);
		int imageCount = data.getUnsignedShort(6);
		int colorBase = data.getUnsigned(8);
		int colorCount = 1 + data.getUnsigned(9);

		int imageSize = width * height;
		int imageOffset = data.capacity() - (imageCount * imageSize);

		Hashtable<String, Integer> props = new Hashtable<>();
		props.put(X_OFFSET.name(), xStart);
		props.put(Y_OFFSET.name(), yStart);

		IndexColorModel cm = DAXPalette.createColorModel(data, 10, colorCount, colorBase, type);

		data.position(imageOffset);
		for (int i = 0; i < imageCount; i++, imageOffset += imageSize) {
			byte[] imageData = new byte[imageSize];
			data.get(imageData);

			DataBufferByte db = new DataBufferByte(imageData, imageSize);
			WritableRaster r = WritableRaster.createInterleavedRaster(db, width, height, width, 1, new int[] { 0 }, null);

			images.add(new BufferedImage(cm, r, false, props));
		}
	}
}
