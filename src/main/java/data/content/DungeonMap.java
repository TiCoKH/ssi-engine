package data.content;

import java.util.EnumMap;
import java.util.Map;

import common.ByteBufferWrapper;

public class DungeonMap extends DAXContent {
	private static final int WALLS_NE_START = 0x000;
	private static final int WALLS_SW_START = 0x100;
	private static final int SQUARE_INFO_START = 0x200;
	private static final int WALLS_FLAGS_START = 0x300;

	private DungeonSquare[][] map;

	public DungeonMap(ByteBufferWrapper data) {
		data.rewind();
		int geoId = data.getUnsignedShort();
		if (geoId != 4353) {
			throw new IllegalArgumentException("data is not a valid geo dungeon map");
		}

		data = data.slice();

		map = new DungeonSquare[16][16];
		for (int y = 0; y < 16; y++) {
			int stride = y << 4;
			for (int x = 0; x < 16; x++) {
				Map<Direction, Integer> wallTypes = new EnumMap<>(Direction.class);
				wallTypes.put(Direction.NORTH, (data.get(WALLS_NE_START + stride + x) & 0xF0) >> 4);
				wallTypes.put(Direction.EAST, data.get(WALLS_NE_START + stride + x) & 0x0F);
				wallTypes.put(Direction.SOUTH, (data.get(WALLS_SW_START + stride + x) & 0xF0) >> 4);
				wallTypes.put(Direction.WEST, data.get(WALLS_SW_START + stride + x) & 0x0F);
				int squareInfo = data.getUnsigned(SQUARE_INFO_START + stride + x);
				Map<Direction, Integer> doorFlags = new EnumMap<>(Direction.class);
				doorFlags.put(Direction.WEST, (data.get(WALLS_FLAGS_START + stride + x) & 0xC0) >> 6);
				doorFlags.put(Direction.SOUTH, (data.get(WALLS_FLAGS_START + stride + x) & 0x30) >> 4);
				doorFlags.put(Direction.EAST, (data.get(WALLS_FLAGS_START + stride + x) & 0x0C) >> 2);
				doorFlags.put(Direction.NORTH, data.get(WALLS_FLAGS_START + stride + x) & 0x03);
				map[x][y] = new DungeonSquare(wallTypes, squareInfo, doorFlags);
			}
		}
	}

	public boolean canMove(int x, int y, Direction d) {
		DungeonSquare square = map[x][y];
		if (square.getWall(d) > 0 && !square.isDoor(d)) {
			return false;
		}
		switch (d) {
			case NORTH:
				return y > 0;
			case EAST:
				return x < 15;
			case SOUTH:
				return y < 15;
			case WEST:
				return x > 0;
		}
		return false;
	}

	public int wallIndexAt(int x, int y, Direction o) {
		return map[x][y].getWall(o);
	}

	public int squareInfoAt(int x, int y) {
		return map[x][y].getSquareInfo();
	}

	private static class DungeonSquare {
		private Map<Direction, Integer> wallTypes;
		private Map<Direction, Integer> doorFlags;
		private int squareInfo;

		private DungeonSquare(Map<Direction, Integer> wallTypes, int squareInfo, Map<Direction, Integer> doorFlags) {
			this.wallTypes = wallTypes;
			this.squareInfo = squareInfo;
			this.doorFlags = doorFlags;
		}

		private int getWall(Direction d) {
			return wallTypes.get(d);
		}

		private int getSquareInfo() {
			return squareInfo;
		}

		private boolean isDoor(Direction d) {
			return doorFlags.get(d) > 0;
		}

		private boolean isBashableDoor(Direction d) {
			return doorFlags.get(d) == 3;
		}
	}

	public enum Direction {
		NORTH(0, -1), EAST(1, 0), SOUTH(0, 1), WEST(-1, 0);
		private int deltaX;
		private int deltaY;

		private Direction(int deltaX, int deltaY) {
			this.deltaX = deltaX;
			this.deltaY = deltaY;
		}

		public Direction getRight() {
			return Direction.values()[(ordinal() + 1) % 4];
		}

		public Direction getReverse() {
			return Direction.values()[(ordinal() + 2) % 4];
		}

		public Direction getLeft() {
			return Direction.values()[(ordinal() + 3) % 4];
		}

		public int getDeltaX() {
			return deltaX;
		}

		public int getDeltaY() {
			return deltaY;
		}

		public static Direction withId(int id) {
			return Direction.values()[id % 4];
		}
	}
}
