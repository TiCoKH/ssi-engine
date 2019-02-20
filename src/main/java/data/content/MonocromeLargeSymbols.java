package data.content;

import static java.awt.image.BufferedImage.TYPE_BYTE_BINARY;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;

import common.ByteBufferWrapper;

public class MonocromeLargeSymbols extends DAXImageContent {

	public MonocromeLargeSymbols(ByteBufferWrapper data, DAXContentType type) {
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

			WritableRaster r = Raster.createPackedRaster(new DataBufferByte(glyph.array(), 8), 8, 8, 1, null);
			BufferedImage image = new BufferedImage(8, 8, TYPE_BYTE_BINARY);
			image.setData(r);
			images.add(image);
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
