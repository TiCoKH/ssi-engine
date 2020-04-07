package data.image;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;

import common.ByteBufferWrapper;
import data.ContentType;
import data.palette.Palette;

public class MonocromeLargeSymbols extends ImageContent {

	public MonocromeLargeSymbols(ByteBufferWrapper data, ContentType type) {
		IndexColorModel cm = Palette.toPaletteWithFG(null, Palette.COLOR_WHITE);
		for (int i = 0; i < data.capacity() / 12; i++) {
			// font is 8x12, shown as 8x8; some rows are skipped
			ByteBufferWrapper glyph = ByteBufferWrapper.allocateLE(8);
			glyph.put(data.get());
			glyph.put(data.get());
			data.get();
			glyph.put(data.get());

			glyph.put(data.get());
			data.get();
			glyph.put(data.get());
			data.get();

			glyph.put(data.get());
			glyph.put(data.get());
			data.get();
			data.get();

			DataBufferByte db = new DataBufferByte(glyph.array(), 8);
			WritableRaster r = Raster.createPackedRaster(db, 8, 8, 1, null);
			images.add(new BufferedImage(cm, r, false, null));
		}

		// game font uses ascii ordering, not the one ECL strings needs
		List<BufferedImage> backup = new ArrayList<>(images);
		for (int i = 0; i < 32; i++) {
			images.set(i, backup.get(i + 32));
		}
		for (int i = 32; i < 64; i++) {
			images.set(i, backup.get(i - 32));
		}
	}
}
