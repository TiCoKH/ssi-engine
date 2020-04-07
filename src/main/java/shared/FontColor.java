package shared;

import java.awt.Color;

import javax.annotation.Nonnull;

import data.palette.Palette;

public enum FontColor {
	NORMAL(Palette.COLOR_GREEN_BRIGHT, Palette.COLOR_GREY_LIGHT), //
	INTENSE(null, Palette.COLOR_CYAN), //
	RUNES(Palette.COLOR_WHITE, Palette.COLOR_GREY_LIGHT), // (
	SHORTCUT(Palette.COLOR_WHITE, Palette.COLOR_GREY_LIGHT), // (
	GAME_NAME(Palette.COLOR_MAGENTA_BRIGHT, Palette.COLOR_GREY_LIGHT), //
	PC(Palette.COLOR_WHITE, Palette.COLOR_WHITE), //
	SEL_PC(Palette.COLOR_BLUE_BRIGHT, Palette.COLOR_GREY_LIGHT), //
	PC_HEADING(Palette.COLOR_WHITE, Palette.COLOR_RED_BRIGHT), //
	DAMAGE(Palette.COLOR_WHITE, Palette.COLOR_WHITE), //
	FUEL(Palette.COLOR_MAGENTA_BRIGHT, null), //
	ECL_0(Palette.COLOR_GREEN_BRIGHT, Palette.COLOR_GREY_LIGHT), //
	ECL_1(Palette.COLOR_GAME_STATIC[0x1], Palette.COLOR_BLACK), //
	ECL_2(Palette.COLOR_GAME_STATIC[0x2], Palette.COLOR_GREY_LIGHT), //
	ECL_3(Palette.COLOR_GAME_STATIC[0x3], Palette.COLOR_GREY_LIGHT), //
	ECL_4(Palette.COLOR_GAME_STATIC[0x4], Palette.COLOR_GREY_LIGHT), //
	ECL_5(Palette.COLOR_GAME_STATIC[0x5], Palette.COLOR_GREY_LIGHT), //
	ECL_6(Palette.COLOR_GAME_STATIC[0x6], Palette.COLOR_GREY_LIGHT), //
	ECL_7(Palette.COLOR_GAME_STATIC[0x7], Palette.COLOR_GREY_LIGHT), //
	ECL_8(Palette.COLOR_GAME_STATIC[0x8], Palette.COLOR_GREY_LIGHT), //
	ECL_9(Palette.COLOR_GAME_STATIC[0x9], Palette.COLOR_GREY_LIGHT), //
	ECL_A(Palette.COLOR_GAME_STATIC[0xA], Palette.COLOR_GREY_LIGHT), //
	ECL_B(Palette.COLOR_GAME_STATIC[0xB], Palette.COLOR_GREY_LIGHT), //
	ECL_C(Palette.COLOR_GAME_STATIC[0xC], Palette.COLOR_GREY_LIGHT), //
	ECL_D(Palette.COLOR_GAME_STATIC[0xD], Palette.COLOR_GREY_LIGHT), //
	ECL_E(Palette.COLOR_GAME_STATIC[0xE], Palette.COLOR_CYAN), //
	ECL_F(Palette.COLOR_GAME_STATIC[0xF], Palette.COLOR_GREY_LIGHT);

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
