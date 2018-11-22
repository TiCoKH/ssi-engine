package data.content;

import static data.content.ImageContentProperties.DELAY;
import static data.content.ImageContentProperties.X_OFFSET;
import static data.content.ImageContentProperties.Y_OFFSET;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.Hashtable;

import javax.annotation.Nonnull;

import common.ByteBufferWrapper;

public class EGADependentImages extends DAXImageContent {
	protected static final DAXContentType SAME_SIZE_IMAGES = DAXContentType.PIC;
	protected static final int BASE_IMAGE_START = 22;

	protected EGADependentImages() {
		// Dummy constructor
	}

	public EGADependentImages(@Nonnull ByteBufferWrapper data, @Nonnull DAXContentType type) {
		int imageCount = data.getUnsigned();

		for (int i = 0; i < imageCount; i++) {
			EGAHeader header = parseHeader(data, type);

			IndexColorModel cm = parseCLUT(data, header);

			images.add(parseData(data, header, cm));
		}
	}

	protected EGAHeader parseHeader(@Nonnull ByteBufferWrapper data, @Nonnull DAXContentType type) {
		EGAHeader result = new EGAHeader();
		result.type = type;
		result.delay = (int) data.getUnsignedInt();
		result.height = data.getUnsignedShort();
		result.width = data.getUnsignedShort();
		result.xStart = data.getUnsignedShort();
		result.yStart = data.getUnsignedShort();
		result.imageCount = data.getUnsigned();
		return result;
	}

	protected IndexColorModel parseCLUT(@Nonnull ByteBufferWrapper data, @Nonnull EGAHeader header) {
		byte[] cgaColorMapping = new byte[8];
		data.get(cgaColorMapping);

		return DAXPalette.createColorModel(header.type);
	}

	protected BufferedImage parseData(@Nonnull ByteBufferWrapper data, @Nonnull EGAHeader header, @Nonnull IndexColorModel cm) {
		int stride = header.width << 2;
		int imageDataSize = stride * header.height;

		Hashtable<String, Integer> props = new Hashtable<>();
		props.put(DELAY.name(), header.delay);
		props.put(X_OFFSET.name(), 8 * header.xStart);
		props.put(Y_OFFSET.name(), 8 * header.yStart);

		DataBufferByte db;
		if (data.position() == BASE_IMAGE_START || header.type != SAME_SIZE_IMAGES) {
			db = new DataBufferByte(data.array(), imageDataSize, data.position());
		} else {
			byte[] imageData = new byte[imageDataSize];
			int imageDataStart = data.position();
			for (int j = 0; j < imageData.length; j++) {
				imageData[j] = (byte) (data.getUnsigned(BASE_IMAGE_START + j) ^ data.getUnsigned(imageDataStart + j));
			}
			db = new DataBufferByte(imageData, imageDataSize, 0);
		}
		data.position(data.position() + imageDataSize);

		WritableRaster r = Raster.createPackedRaster(db, header.width << 3, header.height, 4, null);
		return new BufferedImage(cm, r, false, props);
	}
}
