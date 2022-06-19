package ui.shared.text;

import static io.vavr.API.CharSeq;

import javax.annotation.Nonnull;

import io.vavr.collection.CharSeq;

import shared.CustomGoldboxString;

public class GoldboxStringInput extends CustomGoldboxString {

	private CharSeq chars = CharSeq("");

	private int maxCount;

	public GoldboxStringInput(@Nonnull String prefix, int maxCount) {
		super(prefix);
		this.maxCount = maxCount;
	}

	public void addChar(char c) {
		if (getInputCount() < maxCount) {
			chars = chars.append(c);
			content = content.append(fromASCII(c));
		}
	}

	public int getInputCount() {
		return chars.size();
	}

	public void removeLastChar() {
		if (getInputCount() > 0) {
			chars = chars.removeAt(chars.size() - 1);
			content = content.removeAt(content.size() - 1);
		}
	}

	@Override
	public String toString() {
		return chars.toString();
	}
}
