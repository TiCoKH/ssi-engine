package shared;

import java.util.Optional;

public abstract class GoldboxStringPart extends GoldboxString {
	private PartType type;
	private GoldboxString source;
	private int fromIndex;
	private int toIndex;

	protected GoldboxStringPart(PartType type) {
		this.type = type;
	}

	protected GoldboxStringPart(PartType type, GoldboxString source, int fromIndex, int toIndex) {
		this.type = type;
		this.source = source;
		this.fromIndex = fromIndex;
		this.toIndex = toIndex;
	}

	@Override
	public byte getChar(int index) {
		return source.getChar(fromIndex + index);
	}

	@Override
	public int getLength() {
		return toIndex - fromIndex;
	}

	public PartType getType() {
		return type;
	}

	public Optional<FontColor> getFontColor() {
		return Optional.empty();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < getLength(); i++) {
			sb.append(getCharAsAscii(i));
		}
		return sb.toString();
	}

	public enum PartType {
		COLOR(false), //
		LEADING_SPACE(true), //
		LINE_BREAK(false), //
		SPACE(true), //
		SPECIAL_CHAR(true), //
		TEXT(true);

		private boolean displayable;

		private PartType(boolean displayable) {
			this.displayable = displayable;
		}

		public boolean isDisplayable() {
			return displayable;
		}
	}
}
