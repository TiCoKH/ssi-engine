package engine.opcodes;

import common.ByteBufferWrapper;
import types.GoldboxString;

public class EclString extends GoldboxString {
	private ByteBufferWrapper content;

	public EclString(ByteBufferWrapper content) {
		this.content = content;
	}

	@Override
	public int getLength() {
		return content.limit();
	}

	@Override
	public byte getChar(int index) {
		return content.get(index);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < content.limit(); i++) {
			sb.append(toASCII(content.get(i)));
		}
		return sb.toString();
	}
}
