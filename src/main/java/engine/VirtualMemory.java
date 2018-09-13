package engine;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import data.content.DungeonMap.Direction;
import engine.opcodes.EclArgument;
import engine.opcodes.EclString;

public class VirtualMemory {
	private static final int MEMLOC_MAP_POS_X = 0xC04B;
	private static final int MEMLOC_MAP_POS_Y = 0xC04C;
	private static final int MEMLOC_MAP_ORIENTATION = 0xC04D;

	private ByteBuffer mem;

	public VirtualMemory() {
		mem = ByteBuffer.allocate(0x10000).order(ByteOrder.LITTLE_ENDIAN);
	}

	public int getCurrentMapX() {
		return mem.get(MEMLOC_MAP_POS_X) & 0xFF;
	}

	public void setCurrentMapX(int currentMapX) {
		mem.put(MEMLOC_MAP_POS_X, (byte) currentMapX);
	}

	public int getCurrentMapY() {
		return mem.get(MEMLOC_MAP_POS_Y) & 0xFF;
	}

	public void setCurrentMapY(int currentMapY) {
		mem.put(MEMLOC_MAP_POS_Y, (byte) currentMapY);
	}

	public Direction getCurrentMapOrient() {
		return Direction.withId(mem.get(MEMLOC_MAP_ORIENTATION) & 0xFF);
	}

	public void setCurrentMapOrient(Direction currentMapOrient) {
		mem.put(MEMLOC_MAP_ORIENTATION, (byte) currentMapOrient.getId());
	}

	public int readMemInt(EclArgument a) {
		if (!a.isMemAddress()) {
			return 0;
		}
		return a.isShortValue() ? mem.getShort(a.valueAsInt()) : mem.get(a.valueAsInt());
	}

	public int readMemInt(EclArgument base, int offset) {
		if (!base.isMemAddress()) {
			return 0;
		}
		return base.isShortValue() ? mem.getShort(base.valueAsInt() + offset) : mem.get(base.valueAsInt() + offset);
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

	public void writeMemInt(EclArgument base, int offset, int value) {
		if (!base.isMemAddress()) {
			return;
		}
		if (base.isShortValue()) {
			mem.putShort(base.valueAsInt() + offset, (short) value);
		} else {
			mem.put(base.valueAsInt() + offset, (byte) value);
		}
	}

	public void copyMemInt(EclArgument base, int offset, EclArgument target) {
		if (!base.isMemAddress() || !target.isMemAddress()) {
			return;
		}
		if (target.isShortValue()) {
			mem.putShort(target.valueAsInt(), (short) readMemInt(base, offset));
		} else {
			mem.put(target.valueAsInt(), (byte) readMemInt(base, offset));
		}
	}

	public EclString readMemString(EclArgument a) {
		return null;
	}

	public void writeMemString(EclArgument a, EclString value) {

	}

}
