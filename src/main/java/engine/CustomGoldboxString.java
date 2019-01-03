package engine;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import types.GoldboxString;

public class CustomGoldboxString extends GoldboxString {
	protected List<Byte> content = new ArrayList<>();
	private String originalString;

	public CustomGoldboxString(@Nonnull String s) {
		setText(s);
	}

	protected void setText(@Nonnull String s) {
		this.originalString = s;
		content.clear();
		s.chars().forEachOrdered(c -> content.add(fromASCII((char) c)));
	}

	@Override
	public byte getChar(int index) {
		return content.get(index);
	}

	@Override
	public int getLength() {
		return content.size();
	}

	@Override
	public String toString() {
		return originalString;
	}
}
