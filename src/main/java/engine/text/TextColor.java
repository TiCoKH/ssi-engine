package engine.text;

import java.util.Optional;

import javax.annotation.Nonnull;

import shared.FontColor;
import shared.GoldboxString;
import shared.GoldboxStringPart;

public class TextColor extends GoldboxStringPart {
	private FontColor fontColor;

	TextColor(@Nonnull FontColor fontColor) {
		super(PartType.COLOR);
		this.fontColor = fontColor;
	}

	TextColor(int textcolor) {
		super(PartType.COLOR);
		this.fontColor = FontColor.forTextColor(textcolor);
	}

	TextColor(@Nonnull GoldboxString source, int fromIndex, int toIndex) {
		super(PartType.COLOR, source, fromIndex, toIndex);
		this.fontColor = FontColor.forTextColor(this);
	}

	@Override
	public Optional<FontColor> getFontColor() {
		return Optional.of(fontColor);
	}
}
