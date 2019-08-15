package data.content;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import javax.annotation.Nonnull;

import common.ByteBufferWrapper;

public class MonocromeSymbols extends DAXImageContent {

	public MonocromeSymbols(@Nonnull ByteBufferWrapper data, @Nonnull DAXContentType type) {
		IndexColorModel cm = DAXPalette.toPaletteWithFG(null, DAXPalette.COLOR_WHITE);
		for (int i = 0; i < data.capacity() / 8; i++) {
			DataBufferByte db = new DataBufferByte(data.array(), 8, i * 8);
			WritableRaster r = Raster.createPackedRaster(db, 8, 8, 1, null);
			images.add(new BufferedImage(cm, r, false, null));
		}
	}
}
