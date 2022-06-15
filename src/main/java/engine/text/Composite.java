package engine.text;

import static io.vavr.API.Seq;

import io.vavr.collection.Seq;

import shared.GoldboxString;
import shared.GoldboxStringPart;

public class Composite extends GoldboxStringPart {
	private Seq<GoldboxString> parts = Seq();

	Composite(GoldboxString part) {
		super(PartType.TEXT);
		parts = parts.append(part);
	}

	Composite(Iterable<? extends GoldboxString> partsIt) {
		super(PartType.TEXT);
		parts = parts.appendAll(partsIt);
	}

	Composite(Iterable<? extends GoldboxString> partsIt, GoldboxString part) {
		this(partsIt);
		parts = parts.append(part);
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
		return this.parts.map(GoldboxString::getLength).sum().intValue();
	}
}
