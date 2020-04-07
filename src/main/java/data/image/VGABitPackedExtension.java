package data.image;

import static data.image.ImageContentProperties.DELAY;
import static data.image.ImageContentProperties.X_OFFSET;
import static data.image.ImageContentProperties.Y_OFFSET;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.util.Hashtable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import common.ByteBufferWrapper;
import data.palette.Palette;

public interface VGABitPackedExtension {

	default IndexColorModel parseExtendedCLUT(@Nonnull ByteBufferWrapper data, @Nonnull EGAHeader header) {
		int imageFlags = data.getUnsigned();

		byte[] b8 = new byte[8];
		if ((imageFlags & 0x1) > 0) {
			data.get(b8);
		}

		byte[] b16 = new byte[16];
		if ((imageFlags & 0x2) > 0) {
			data.get(b16);
		}

		byte[] b48 = new byte[48];
		if ((imageFlags & 0x8) > 0) {
			byte[] b24 = new byte[24];
			data.get(b24);

			for (int i = 0; i < b24.length; i++) {
				b48[i + 00] = (byte) ((b24[i] & 0x0F) << 2);
				b48[i + 24] = (byte) ((b24[i] & 0xF0) >> 2);
			}
		}

		return Palette.createColorModel(b48, header.type);
	}

	default BufferedImage parseExtendedData(@Nonnull ByteBufferWrapper data, @Nonnull EGAHeader header, @Nonnull IndexColorModel cm,
		@Nullable BufferedImage baseImage) {

		int stride = header.width << 3;
		int imageDataSize = stride * header.height;

		ByteBufferWrapper imageData = uncompress(data, header.height, header.width);

		if (baseImage != null) {
			DataBuffer baseImageData = baseImage.getData().getDataBuffer();
			for (int j = 0; j < imageData.limit(); j++) {
				imageData.put(j, (byte) (baseImageData.getElem(j) ^ imageData.getUnsigned(j)));
			}
		}
		DataBufferByte db = new DataBufferByte(imageData.array(), imageDataSize);
		WritableRaster r = WritableRaster.createInterleavedRaster(db, stride, header.height, stride, 1, new int[] { 0 }, null);

		Hashtable<String, Integer> props = new Hashtable<>();
		props.put(X_OFFSET.name(), 8 * header.xStart);
		props.put(Y_OFFSET.name(), 8 * header.yStart);
		if (header.delay != Integer.MIN_VALUE)
			props.put(DELAY.name(), header.delay);

		return new BufferedImage(baseImage != null ? baseImage.getColorModel() : cm, r, false, props);
	}

	default ByteBufferWrapper uncompress(@Nonnull ByteBufferWrapper compressed, int height, int width) {
		ByteBufferWrapper result = ByteBufferWrapper.allocateLE(height * 8 * width);

		byte[] b5w = new byte[5 * width];

		for (int y = 0; y < height; y++) {
			compressed.get(b5w);
			for (int x = 0; x < width; x++) {
				for (int i = 0; i < 8; i++) {
					int mask = 0x80 >> i;

					byte colorIndex = 0;
					if ((b5w[x + 0 * width] & mask) > 0)
						colorIndex |= 0x01;
					if ((b5w[x + 1 * width] & mask) > 0)
						colorIndex |= 0x02;
					if ((b5w[x + 2 * width] & mask) > 0)
						colorIndex |= 0x04;
					if ((b5w[x + 3 * width] & mask) > 0)
						colorIndex |= 0x08;
					if ((b5w[x + 4 * width] & mask) > 0)
						colorIndex |= 0x10;
					result.put(colorIndex);
				}
			}
		}
		return result;
	}
}
