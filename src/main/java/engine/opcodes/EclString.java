package engine.opcodes;

import java.nio.ByteBuffer;

public class EclString {
	private ByteBuffer content;

	public EclString(ByteBuffer content) {
		this.content = content;
	}

	public int getLength() {
		return content.capacity();
	}

	public byte getChar(int index) {
		return content.get(index);
	}

	private static char inflateChar(byte c) {
		return (char) (c > 0x1F ? c : c + 0x40);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < content.capacity(); i++) {
			sb.append(inflateChar(content.get(i)));
		}
		return sb.toString();
	}
}
