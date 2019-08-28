package engine.text;

public enum SpecialCharType {
	UMLAUT_A((byte) -4), UMLAUT_O((byte) -3), UMLAUT_U((byte) -2), SHARP_S((byte) -1);

	private byte charIndex;

	private SpecialCharType(byte charIndex) {
		this.charIndex = charIndex;
	}

	public byte getCharIndex() {
		return charIndex;
	}
}
