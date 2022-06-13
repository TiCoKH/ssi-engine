package ui.shared.text;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import shared.CustomGoldboxString;

public class GoldboxStringInput extends CustomGoldboxString {

	private List<Character> chars = new ArrayList<>();

	private int maxCount;

	public GoldboxStringInput(@Nonnull String prefix, int maxCount) {
		super(prefix);
		this.maxCount = maxCount;
	}

	public void addChar(char c) {
		if (getInputCount() < maxCount) {
			chars.add(c);
			content = content.append(fromASCII(c));
		}
	}

	public int getInputCount() {
		return chars.size();
	}

	public void removeLastChar() {
		if (getInputCount() > 0) {
			chars.remove(chars.size() - 1);
			content = content.removeAt(content.size() - 1);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		chars.stream().forEachOrdered(c -> sb.append(c));
		return sb.toString();
	}
}
