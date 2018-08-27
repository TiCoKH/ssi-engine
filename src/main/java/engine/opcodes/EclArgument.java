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
		case 0: {
			int value = eclBlock.get() & 0xFF;
			return new EclArgument(type, value);
		}
		case 1:
		case 2:
		case 3:
		case 5:
		case 0x81: // string from memory address
		{
			int value = eclBlock.getShort() & 0xFFFF;
			return new EclArgument(type, value);
		}
		case 0x80: {
			// compressed string
			int strLen = eclBlock.get() & 0xFF;
			byte[] cmpString = new byte[strLen];
			eclBlock.get(cmpString);
			String value = decompressString(cmpString);
			return new EclArgument(type, value);
		}
		default: {
			throw new IllegalArgumentException("Unknown type: " + type);
		}
		}
	}

	/**
	 * Decode compressed string data of the form AAAAAABB BBBBCCCC CCDDDDDD to
	 * 0x00AAAAAA 0x00BBBBBB 0x00CCCCCC 0x00DDDDDD.
	 * 
	 * @param data
	 * @return
	 */
	private static String decompressString(byte[] data) {
		StringBuilder sb = new StringBuilder();
		int state = 1;
		int lastByte = 0;

		for (byte b : data) {
			int ubyte = b & 0xFF;
			int charVal = 0;
			switch (state) {
			case 1:
				charVal = (ubyte >> 2) & 0x3F;
				state = 2;
				break;
			case 2:
				charVal = ((lastByte << 4) | (ubyte >> 4)) & 0x3F;
				state = 3;
				break;
			case 3:
				charVal = ((lastByte << 2) | (ubyte >> 6)) & 0x3F;
				if (charVal != 0) {
					sb.append(inflateChar(charVal));
				}
				charVal = ubyte & 0x3F;
				state = 1;
				break;
			}
			if (charVal != 0) {
				sb.append(inflateChar(charVal));
			}
			lastByte = ubyte;
		}

		return sb.toString();
	}

	private static char inflateChar(int c) {
		return (char) (c > 0x1F ? c : c + 0x40);
	}

	public int getType() {
		return type;
	}

	public Object getValue() {
		return value;
	}

	public boolean isMemAddress() {
		return (type & 0x01) > 0;
	}

	public boolean isShortValue() {
		return (type & 0x02) > 0;
	}

	public boolean isNumberValue() {
		return (type & 0x80) == 0;
	}

	public boolean isStringValue() {
		return (type & 0x80) > 0;
	}

	public int valueAsInt() {
		return (Integer) value;
	}

	public String valueAsString() {
		return (String) value;
	}

	@Override
	public String toString() {
		if (isMemAddress()) {
			return type + ":0x" + Integer.toHexString(valueAsInt()).toUpperCase();
		}
		if (type == 0x80) {
			return type + ":\"" + value.toString() + '"';
		}
		return type + ":" + value.toString();
	}
}
