package data.content;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

public class VGADependentImages extends DAXImageContent {

	public VGADependentImages(ByteBuffer data) {
		// 11 byte Header
		int height = data.getShort(0) & 0xFFFF;
		int width = 8 * (data.getShort(2) & 0xFFFF);
		int xStart = data.getShort(4) & 0xFFFF;
		int yStart = data.getShort(6) & 0xFFFF;
		int imageCount = 1 + data.get(8) & 0xFF;
		int colorBase = data.get(9) & 0xFF;
		int colorCount = data.get(10) & 0xFF;

		int imageSize = width * height;

		Color[] color = DAXPalette.createGamePalette(data, 11, colorCount, colorBase);

		byte[] egaColorMapping = new byte[colorCount >> 1];
		data.position(11 + 3 * colorCount);
		data.get(egaColorMapping);

		data.position(data.position() + 4); // unkown
		int baseImage = data.get() & 0xFF;
		data.position(data.position() + 1); // unkown
		data.position(data.position() + 3 * imageCount); // image packed sizes + 1 byte value

		ByteBuffer imageData = uncompress(data.slice().order(LITTLE_ENDIAN), imageCount * imageSize);

		for (int i = 0; i < imageCount; i++) {
			BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					int c = imageData.get((imageSize * i) + (y * width) + x) & 0xFF;
					if (i == baseImage) {
						image.setRGB(x, y, color[c].getRGB());
					} else {
						int cBase = imageData.get((imageSize * baseImage) + (y * width) + x) & 0xFF;
						image.setRGB(x, y, color[c ^ cBase].getRGB());
					}
				}
			}
			images.add(image);
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

	public static ByteBuffer uncompress(ByteBuffer compressed, int sizeRaw) {
		ByteBuffer result = ByteBuffer.allocate(sizeRaw).order(LITTLE_ENDIAN);

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
