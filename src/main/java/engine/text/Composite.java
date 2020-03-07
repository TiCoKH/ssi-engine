package engine.text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import shared.GoldboxString;
import shared.GoldboxStringPart;

public class Composite extends GoldboxStringPart {
	private List<GoldboxString> parts = new ArrayList<>();

	Composite(GoldboxString part) {
		super(PartType.TEXT);
		this.parts.add(part);
	}

	Composite(Collection<? extends GoldboxString> parts) {
		super(PartType.TEXT);
		this.parts.addAll(parts);
	}

	Composite(Collection<? extends GoldboxString> parts, GoldboxString part) {
		this(parts);
		this.parts.add(part);
	}

	Composite plus(GoldboxString part) {
		return new Composite(parts, part);
	}

	@Override
	public byte getChar(int index) {
		int start = 0;
		for (GoldboxString part : parts) {
			if (index >= start && index < start + part.getLength()) {
				return part.getChar(index - start);
			}
			start += part.getLength();
		}
		return 0;
	}

	@Override
	public char getCharAsAscii(int index) {
		int start = 0;
		for (GoldboxString part : parts) {
			if (index >= start && index < start + part.getLength()) {
				return part.getCharAsAscii(index - start);
			}
			start += part.getLength();
		}
		return 0;
	}

	@Override
	public int getLength() {
		return this.parts.stream().mapToInt(GoldboxString::getLength).sum();
	}
}
