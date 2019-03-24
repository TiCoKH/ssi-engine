package data.content;

import static java.awt.image.BufferedImage.TYPE_BYTE_BINARY;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import javax.annotation.Nonnull;

import common.ByteBufferWrapper;

public class MonocromeSymbols extends DAXImageContent {

	public MonocromeSymbols(@Nonnull ByteBufferWrapper data, @Nonnull DAXContentType type) {
		for (int i = 0; i < data.capacity() / 8; i++) {
			WritableRaster r = Raster.createPackedRaster(new DataBufferByte(data.array(), 8, i * 8), 8, 8, 1, null);
			BufferedImage image = new BufferedImage(8, 8, TYPE_BYTE_BINARY);
			image.setData(r);
			images.add(image);
		}
	}
}
