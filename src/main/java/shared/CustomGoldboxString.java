package shared;

import javax.annotation.Nonnull;

import io.vavr.API;
import io.vavr.collection.Seq;

public class CustomGoldboxString extends GoldboxString {
	protected Seq<Byte> content;
	private String originalString;

	public CustomGoldboxString(@Nonnull String s) {
		setText(s);
	}

	protected void setText(@Nonnull String s) {
		this.originalString = s;
		content = API.CharSeq(s).map(GoldboxString::fromASCII);
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
