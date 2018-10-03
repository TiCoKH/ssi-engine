package engine;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import data.content.DungeonMap.Direction;
import engine.opcodes.EclArgument;
import engine.opcodes.EclString;

public class VirtualMemory {
	public static final int MEMLOC_CURRENT_ECL = 0x0000;
	public static final int MEMLOC_AREA_START = 0x0001;
	public static final int MEMLOC_AREA_DECO_START = 0x0004;
	public static final int MEMLOC_LAST_ECL = 0x4BF2;
	public static final int MEMLOC_FOR_LOOP_COUNT = 0x4CF6;
	public static final int MEMLOC_COMBAT_RESULT = 0x7EC7;
	public static final int MEMLOC_MAP_POS_X = 0xC04B;
	public static final int MEMLOC_MAP_POS_Y = 0xC04C;
	public static final int MEMLOC_MAP_ORIENTATION = 0xC04D;
	public static final int MEMLOC_MAP_WALL_TYPE = 0xC04E;
	public static final int MEMLOC_MAP_SQUARE_INFO = 0xC04F;

	private ByteBuffer mem;

	private int menuChoice;

	public VirtualMemory() {
		mem = ByteBuffer.allocate(0x10000).order(ByteOrder.LITTLE_ENDIAN);
	}

	public void loadFrom(FileChannel fc) throws IOException {
		mem.position(0);
		try {
			fc.read(mem, 0);
		} finally {
			fc.close();
		}
	}

	public void saveTo(FileChannel fc) throws IOException {
		mem.position(0);
		try {
			fc.write(mem, 0);
			fc.force(true);
		} finally {
			fc.close();
		}
	}

	public int getMenuChoice() {
		return menuChoice;
	}

	public void setMenuChoice(int menuChoice) {
		this.menuChoice = menuChoice;
	}

	public int getCurrentECL() {
		return mem.get(MEMLOC_CURRENT_ECL) & 0xFF;
	}

	public void setCurrentECL(int currentECL) {
		mem.put(MEMLOC_CURRENT_ECL, (byte) currentECL);
	}

	public int getAreaValue(int id) {
		return mem.get(MEMLOC_AREA_START + id) & 0xFF;
	}

	public void setAreaValues(int id0, int id1, int id2) {
		mem.put(MEMLOC_AREA_START, (byte) id0);
		mem.put(MEMLOC_AREA_START + 1, (byte) id1);
		mem.put(MEMLOC_AREA_START + 2, (byte) id2);
	}

	public int getAreaDecoValue(int id) {
		return mem.get(MEMLOC_AREA_DECO_START + id) & 0xFF;
	}

	public void setAreaDecoValues(int id0, int id1, int id2) {
		mem.put(MEMLOC_AREA_DECO_START, (byte) id0);
		mem.put(MEMLOC_AREA_DECO_START + 1, (byte) id1);
		mem.put(MEMLOC_AREA_DECO_START + 2, (byte) id2);
	}

	public int getLastECL() {
		return mem.get(MEMLOC_LAST_ECL) & 0xFF;
	}

	public void setLastECL(int lastECL) {
		mem.put(MEMLOC_LAST_ECL, (byte) lastECL);
	}

	public int getForLoopCount() {
		return mem.get(MEMLOC_FOR_LOOP_COUNT) & 0xFF;
	}

	public void setForLoopCount(int loopCount) {
		mem.put(MEMLOC_FOR_LOOP_COUNT, (byte) loopCount);
	}

	public int getCombatResult() {
		return mem.get(MEMLOC_COMBAT_RESULT) & 0xFF;
	}

	public void setCombatResult(int combatResult) {
		mem.put(MEMLOC_COMBAT_RESULT, (byte) combatResult);
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
		mem.put(MEMLOC_MAP_ORIENTATION, (byte) currentMapOrient.ordinal());
	}

	public int getWallType() {
		return mem.get(MEMLOC_MAP_WALL_TYPE) & 0xFF;
	}

	public void setWallType(int wallType) {
		mem.put(MEMLOC_MAP_WALL_TYPE, (byte) wallType);
	}

	public int getSquareInfo() {
		return mem.get(MEMLOC_MAP_SQUARE_INFO) & 0xFF;
	}

	public void setSquareInfo(int squareInfo) {
		mem.put(MEMLOC_MAP_SQUARE_INFO, (byte) squareInfo);
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
		if (!a.isMemAddress()) {
			return null;
		}

		mem.position(a.valueAsInt());
		int length = mem.get() & 0xFF;
		ByteBuffer buf = mem.slice().order(ByteOrder.LITTLE_ENDIAN);
		buf.limit(length);
		return new EclString(buf);
	}

	public void writeMemString(EclArgument a, EclString value) {
		if (!a.isMemAddress()) {
			return;
		}

		mem.position(a.valueAsInt());
		mem.put((byte) value.getLength());
		for (int i = 0; i < value.getLength(); i++) {
			mem.put(value.getChar(i));
		}
	}

	public void writeProgram(int startAddress, ByteBuffer eclCode) {
		eclCode.rewind();
		mem.position(startAddress);
		mem.put(eclCode);
	}
}
