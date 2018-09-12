package engine.opcodes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class EclString {
	private ByteBuffer content;

	public EclString(ByteBuffer content) {
		this.content = content;
	}

	public EclString(String s) {
		content = ByteBuffer.allocate(s.length()).order(ByteOrder.LITTLE_ENDIAN);
		s.chars().forEachOrdered(c -> content.put(deflateChar((char) c)));
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

	private static byte deflateChar(char c) {
		return (byte) (c >= 0x40 && c <= 0x5F ? c - 0x40 : c);
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
