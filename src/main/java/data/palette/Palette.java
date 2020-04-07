package data.palette;

import java.awt.Color;
import java.awt.image.IndexColorModel;
import java.util.Arrays;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import common.ByteBufferWrapper;
import data.ContentType;

public class Palette {
	public static final Color COLOR_TRANSPARENT = new Color(0xff000000);
	public static final Color COLOR_BLACK = new Color(0x000000);
	public static final Color COLOR_BLUE = new Color(0x0000AA);
	public static final Color COLOR_GREEN = new Color(0x00AA00);
	public static final Color COLOR_CYAN = new Color(0x00AAAA);
	public static final Color COLOR_RED = new Color(0xAA0000);
	public static final Color COLOR_MAGENTA = new Color(0xAA00AA);
	public static final Color COLOR_BROWN = new Color(0xAA5500);
	public static final Color COLOR_GREY_LIGHT = new Color(0xAAAAAA);
	public static final Color COLOR_GREY_DARK = new Color(0x555555);
	public static final Color COLOR_BLUE_BRIGHT = new Color(0x5555FF);
	public static final Color COLOR_GREEN_BRIGHT = new Color(0x55FF55);
	public static final Color COLOR_CYAN_BRIGHT = new Color(0x55FFFF);
	public static final Color COLOR_RED_BRIGHT = new Color(0xFF5555);
	public static final Color COLOR_MAGENTA_BRIGHT = new Color(0xFF55FF);
	public static final Color COLOR_YELLOW_BRIGHT = new Color(0xFFFF55);
	public static final Color COLOR_WHITE = new Color(0xFFFFFF);

	public static final Color[] COLOR_GAME_STATIC = { COLOR_BLACK, COLOR_BLUE, COLOR_GREEN, COLOR_CYAN, COLOR_RED, COLOR_MAGENTA, COLOR_BROWN, COLOR_GREY_LIGHT, COLOR_GREY_DARK, COLOR_BLUE_BRIGHT, COLOR_GREEN_BRIGHT, COLOR_CYAN_BRIGHT, COLOR_RED_BRIGHT, COLOR_MAGENTA_BRIGHT, COLOR_YELLOW_BRIGHT, COLOR_WHITE };
	private static final Color[] COLOR_SPRITE_STATIC = { COLOR_TRANSPARENT, COLOR_BLUE, COLOR_GREEN, COLOR_CYAN, COLOR_RED, COLOR_MAGENTA, COLOR_BROWN, COLOR_GREY_LIGHT, COLOR_GREY_DARK, COLOR_BLUE_BRIGHT, COLOR_GREEN_BRIGHT, COLOR_CYAN_BRIGHT, COLOR_RED_BRIGHT, COLOR_BLACK, COLOR_YELLOW_BRIGHT, COLOR_WHITE };
	private static final Color[] COLOR_COMBAT_STATIC = { COLOR_TRANSPARENT, COLOR_BLUE, COLOR_GREEN, COLOR_CYAN, COLOR_RED, COLOR_MAGENTA, COLOR_BROWN, COLOR_GREY_LIGHT, COLOR_BLACK, COLOR_BLUE_BRIGHT, COLOR_GREEN_BRIGHT, COLOR_CYAN_BRIGHT, COLOR_RED_BRIGHT, COLOR_MAGENTA_BRIGHT, COLOR_YELLOW_BRIGHT, COLOR_WHITE };

	public static IndexColorModel createColorModel(@Nonnull ContentType type) {
		switch (type) {
			case _8X8D:
			case BACK:
				return createColorModel(COLOR_GAME_STATIC, COLOR_MAGENTA_BRIGHT);
			case BIGPIC:
			case BODY:
			case HEAD:
			case PIC:
			case TITLE:
				return createColorModel(COLOR_GAME_STATIC, null);
			case SPRIT:
				return createColorModel(COLOR_SPRITE_STATIC, COLOR_TRANSPARENT);
			default:
				throw new IllegalArgumentException("illegal image type: " + type.name());
		}
	}

	private static IndexColorModel createColorModel(@Nonnull Color[] staticColors, @Nullable Color transparent) {
		byte[] r = new byte[16];
		byte[] g = new byte[16];
		byte[] b = new byte[16];

		for (int i = 0; i < staticColors.length; i++) {
			r[i] = (byte) staticColors[i].getRed();
			g[i] = (byte) staticColors[i].getGreen();
			b[i] = (byte) staticColors[i].getBlue();
		}
		if (transparent == null)
			return new IndexColorModel(4, 16, r, g, b);
		else
			return new IndexColorModel(4, 16, r, g, b, Arrays.asList(staticColors).indexOf(transparent));
	}

	public static IndexColorModel createColorModel(@Nonnull byte[] data, @Nonnull ContentType type) {
		switch (type) {
			case _8X8D:
			case BACK:
				return createColorModel(data, COLOR_GAME_STATIC, COLOR_MAGENTA_BRIGHT);
			case BIGPIC:
			case BODY:
			case HEAD:
			case PIC:
			case TITLE:
				return createColorModel(data, COLOR_GAME_STATIC, null);
			default:
				throw new IllegalArgumentException("illegal image type: " + type.name());
		}
	}

	private static IndexColorModel createColorModel(@Nonnull byte[] data, @Nonnull Color[] staticColors, @Nullable Color transparent) {
		byte[] r = new byte[256];
		byte[] g = new byte[256];
		byte[] b = new byte[256];

		for (int i = 0; i < staticColors.length; i++) {
			for (int h = 0; h < 0xFF; h += 0x10) {
				r[h + i] = (byte) staticColors[i].getRed();
				g[h + i] = (byte) staticColors[i].getGreen();
				b[h + i] = (byte) staticColors[i].getBlue();
			}
		}

		for (int i = 0; i < 16; i++) {
			r[0x10 + i] = (byte) (data[3 * i + 0] << 2);
			g[0x10 + i] = (byte) (data[3 * i + 1] << 2);
			b[0x10 + i] = (byte) (data[3 * i + 2] << 2);
		}

		if (transparent == null)
			return new IndexColorModel(8, 256, r, g, b);
		else
			return new IndexColorModel(8, 256, r, g, b, Arrays.asList(staticColors).indexOf(transparent));
	}

	public static IndexColorModel createColorModel(@Nonnull ByteBufferWrapper data, int dataOffset, int colorCount, int colorStart,
		@Nonnull ContentType type) {

		switch (type) {
			case _8X8D:
			case BACK:
				return createColorModel(data, dataOffset, colorCount, colorStart, COLOR_GAME_STATIC, COLOR_MAGENTA_BRIGHT);
			case BIGPIC:
			case BODY:
			case HEAD:
			case PIC:
			case TITLE:
				return createColorModel(data, dataOffset, colorCount, colorStart, COLOR_GAME_STATIC, null);
			case SPRIT:
				return createColorModel(data, dataOffset, colorCount, colorStart, COLOR_SPRITE_STATIC, COLOR_TRANSPARENT);
			default:
				throw new IllegalArgumentException("illegal image type: " + type.name());
		}
	}

	private static IndexColorModel createColorModel(ByteBufferWrapper data, int dataOffset, int colorCount, int colorStart, Color[] staticColors,
		@Nullable Color transparent) {

		byte[] r = new byte[256];
		byte[] g = new byte[256];
		byte[] b = new byte[256];

		for (int i = 0; i < staticColors.length; i++) {
			for (int h = 0; h < 0xFF; h += 0x10) {
				r[h + i] = (byte) staticColors[i].getRed();
				g[h + i] = (byte) staticColors[i].getGreen();
				b[h + i] = (byte) staticColors[i].getBlue();
			}
		}

		for (int i = 0; i < colorCount; i++) {
			r[colorStart + i] = (byte) (data.get(dataOffset + 3 * i + 0) << 2);
			g[colorStart + i] = (byte) (data.get(dataOffset + 3 * i + 1) << 2);
			b[colorStart + i] = (byte) (data.get(dataOffset + 3 * i + 2) << 2);
		}

		if (transparent == null)
			return new IndexColorModel(8, 256, r, g, b);
		else
			return new IndexColorModel(8, 256, r, g, b, Arrays.asList(staticColors).indexOf(transparent));
	}

	public static IndexColorModel createColorModelNoShift(@Nonnull ByteBufferWrapper data, int colorCount, int colorStart, boolean transparent,
		@Nonnull ContentType type) {

		switch (type) {
			case _8X8D:
			case BACK:
				return createColorModelNoShift(data, colorCount, colorStart, transparent, COLOR_GAME_STATIC);
			case BIGPIC:
			case PIC:
			case TITLE:
				return createColorModelNoShift(data, colorCount, colorStart, transparent, COLOR_GAME_STATIC);
			case SPRIT:
				return createColorModelNoShift(data, colorCount, colorStart, transparent, COLOR_SPRITE_STATIC);
			default:
				throw new IllegalArgumentException("illegal image type: " + type.name());
		}
	}

	private static IndexColorModel createColorModelNoShift(ByteBufferWrapper data, int colorCount, int colorStart, boolean transparent,
		Color[] staticColors) {

		byte[] r = new byte[256];
		byte[] g = new byte[256];
		byte[] b = new byte[256];

		for (int i = 0; i < staticColors.length; i++) {
			for (int h = 0; h < 0xFF; h += 0x10) {
				r[h + i] = (byte) staticColors[i].getRed();
				g[h + i] = (byte) staticColors[i].getGreen();
				b[h + i] = (byte) staticColors[i].getBlue();
			}
		}

		for (int i = 0; i < colorCount; i++) {
			r[colorStart + i] = data.get(3 * i + 0);
			g[colorStart + i] = data.get(3 * i + 1);
			b[colorStart + i] = data.get(3 * i + 2);
		}

		if (transparent) {
			return new IndexColorModel(8, 256, r, g, b, 255);
		}
		return new IndexColorModel(8, 256, r, g, b);
	}

	public static IndexColorModel toInvertedPalette(@Nullable IndexColorModel cm) {
		if (cm == null || cm.getMapSize() == 2) {
			return binaryPaletteWith(COLOR_WHITE, COLOR_BLACK, -1);
		}
		return paletteWith(cm, COLOR_WHITE, COLOR_BLACK);
	}

	public static IndexColorModel toPaletteWithGreenFG(@Nullable IndexColorModel cm) {
		return toPaletteWithFG(cm, COLOR_GREEN_BRIGHT);
	}

	public static IndexColorModel toPaletteWithMagentaFG(@Nullable IndexColorModel cm) {
		return toPaletteWithFG(cm, COLOR_MAGENTA_BRIGHT);
	}

	public static IndexColorModel toPaletteWithFG(@Nullable IndexColorModel cm, @Nonnull Color fgColor) {
		if (cm == null || cm.getMapSize() == 2) {
			return binaryPaletteWith(COLOR_BLACK, fgColor, 0);
		}
		return paletteWith(cm, COLOR_BLACK, fgColor);
	}

	private static IndexColorModel binaryPaletteWith(@Nonnull Color bgColor, @Nonnull Color fgColor, int trans) {
		byte[] r = new byte[] { (byte) bgColor.getRed(), (byte) fgColor.getRed() };
		byte[] g = new byte[] { (byte) bgColor.getGreen(), (byte) fgColor.getGreen() };
		byte[] b = new byte[] { (byte) bgColor.getBlue(), (byte) fgColor.getBlue() };

		return new IndexColorModel(1, 2, r, g, b, trans);
	}

	private static IndexColorModel paletteWith(@Nonnull IndexColorModel cm, @Nonnull Color bgColor, @Nonnull Color fgColor) {
		byte[] r = new byte[cm.getMapSize()];
		byte[] g = new byte[cm.getMapSize()];
		byte[] b = new byte[cm.getMapSize()];

		for (int i = 0; i < cm.getMapSize(); i++) {
			r[i] = (byte) cm.getRed(i);
			g[i] = (byte) cm.getGreen(i);
			b[i] = (byte) cm.getBlue(i);
		}
		r[0x0] = (byte) bgColor.getRed();
		g[0x0] = (byte) bgColor.getGreen();
		b[0x0] = (byte) bgColor.getBlue();
		r[0xF] = (byte) fgColor.getRed();
		g[0xF] = (byte) fgColor.getGreen();
		b[0xF] = (byte) fgColor.getBlue();

		return new IndexColorModel(cm.getComponentSize(0), cm.getMapSize(), r, g, b, cm.getTransparentPixel());
	}
}
