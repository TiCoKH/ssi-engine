package ui;

import static shared.GoldboxStringPart.PartType.COLOR;
import static shared.GoldboxStringPart.PartType.TEXT;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;

import shared.FontColor;
import shared.GoldboxStringPart;
import ui.UISettings.TextSpeed;

public class StoryText {
	private List<GoldboxStringPart> textList = new ArrayList<>();
	private int charCount = 0;
	private int charStop = 0;
	private Optional<FontColor> defaultTextColor = Optional.empty();

	public void addText(@Nonnull List<GoldboxStringPart> moreText) {
		textList.addAll(moreText);
		updateCharCount();
	}

	public void clearScreen() {
		// set to last text color of current text
		defaultTextColor = textList.stream() //
			.filter(t -> COLOR.equals(t.getType())) //
			.filter(t -> t.getFontColor().map(FontColor::isECLColor).orElse(false)) //
			.map(textList::indexOf) //
			.max(Integer::compare) //
			.map(textList::get) //
			.map(t -> t.getFontColor().get());
		resetText();
	}

	public void resetText() {
		textList.clear();
		charCount = 0;
		charStop = 0;
	}

	@Nonnull
	public Optional<FontColor> getDefaultTextColor() {
		return defaultTextColor;
	}

	@Nonnull
	public Optional<List<GoldboxStringPart>> getTextList() {
		return textList.isEmpty() ? Optional.empty() : Optional.of(ImmutableList.copyOf(textList));
	}

	public int getCharStop() {
		return charStop;
	}

	public boolean hasCharStopReachedLimit() {
		return charStop >= charCount;
	}

	public void incCharStop(TextSpeed textSpeed) {
		switch (textSpeed) {
			case SLOW:
				charStop++;
				break;
			case FAST:
				int count = 0;
				int index = 0;
				GoldboxStringPart part = textList.get(index);
				while (count < charStop && index < textList.size()) {
					if (part.getType().isDisplayable()) {
						count += part.getLength();
					}
					part = textList.get(++index);
				}
				if (part != null && TEXT.equals(part.getType())) {
					charStop = count + part.getLength();
				} else {
					for (int i = index; i < textList.size(); i++) {
						part = textList.get(i);
						if (part.getType().isDisplayable()) {
							count += part.getLength();
						}
						if (TEXT.equals(part.getType())) {
							charStop = count;
							break;
						}
						index++;
					}
					charStop = count;
				}
				break;
			case INSTANT:
				charStop = charCount;
		}
	}

	private void updateCharCount() {
		charCount = textList.stream().filter(t -> t.getType().isDisplayable()).mapToInt(GoldboxStringPart::getLength).sum();
	}
}
