package data.content;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;

import javax.annotation.Nonnull;

import common.ByteBufferWrapper;

public class VGADependentImages extends DAXImageContent {

	public VGADependentImages(@Nonnull ByteBufferWrapper data, @Nonnull DAXContentType type) {
		// 11 byte Header
		int height = data.getUnsignedShort(0);
		int width = 8 * data.getUnsignedShort(2);
		int xStart = data.getUnsignedShort(4);
		int yStart = data.getUnsignedShort(6);
		int imageCount = 1 + data.getUnsigned(8);
		int colorBase = data.getUnsigned(9);
		int colorCount = data.getUnsigned(10);

		int imageSize = width * height;

		IndexColorModel cm = DAXPalette.createColorModel(data, 11, colorCount, colorBase, type);

		byte[] egaColorMapping = new byte[colorCount >> 1];
		data.position(11 + 3 * colorCount);
		data.get(egaColorMapping);

		data.position(data.position() + 4); // unkown
		int baseImage = data.getUnsigned();
		data.position(data.position() + 1); // unkown
		data.position(data.position() + 3 * imageCount); // image packed sizes + 1 byte value

		ByteBufferWrapper allImageData = uncompress(data.slice(), imageCount * imageSize);

		// Read Base image first
		byte[] baseImageData = new byte[imageSize];
		allImageData.position(baseImage * imageSize);
		allImageData.get(baseImageData);

		allImageData.rewind();
		for (int i = 0; i < imageCount; i++) {
			byte[] imageData = new byte[imageSize];
			allImageData.get(imageData);

			if (i != baseImage) {
				for (int j = 0; j < imageData.length; j++) {
					imageData[j] = (byte) (imageData[j] ^ baseImageData[j]);
				}
			}

			DataBufferByte db = new DataBufferByte(imageData, imageSize);
			WritableRaster r = WritableRaster.createInterleavedRaster(db, width, height, width, 1, new int[] { 0 }, null);

			images.add(new BufferedImage(cm, r, false, null));
		}
	}

	private int mapToEGAColor(byte[] egaColorMapping, int colorBase, int colorCount, int vgaIndex) {
		if (vgaIndex < colorBase || vgaIndex > colorBase + colorCount) {
			return vgaIndex & 0x0F;
		}
		int mappingIndex = (vgaIndex - colorBase) >> 1;
		boolean lowBits = ((vgaIndex - colorBase) & 0x1) > 0;
		return lowBits ? (egaColorMapping[mappingIndex] & 0x0F) : (egaColorMapping[mappingIndex] & 0xF0) >> 4;
	}

	public static ByteBufferWrapper uncompress(ByteBufferWrapper compressed, int sizeRaw) {
		ByteBufferWrapper result = ByteBufferWrapper.allocateLE(sizeRaw);

		int in = 0;
		int out = 0;
		compressed.rewind();
		while (in < compressed.limit()) {
			byte next = compressed.get(in++);
			int count = Math.abs(next);
			if (next >= 0) {
				for (int i = 0; i < count + 1; i++) {
					result.put(out++, compressed.get(in++));
				}
			} else {
				byte repeat = compressed.get(in++);
				for (int i = 0; i < count + 1; i++) {
					result.put(out++, repeat);
				}
			}
		}
		return result;
	}
}
