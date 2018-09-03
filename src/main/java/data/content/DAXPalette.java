package data.content;

import java.awt.Color;
import java.nio.ByteBuffer;

public class DAXPalette {
	private static final Color COLOR_TRANSPARENT = new Color(0x67F79F);
	private static final Color COLOR_GREY = new Color(0x525252);

	private static final Color COLOR_BLACK = new Color(0x000000);
	private static final Color COLOR_BLUE = new Color(0x0000AA);
	private static final Color COLOR_GREEN = new Color(0x00AA00);
	private static final Color COLOR_CYAN = new Color(0x00AAAA);
	private static final Color COLOR_RED = new Color(0xAA0000);
	private static final Color COLOR_MAGENTA = new Color(0xAA00AA);
	private static final Color COLOR_BROWN = new Color(0xAA5500);
	private static final Color COLOR_GREY_LIGHT = new Color(0xAAAAAA);
	private static final Color COLOR_GREY_DARK = new Color(0x555555);
	private static final Color COLOR_BLUE_BRIGHT = new Color(0x5555FF);
	private static final Color COLOR_GREEN_BRIGHT = new Color(0x55FF55);
	private static final Color COLOR_CYAN_BRIGHT = new Color(0x55FFFF);
	private static final Color COLOR_RED_BRIGHT = new Color(0xFF5555);
	private static final Color COLOR_MAGENTA_BRIGHT = new Color(0xFF55FF);
	private static final Color COLOR_YELLOW_BRIGHT = new Color(0xFFFF55);
	private static final Color COLOR_WHITE = new Color(0xFFFFFF);

	private static final Color[] COLOR_GAME_STATIC = { COLOR_BLACK, COLOR_BLUE, COLOR_GREEN, COLOR_CYAN, COLOR_RED,
			COLOR_MAGENTA, COLOR_BROWN, COLOR_GREY_LIGHT, COLOR_GREY_DARK, COLOR_BLUE_BRIGHT, COLOR_GREEN_BRIGHT,
			COLOR_CYAN_BRIGHT, COLOR_RED_BRIGHT, COLOR_MAGENTA_BRIGHT, COLOR_YELLOW_BRIGHT, COLOR_WHITE };

	private static final Color[] COLOR_COMBAT_STATIC = { COLOR_TRANSPARENT, COLOR_BLUE, COLOR_GREEN, COLOR_CYAN,
			COLOR_RED, COLOR_MAGENTA, COLOR_BROWN, COLOR_GREY_LIGHT, COLOR_BLACK, COLOR_BLUE_BRIGHT, COLOR_GREEN_BRIGHT,
			COLOR_CYAN_BRIGHT, COLOR_RED_BRIGHT, COLOR_MAGENTA_BRIGHT, COLOR_YELLOW_BRIGHT, COLOR_WHITE };

	public static Color[] createGamePalette(ByteBuffer data, int dataOffset, int colorCount, int colorStart) {
		return createPalette(data, dataOffset, colorCount, colorStart, COLOR_GAME_STATIC);
	}

	public static Color[] createCombatPalette(ByteBuffer data, int dataOffset, int colorCount, int colorStart) {
		return createPalette(data, dataOffset, colorCount, colorStart, COLOR_COMBAT_STATIC);
	}

	private static Color[] createPalette(ByteBuffer data, int dataOffset, int colorCount, int colorStart,
			Color[] staticColors) {

		Color[] result = new Color[256];
		for (int i = 0; i < staticColors.length; i++) {
			result[0x00 + i] = staticColors[i];
			result[0x10 + i] = staticColors[i];
			result[0x20 + i] = staticColors[i];
			result[0x30 + i] = staticColors[i];
			result[0x40 + i] = staticColors[i];
			result[0x50 + i] = staticColors[i];
			result[0x60 + i] = staticColors[i];
			result[0x70 + i] = staticColors[i];
			result[0x80 + i] = staticColors[i];
			result[0x90 + i] = staticColors[i];
			result[0xA0 + i] = staticColors[i];
			result[0xB0 + i] = staticColors[i];
			result[0xC0 + i] = staticColors[i];
			result[0xD0 + i] = staticColors[i];
			result[0xE0 + i] = staticColors[i];
			result[0xF0 + i] = staticColors[i];
		}
		for (int i = 0; i < colorCount; i++) {
			int r = 4 * (data.get(dataOffset + 3 * i + 0) & 0xFF);
			int g = 4 * (data.get(dataOffset + 3 * i + 1) & 0xFF);
			int b = 4 * (data.get(dataOffset + 3 * i + 2) & 0xFF);
			result[colorStart + i] = new Color((r << 16) | (g << 8) | b);
		}
		return result;
	}
}
