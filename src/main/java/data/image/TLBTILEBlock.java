package data.image;

import static data.HLIBContent.uncompress;
import static data.image.ImageContentProperties.X_OFFSET;
import static data.image.ImageContentProperties.Y_OFFSET;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.util.Hashtable;
import java.util.List;

import javax.annotation.Nonnull;

import common.ByteBufferWrapper;
import data.ContentType;
import data.palette.Palette;

public class TLBTILEBlock extends ImageContent {

	public TLBTILEBlock(@Nonnull List<ByteBufferWrapper> tileBuffers, @Nonnull ContentType type) {
		TILEHeader colorHeader = null;
		ByteBufferWrapper colorData = null;

		for (int i = 0; i < tileBuffers.size(); i++) {
			ByteBufferWrapper data = tileBuffers.get(i).rewind();

			TILEHeader header = TILEHeader.readFrom(data, type);

			ByteBufferWrapper imageData;
			boolean transparent = false;
			switch (header.type) {
				case 0x10:
					imageData = readImageData0x10(header, data.slice());
					break;
				case 0x11:
					imageData = readImageData0x11(header, data.slice());
					transparent = true;
					break;
				case 0x12:
					imageData = readImageData0x12(header, data.slice());
					break;
				case 0x15:
					imageData = readImageData0x15(header, data.slice());
					transparent = true;
					break;
				case 0x17:
					imageData = readImageData0x17(header, data.slice());
					transparent = true;
					break;
				case 0x18:
					colorHeader = header;
					colorData = data.slice();
					continue;
				case 0x19:
					readColorCycling(header, data.slice());
					continue;
				default:
					throw new IllegalArgumentException(String.format("Unknown tile type %2X", header.type));
			}
			int height = header.short1;
			int width = header.byte1 << 2;
			int imageSize = height * width;

			Hashtable<String, Integer> props = new Hashtable<>();
			props.put(X_OFFSET.name(), header.short3);
			props.put(Y_OFFSET.name(), header.short2);

			try {
				IndexColorModel cm = readColorPalette(colorHeader, colorData, transparent);
				DataBufferByte db = new DataBufferByte(imageData.array(), imageSize);
				WritableRaster r = WritableRaster.createInterleavedRaster(db, width, height, width, 1, new int[] { 0 }, null);

				images.add(new BufferedImage(cm, r, false, props));
			} catch (Exception e) {
				System.err.println(String.format("Exception during processing of TILE %d with type %2X", i, header.type));
				e.printStackTrace(System.err);
			}
		}
	}

	// Uncompressed non-transparent image data
	private static ByteBufferWrapper readImageData0x10(TILEHeader header, ByteBufferWrapper imageData) {
		return deswizzle(imageData, imageData.limit());
	}

	// Uncompressed transparent image data
	private static ByteBufferWrapper readImageData0x11(TILEHeader header, ByteBufferWrapper imageData) {
		int height = header.short1;
		int width = header.byte1 << 2;
		int dataSize = height * width;

		ByteBufferWrapper maskData = ByteBufferWrapper.allocateLE(dataSize);
		imageData.get(maskData.array());

		ByteBufferWrapper colorData = ByteBufferWrapper.allocateLE(dataSize);
		imageData.get(colorData.array());

		for (int i = 0; i < maskData.limit(); i++) {
			int mask = maskData.getUnsigned(i);
			if (mask == 0xFF) {
				colorData.put(i, (byte) 0);
			}
		}
		return deswizzle(colorData, dataSize);
	}

	// Compressed non-transparent image data
	private static ByteBufferWrapper readImageData0x12(TILEHeader header, ByteBufferWrapper imageData) {
		int height = header.short1;
		int width = header.byte1 << 2;
		int imageSize = height * width;
		return deswizzle(uncompress(imageData, imageSize), width);
	}

	// Uncompressed transparent image data
	private static ByteBufferWrapper readImageData0x15(TILEHeader header, ByteBufferWrapper imageData) {
		return deswizzle(imageData, imageData.limit());
	}

	// Compressed transparent image data
	private static ByteBufferWrapper readImageData0x17(TILEHeader header, ByteBufferWrapper imageData) {
		int height = header.short1;
		int width = header.byte1 << 2;

		return uncompressAndDeswizzle(imageData, height, width);
	}

	private static IndexColorModel readColorPalette(TILEHeader header, ByteBufferWrapper colorData, boolean transparent) {
		return Palette.createColorModelNoShift(colorData, header.short3, header.short2, transparent, header.contentType);
	}

	private void readColorCycling(TILEHeader header, ByteBufferWrapper colorData) {
		// TODO
	}

	private static ByteBufferWrapper deswizzle(ByteBufferWrapper src, int rowLength) {
		ByteBufferWrapper result = ByteBufferWrapper.allocateLE(src.limit());

		int blocksPerRow = rowLength >> 2;
		for (int i = 0; i < src.limit(); i++) {
			int rowStart = (i / rowLength) * rowLength;
			int blockInRow = (i - rowStart) % blocksPerRow;
			int offsetInBlock = (i - rowStart) / blocksPerRow;
			int resultIndex = rowStart + (blockInRow << 2) + offsetInBlock;
			result.put(resultIndex, src.get(i));
		}
		return result;
	}

	private static ByteBufferWrapper uncompressAndDeswizzle(ByteBufferWrapper src, int height, int width) {
		int imageSize = height * width;

		ByteBufferWrapper result = ByteBufferWrapper.allocateLE(imageSize);
		for (int i = 0; i < result.limit(); i++)
			result.put(i, (byte) 0xFF);
		result.rewind();

		int currentRow = 0;
		int blockInRow = 0;
		int offsetInBlock = 0;
		while (offsetInBlock < 4) {
			byte next = src.get();
			if (next == 0) {
				blockInRow = 0;
				if (++currentRow >= height) {
					currentRow = 0;
					offsetInBlock++;
				}
			} else {
				int count = Math.abs(next);
				if (next > 0) {
					int rowStart = currentRow * width;
					for (int i = 0; i < count; i++) {
						int resultIndex = rowStart + (blockInRow << 2) + offsetInBlock;
						result.put(resultIndex, src.get());
						blockInRow++;
					}
				} else {
					blockInRow += count;
				}
			}
		}
		return result;
	}

	private static class TILEHeader {
		private int short1;
		private int short2;
		private int short3;
		private int byte1;
		private int type;
		private ContentType contentType;

		private TILEHeader(int short1, int short2, int short3, int byte1, int type, @Nonnull ContentType contentType) {
			this.short1 = short1;
			this.short2 = short2;
			this.short3 = short3;
			this.byte1 = byte1;
			this.type = type;
			this.contentType = contentType;
		}

		public static TILEHeader readFrom(ByteBufferWrapper data, ContentType type) {
			int short1 = data.getShort();
			int short2 = data.getShort();
			int short3 = data.getShort();
			int byte1 = data.getUnsigned();
			int imageType = data.getUnsigned();

			return new TILEHeader(short1, short2, short3, byte1, imageType, type);
		}
	}
}
