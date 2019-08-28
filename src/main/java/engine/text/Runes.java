package engine.text;

import static shared.GoldboxStringPart.PartType.TEXT;

import java.util.Optional;

import shared.FontColor;
import shared.GoldboxString;
import shared.GoldboxStringPart;

public class Runes extends GoldboxStringPart {
	Runes(GoldboxString source, int fromIndex, int toIndex) {
		super(TEXT, source, fromIndex, toIndex);
	}

	@Override
	public byte getChar(int index) {
		return (byte) (super.getChar(index) + 1);
	}

	@Override
	public Optional<FontColor> getFontColor() {
		return Optional.of(FontColor.RUNES);
	}
}
