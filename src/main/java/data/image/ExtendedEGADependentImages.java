package data.image;

import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;

import common.ByteBufferWrapper;
import data.ContentType;

public class ExtendedEGADependentImages extends EGADependentImages implements VGABitPackedExtension {

	public ExtendedEGADependentImages(ByteBufferWrapper data, ContentType type) {
		int imageCount = data.getUnsigned();

		BufferedImage baseImage = null;
		for (int i = 0; i < imageCount; i++) {
			EGAHeader header = parseHeader(data, type);

			int dataSizeEGA = 1 + imageCount * (21 + (header.width << 2) * header.height);
			boolean isEGA = data.limit() == dataSizeEGA;

			IndexColorModel cm = isEGA ? parseCLUT(data, header) : parseExtendedCLUT(data, header);

			images = images
				.append(isEGA ? parseData(data, header, cm) : parseExtendedData(data, header, cm, baseImage));

			baseImage = images.get(i);
		}
	}
}
