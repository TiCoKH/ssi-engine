package shared;

import java.awt.Color;

import javax.annotation.Nonnull;

import data.content.DAXPalette;

public enum FontColor {
	NORMAL(DAXPalette.COLOR_GREEN_BRIGHT, DAXPalette.COLOR_GREY_LIGHT), //
	INTENSE(null, DAXPalette.COLOR_CYAN), //
	RUNES(DAXPalette.COLOR_WHITE, DAXPalette.COLOR_GREY_LIGHT), // (
	SHORTCUT(DAXPalette.COLOR_WHITE, DAXPalette.COLOR_GREY_LIGHT), // (
	GAME_NAME(DAXPalette.COLOR_MAGENTA_BRIGHT, DAXPalette.COLOR_GREY_LIGHT), //
	PC(DAXPalette.COLOR_WHITE, DAXPalette.COLOR_WHITE), //
	SEL_PC(DAXPalette.COLOR_BLUE_BRIGHT, DAXPalette.COLOR_GREY_LIGHT), //
	PC_HEADING(DAXPalette.COLOR_WHITE, DAXPalette.COLOR_RED_BRIGHT), //
	DAMAGE(DAXPalette.COLOR_WHITE, DAXPalette.COLOR_WHITE), //
	FUEL(DAXPalette.COLOR_MAGENTA_BRIGHT, null), //
	ECL_0(DAXPalette.COLOR_GREEN_BRIGHT, DAXPalette.COLOR_GREY_LIGHT), //
	ECL_1(DAXPalette.COLOR_GAME_STATIC[0x1], DAXPalette.COLOR_BLACK), //
	ECL_2(DAXPalette.COLOR_GAME_STATIC[0x2], DAXPalette.COLOR_GREY_LIGHT), //
	ECL_3(DAXPalette.COLOR_GAME_STATIC[0x3], DAXPalette.COLOR_GREY_LIGHT), //
	ECL_4(DAXPalette.COLOR_GAME_STATIC[0x4], DAXPalette.COLOR_GREY_LIGHT), //
	ECL_5(DAXPalette.COLOR_GAME_STATIC[0x5], DAXPalette.COLOR_GREY_LIGHT), //
	ECL_6(DAXPalette.COLOR_GAME_STATIC[0x6], DAXPalette.COLOR_GREY_LIGHT), //
	ECL_7(DAXPalette.COLOR_GAME_STATIC[0x7], DAXPalette.COLOR_GREY_LIGHT), //
	ECL_8(DAXPalette.COLOR_GAME_STATIC[0x8], DAXPalette.COLOR_GREY_LIGHT), //
	ECL_9(DAXPalette.COLOR_GAME_STATIC[0x9], DAXPalette.COLOR_GREY_LIGHT), //
	ECL_A(DAXPalette.COLOR_GAME_STATIC[0xA], DAXPalette.COLOR_GREY_LIGHT), //
	ECL_B(DAXPalette.COLOR_GAME_STATIC[0xB], DAXPalette.COLOR_GREY_LIGHT), //
	ECL_C(DAXPalette.COLOR_GAME_STATIC[0xC], DAXPalette.COLOR_GREY_LIGHT), //
	ECL_D(DAXPalette.COLOR_GAME_STATIC[0xD], DAXPalette.COLOR_GREY_LIGHT), //
	ECL_E(DAXPalette.COLOR_GAME_STATIC[0xE], DAXPalette.COLOR_CYAN), //
	ECL_F(DAXPalette.COLOR_GAME_STATIC[0xF], DAXPalette.COLOR_GREY_LIGHT);

	private static final String ECL_PREFIX = "ECL_";

	private Color fontColor;
	private Color frameFontColor;

	private FontColor(Color fontColor, Color frameFontColor) {
		this.fontColor = fontColor;
		this.frameFontColor = frameFontColor;
	}

	public Color getFontColor() {
		return fontColor;
	}

	public Color getFrameFontColor() {
		return frameFontColor;
	}

	public boolean isECLColor() {
		return name().startsWith(ECL_PREFIX);
	}

	public static FontColor forTextColor(@Nonnull GoldboxStringPart textColor) {
		return valueOf(ECL_PREFIX + textColor.getCharAsAscii(0));
	}

	public static FontColor forTextColor(int textColor) {
		return valueOf(ECL_PREFIX + String.format("%1X", textColor));
	}
}
