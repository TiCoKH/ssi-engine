package data.content;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

public class VGAImage extends DAXImageContent {

	public VGAImage(ByteBuffer data) {
		// 10 byte Header
		int height = data.get(0) & 0xFF;
		int width = 8 * (data.get(1) & 0xFF);
		// data[2] & 0xFF;
		// data[3] & 0xFF;
		// data[4] & 0xFF;
		// data[5] & 0xFF;
		int imageCount = data.getShort(6) & 0xFFFF;
		int colorBase = data.get(8) & 0xFF;
		int colorCount = 1 + (data.get(9) & 0xFF);

		int imageSize = width * height;
		int imageOffset = data.capacity() - (imageCount * imageSize);

		Color[] color = DAXPalette.createGamePalette(data, 10, colorCount, colorBase);

		for (int i = 0; i < imageCount; i++, imageOffset += imageSize) {
			BufferedImage image= new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					int c = data.get(imageOffset + (y * width) + x) & 0xFF;
					image.setRGB(x, y, color[c].getRGB());
				}
			}
			images.add(image);
		}
	}
}
