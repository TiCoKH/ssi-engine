package engine.opcodes;

import java.util.ArrayList;
import java.util.List;

import common.ByteBufferWrapper;
import types.GoldboxString;

public class EclArgument {
	private int type;
	private int size;
	private Object value;

	public EclArgument(int type, int size, Object value) {
		this.type = type;
		this.size = size;
		this.value = value;
	}

	public static EclArgument parseNext(ByteBufferWrapper eclBlock) {
		int pos = eclBlock.position();
		int type = eclBlock.getUnsigned();
		switch (type) {
			case 0: {
				int value = eclBlock.getUnsigned();
				return new EclArgument(type, 2, value);
			}
			case 1:
			case 2:
			case 3:
			case 5:
			case 0x81: // string from memory address
			{
				int value = eclBlock.getUnsignedShort();
				return new EclArgument(type, 3, value);
			}
			case 0x80: {
				// compressed string
				int strLen = eclBlock.getUnsigned();
				byte[] cmpString = new byte[strLen];
				eclBlock.get(cmpString);
				GoldboxString value = decompressString(cmpString);
				return new EclArgument(type, 2 + strLen, value);
			}
			default: {
				throw new IllegalArgumentException("Unknown type: " + type + " at " + Integer.toHexString(pos));
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
	private static GoldboxString decompressString(byte[] data) {
		int state = 1;
		int lastByte = 0;

		List<Byte> chars = new ArrayList<>();
		for (byte b : data) {
			int ubyte = b & 0xFF;
			byte charVal = 0;

			if (state == 1) {
				charVal = (byte) ((ubyte >> 2) & 0x3F);
				state = 2;
			} else if (state == 2) {
				charVal = (byte) (((lastByte << 4) | (ubyte >> 4)) & 0x3F);
				state = 3;
			} else if (state == 3) {
				charVal = (byte) (((lastByte << 2) | (ubyte >> 6)) & 0x3F);
				if (charVal != 0) {
					chars.add(charVal);
				}
				charVal = (byte) (ubyte & 0x3F);
				state = 1;
			}

			if (charVal != 0) {
				chars.add(charVal);
			}
			lastByte = ubyte;
		}

		ByteBufferWrapper string = ByteBufferWrapper.allocateLE(chars.size());
		chars.stream().forEachOrdered(string::put);
		return new EclString(string);
	}

	public int getSize() {
		return size;
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

	public GoldboxString valueAsString() {
		return (EclString) value;
	}

	@Override
	public String toString() {
		if (isMemAddress()) {
			return String.format("%d:0x%04X", type, valueAsInt());
		}
		if (type == 0x80) {
			return type + ":\"" + value.toString() + '"';
		}
		return type + ":" + value.toString();
	}
}
