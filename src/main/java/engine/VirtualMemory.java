package engine;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import engine.opcodes.EclArgument;

public class VirtualMemory {
	private ByteBuffer mem;

	public VirtualMemory() {
		mem = ByteBuffer.allocate(0x10000).order(ByteOrder.LITTLE_ENDIAN);
	}

	public int readMemInt(EclArgument a) {
		if (!a.isMemAddress()) {
			return 0;
		}
		return a.isShortValue() ? mem.getShort(a.valueAsInt()) : mem.get(a.valueAsInt());
	}

	public void writeMemInt(EclArgument a, int value) {
		if (!a.isMemAddress()) {
			return;
		}
		if (a.isShortValue()) {
			mem.putShort(a.valueAsInt(), (short) value);
		} else {
			mem.put(a.valueAsInt(), (byte) value);
		}
	}

	public String readMemString(EclArgument a) {
		return "";
	}

	public void writeMemString(EclArgument a, String value) {

	}

}
