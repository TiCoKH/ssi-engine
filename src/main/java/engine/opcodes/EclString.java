package engine.opcodes;

import common.ByteBufferWrapper;

public class EclString {
	private ByteBufferWrapper content;

	public EclString(ByteBufferWrapper content) {
		this.content = content;
	}

	public EclString(String s) {
		content = ByteBufferWrapper.allocateLE(s.length());
		s.chars().forEachOrdered(c -> content.put(deflateChar((char) c)));
	}

	public int getLength() {
		return content.limit();
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
		for (int i = 0; i < content.limit(); i++) {
			sb.append(inflateChar(content.get(i)));
		}
		return sb.toString();
	}
}
