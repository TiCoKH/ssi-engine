package engine.text;

import javax.annotation.Nonnull;

import shared.GoldboxStringPart;

public class SpecialChar extends GoldboxStringPart {

	private SpecialCharType type;

	public SpecialChar(@Nonnull SpecialCharType type) {
		super(GoldboxStringPart.PartType.SPECIAL_CHAR);
		this.type = type;
	}

	@Override
	public byte getChar(int index) {
		return this.type.getCharIndex();
	}

	@Override
	public char getCharAsAscii(int index) {
		switch (this.type) {
			case UMLAUT_A:
				return 'Ä';
			case UMLAUT_O:
				return 'Ö';
			case UMLAUT_U:
				return 'Ü';
			case SHARP_S:
				return 'ß';
		}
		return '?';
	}

	@Override
	public int getLength() {
		return 1;
	}
}
