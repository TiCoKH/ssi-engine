package engine.opcodes;

import java.nio.ByteBuffer;

public class EclArgument {
	private int type;
	private Object value;

	private EclArgument(int type, Object value) {
		this.type = type;
		this.value = value;
	}

	public static EclArgument parseNext(ByteBuffer eclBlock) {
		int type = eclBlock.get() & 0xFF;
		switch (type) {
		case 1:
		case 2:
		case 3: {
			int value = eclBlock.getShort() & 0xFFFF;
			return new EclArgument(type, value);
		}
		case 0x80: {
			// compressed string
			int strLen = eclBlock.get() & 0xFF;
			String value = ""; // TODO
			return new EclArgument(type, value);
		}
		case 0x81: {
			// string from memory
			int address = eclBlock.getShort() & 0xFFFF;
			String value = ""; // TODO
			return new EclArgument(type, value);
		}
		default: {
			int value = eclBlock.get() & 0xFF;
			return new EclArgument(type, value);
		}
		}
	}

	public int getType() {
		return type;
	}

	public Object getValue() {
		return value;
	}

	public boolean isNumberValue() {
		return (type == 1 || type == 2 || type == 3);
	}

	public boolean isStringValue() {
		return (type == 0x80 || type == 0x81);
	}

	public int valueAsInt() {
		return (Integer) value;
	}

	public String valueAsString() {
		return (String) value;
	}

	@Override
	public String toString() {
		return type + ":" + value.toString();
	}
}
