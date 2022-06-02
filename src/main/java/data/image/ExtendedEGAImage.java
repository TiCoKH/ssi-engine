package data.image;

import java.awt.image.IndexColorModel;

import common.ByteBufferWrapper;
import data.ContentType;

public class ExtendedEGAImage extends EGAImage implements VGABitPackedExtension {

	public ExtendedEGAImage(ByteBufferWrapper data, ContentType type) {
		EGAHeader header = parseHeader(data, type);

		int dataSizeEGA = BASE_IMAGE_START + header.imageCount * (header.width << 2) * header.height;
		boolean isEGA = data.limit() == dataSizeEGA;

		IndexColorModel cm = isEGA ? parseCLUT(data, header) : parseExtendedCLUT(data, header);

		for (int i = 0; i < header.imageCount; i++) {
			images = images.append(isEGA ? parseData(data, header, cm) : parseExtendedData(data, header, cm, null));
		}
	}
}
