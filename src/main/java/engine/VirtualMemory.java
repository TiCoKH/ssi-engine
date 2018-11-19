package engine;

import java.io.IOException;
import java.nio.channels.FileChannel;

import common.ByteBufferWrapper;
import data.content.DungeonMap.Direction;
import engine.opcodes.EclArgument;
import engine.opcodes.EclString;

public class VirtualMemory implements ViewDungeonPosition {
	public static final int MEMLOC_CURRENT_ECL = 0x0000;
	public static final int MEMLOC_AREA_START = 0x0001;
	public static final int MEMLOC_AREA_DECO_START = 0x0004;
	public static final int MEMLOC_ENGINE_CONF_IS_DUNGEON = 0x4BAB;
	public static final int MEMLOC_LAST_DUNGEON_X = 0x4BF0;
	public static final int MEMLOC_LAST_DUNGEON_Y = 0x4BF1;
	public static final int MEMLOC_LAST_ECL = 0x4BF2;
	public static final int MEMLOC_ENGINE_CONF_GAME_SPEED = 0x4BFC;
	public static final int MEMLOC_FOR_LOOP_COUNT = 0x4CF6;
	public static final int MEMLOC_EXTENDED_DUNGEON_X = 0x4CFD;
	public static final int MEMLOC_EXTENDED_DUNGEON_Y = 0x4CFE;
	public static final int MEMLOC_COMBAT_RESULT = 0x7EC7;
	public static final int MEMLOC_MOVEMENT_BLOCK = 0x7EC9;
	public static final int MEMLOC_TRIED_TO_LEAVE_MAP = 0x7ED5;
	public static final int MEMLOC_DUNGEON_X = 0xC04B;
	public static final int MEMLOC_DUNGEON_Y = 0xC04C;
	public static final int MEMLOC_DUNGEON_DIR = 0xC04D;
	public static final int MEMLOC_MAP_WALL_TYPE = 0xC04E;
	public static final int MEMLOC_MAP_SQUARE_INFO = 0xC04F;

	private ByteBufferWrapper mem;

	private int menuChoice;

	public VirtualMemory() {
		mem = ByteBufferWrapper.allocateLE(0x10000);
	}

	public void loadFrom(FileChannel fc) throws IOException {
		try {
			mem.position(0).readFrom(fc);
		} finally {
			fc.close();
		}
	}

	public void saveTo(FileChannel fc) throws IOException {
		try {
			mem.position(0).writeTo(fc);
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
		return mem.getUnsigned(MEMLOC_CURRENT_ECL);
	}

	public void setCurrentECL(int currentECL) {
		mem.put(MEMLOC_CURRENT_ECL, (byte) currentECL);
	}

	public int getAreaValue(int id) {
		return mem.getUnsigned(MEMLOC_AREA_START + id);
	}

	public void setAreaValues(int id0, int id1, int id2) {
		mem.put(MEMLOC_AREA_START, (byte) id0);
		mem.put(MEMLOC_AREA_START + 1, (byte) id1);
		mem.put(MEMLOC_AREA_START + 2, (byte) id2);
	}

	public int getAreaDecoValue(int id) {
		return mem.getUnsigned(MEMLOC_AREA_DECO_START + id);
	}

	public void setAreaDecoValues(int id0, int id1, int id2) {
		mem.put(MEMLOC_AREA_DECO_START, (byte) id0);
		mem.put(MEMLOC_AREA_DECO_START + 1, (byte) id1);
		mem.put(MEMLOC_AREA_DECO_START + 2, (byte) id2);
	}

	public boolean getIsDungeon() {
		return mem.getUnsigned(MEMLOC_ENGINE_CONF_IS_DUNGEON) > 0;
	}

	public void setIsDungeon(boolean isDungeon) {
		mem.put(MEMLOC_ENGINE_CONF_IS_DUNGEON, (byte) (isDungeon ? 1 : 0));
	}

	public int getLastDungeonX() {
		return mem.getUnsigned(MEMLOC_LAST_DUNGEON_X);
	}

	public void setLastDungeonX(int lastDungeonX) {
		mem.put(MEMLOC_LAST_DUNGEON_X, (byte) lastDungeonX);
	}

	public int getLastDungeonY() {
		return mem.getUnsigned(MEMLOC_LAST_DUNGEON_Y);
	}

	public void setLastDungeonY(int lastDungeonY) {
		mem.put(MEMLOC_LAST_DUNGEON_Y, (byte) lastDungeonY);
	}

	public int getLastECL() {
		return mem.getUnsigned(MEMLOC_LAST_ECL);
	}

	public void setLastECL(int lastECL) {
		mem.put(MEMLOC_LAST_ECL, (byte) lastECL);
	}

	public int getGameSpeed() {
		return mem.getUnsigned(MEMLOC_ENGINE_CONF_GAME_SPEED);
	}

	public void setGameSpeed(int gameSpeed) {
		mem.put(MEMLOC_ENGINE_CONF_GAME_SPEED, (byte) gameSpeed);
	}

	public int getForLoopCount() {
		return mem.getUnsigned(MEMLOC_FOR_LOOP_COUNT);
	}

	public void setForLoopCount(int loopCount) {
		mem.put(MEMLOC_FOR_LOOP_COUNT, (byte) loopCount);
	}

	@Override
	public int getExtendedDungeonX() {
		return mem.getUnsigned(MEMLOC_EXTENDED_DUNGEON_X);
	}

	public void setExtendedDungeonX(int extendedDungeonX) {
		mem.put(MEMLOC_EXTENDED_DUNGEON_X, (byte) extendedDungeonX);
	}

	@Override
	public int getExtendedDungeonY() {
		return mem.getUnsigned(MEMLOC_EXTENDED_DUNGEON_Y);
	}

	public void setExtendedDungeonY(int extendedDungeonY) {
		mem.put(MEMLOC_EXTENDED_DUNGEON_Y, (byte) extendedDungeonY);
	}

	public int getCombatResult() {
		return mem.getUnsigned(MEMLOC_COMBAT_RESULT);
	}

	public void setCombatResult(int combatResult) {
		mem.put(MEMLOC_COMBAT_RESULT, (byte) combatResult);
	}

	public int getMovementBlock() {
		return mem.getUnsigned(MEMLOC_MOVEMENT_BLOCK);
	}

	public void setMovementBlock(int movementBlock) {
		mem.put(MEMLOC_MOVEMENT_BLOCK, (byte) movementBlock);
	}

	public boolean getTriedToLeaveMap() {
		return mem.getUnsigned(MEMLOC_TRIED_TO_LEAVE_MAP) > 0;
	}

	public void setTriedToLeaveMap(boolean triedToLeaveMap) {
		mem.put(MEMLOC_TRIED_TO_LEAVE_MAP, (byte) (triedToLeaveMap ? 1 : 0));
	}

	@Override
	public int getDungeonX() {
		return mem.getUnsigned(MEMLOC_DUNGEON_X);
	}

	public void setDungeonX(int dungeonX) {
		mem.put(MEMLOC_DUNGEON_X, (byte) dungeonX);
	}

	@Override
	public int getDungeonY() {
		return mem.getUnsigned(MEMLOC_DUNGEON_Y);
	}

	public void setDungeonY(int dungeonY) {
		mem.put(MEMLOC_DUNGEON_Y, (byte) dungeonY);
	}

	@Override
	public Direction getDungeonDir() {
		return Direction.withId(mem.getUnsigned(MEMLOC_DUNGEON_DIR));
	}

	public void setDungeonDir(Direction dungeonDir) {
		mem.put(MEMLOC_DUNGEON_DIR, (byte) dungeonDir.ordinal());
	}

	public int getWallType() {
		return mem.getUnsigned(MEMLOC_MAP_WALL_TYPE);
	}

	public void setWallType(int wallType) {
		mem.put(MEMLOC_MAP_WALL_TYPE, (byte) wallType);
	}

	public int getSquareInfo() {
		return mem.getUnsigned(MEMLOC_MAP_SQUARE_INFO);
	}

	public void setSquareInfo(int squareInfo) {
		mem.put(MEMLOC_MAP_SQUARE_INFO, (byte) squareInfo);
	}

	@Override
	public int getBackdropIndex() {
		return (getSquareInfo() & 0x80) >> 7;
	}

	public int readMemInt(EclArgument a) {
		if (!a.isMemAddress()) {
			return 0;
		}
		return a.isShortValue() ? mem.getUnsignedShort(a.valueAsInt()) : mem.getUnsigned(a.valueAsInt());
	}

	public int readMemInt(EclArgument base, int offset) {
		if (!base.isMemAddress()) {
			return 0;
		}
		return base.isShortValue() ? mem.getUnsignedShort(base.valueAsInt() + offset) : mem.getUnsigned(base.valueAsInt() + offset);
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
		int length = mem.getUnsigned();
		ByteBufferWrapper buf = mem.slice().limit(length);
		return new EclString(buf);
	}

	public void writeMemString(EclArgument a, EclString value) {
		if (!a.isMemAddress()) {
			return;
		}

		mem.position(a.valueAsInt()).put((byte) value.getLength());
		for (int i = 0; i < value.getLength(); i++) {
			mem.put(value.getChar(i));
		}
	}

	public void writeProgram(int startAddress, ByteBufferWrapper eclCode) {
		eclCode.rewind();
		mem.position(startAddress).put(eclCode);
	}
}
