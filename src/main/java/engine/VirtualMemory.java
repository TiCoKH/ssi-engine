package engine;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import data.content.DungeonMap.Direction;
import engine.opcodes.EclArgument;
import engine.opcodes.EclString;

public class VirtualMemory {
	private ByteBuffer mem;
	private int currentMapX;
	private int currentMapY;
	private Direction currentMapOrient;

	public VirtualMemory() {
		mem = ByteBuffer.allocate(0x10000).order(ByteOrder.LITTLE_ENDIAN);
		currentMapX = 0;
		currentMapY = 0;
		currentMapOrient = null;
	}

	public int getCurrentMapX() {
		return currentMapX;
	}

	public void setCurrentMapX(int currentMapX) {
		this.currentMapX = currentMapX;
	}

	public int getCurrentMapY() {
		return currentMapY;
	}

	public void setCurrentMapY(int currentMapY) {
		this.currentMapY = currentMapY;
	}

	public Direction getCurrentMapOrient() {
		return currentMapOrient;
	}

	public void setCurrentMapOrient(Direction currentMapOrient) {
		this.currentMapOrient = currentMapOrient;
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

	public EclString readMemString(EclArgument a) {
		return null;
	}

	public void writeMemString(EclArgument a, EclString value) {

	}

}
