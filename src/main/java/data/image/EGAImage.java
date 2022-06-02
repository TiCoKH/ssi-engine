package data.image;

import static data.image.ImageContentProperties.X_OFFSET;
import static data.image.ImageContentProperties.Y_OFFSET;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.Hashtable;

import javax.annotation.Nonnull;

import common.ByteBufferWrapper;
import data.ContentType;
import data.palette.Palette;

public class EGAImage extends ImageContent {
	protected static final int BASE_IMAGE_START = 17;

	protected EGAImage() {
		// Dummy constructor
	}

	public EGAImage(@Nonnull ByteBufferWrapper data, @Nonnull ContentType type) {
		EGAHeader header = parseHeader(data, type);

		IndexColorModel cm = parseCLUT(data, header);

		for (int i = 0; i < header.imageCount; i++) {
			images = images.append(parseData(data, header, cm));
		}
	}

	protected EGAHeader parseHeader(@Nonnull ByteBufferWrapper data, @Nonnull ContentType type) {
		EGAHeader result = new EGAHeader();
		result.type = type;
		result.delay = Integer.MIN_VALUE;
		result.height = data.getUnsignedShort();
		result.width = data.getUnsignedShort();
		result.xStart = data.getUnsignedShort();
		result.yStart = data.getUnsignedShort();
		result.imageCount = data.getUnsigned();
		int dkkContentSize = BASE_IMAGE_START + result.height * (result.width << 2) * (result.imageCount + 1);
		if (data.limit() == dkkContentSize) {
			result.imageCount++;
		}
		return result;
	}

	protected IndexColorModel parseCLUT(@Nonnull ByteBufferWrapper data, @Nonnull EGAHeader header) {
		byte[] cgaColorMapping = new byte[8];
		data.get(cgaColorMapping);

		return Palette.createColorModel(header.type);
	}

	protected BufferedImage parseData(@Nonnull ByteBufferWrapper data, @Nonnull EGAHeader header,
		@Nonnull IndexColorModel cm) {
		int stride = header.width << 2;
		int imageDataSize = stride * header.height;

		Hashtable<String, Integer> props = new Hashtable<>();
		props.put(X_OFFSET.name(), 8 * header.xStart);
		props.put(Y_OFFSET.name(), 8 * header.yStart);

		DataBufferByte db = new DataBufferByte(data.array(), imageDataSize, data.position());
		data.position(data.position() + imageDataSize);

		WritableRaster r = Raster.createPackedRaster(db, header.width << 3, header.height, 4, null);
		return new BufferedImage(cm, r, false, props);
	}
}
