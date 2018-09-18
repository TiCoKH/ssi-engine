package data.content;

import static java.awt.image.BufferedImage.TYPE_BYTE_BINARY;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.Collectors;

public class MonocromeSymbols extends DAXImageContent {

	public MonocromeSymbols(ByteBuffer data) {
		for (int i = 0; i < data.capacity() / 8; i++) {
			WritableRaster r = Raster.createPackedRaster(new DataBufferByte(data.array(), 8, i * 8), 8, 8, 1, null);
			BufferedImage image = new BufferedImage(8, 8, TYPE_BYTE_BINARY);
			image.setData(r);
			images.add(image);
		}
	}

	public List<BufferedImage> withGreenFG() {
		return images.stream().map(CONVERTER::withGreenFG).collect(Collectors.toList());
	}

	public List<BufferedImage> withMagentaFG() {
		return images.stream().map(CONVERTER::withMagentaFG).collect(Collectors.toList());
	}

	public List<BufferedImage> withInvertedColors() {
		return images.stream().map(CONVERTER::withInvertedColors).collect(Collectors.toList());
	}
}
