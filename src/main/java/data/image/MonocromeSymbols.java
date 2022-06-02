package data.image;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import javax.annotation.Nonnull;

import common.ByteBufferWrapper;
import data.ContentType;
import data.palette.Palette;

public class MonocromeSymbols extends ImageContent {

	public MonocromeSymbols(@Nonnull ByteBufferWrapper data, @Nonnull ContentType type) {
		IndexColorModel cm = Palette.toPaletteWithFG(null, Palette.COLOR_WHITE);
		for (int i = 0; i < data.capacity() / 8; i++) {
			DataBufferByte db = new DataBufferByte(data.array(), 8, i * 8);
			WritableRaster r = Raster.createPackedRaster(db, 8, 8, 1, null);
			images = images.append(new BufferedImage(cm, r, false, null));
		}
	}
}
