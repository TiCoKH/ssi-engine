package data.content;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.EnumMap;
import java.util.Map;

public class DungeonMap extends DAXContent {
	private static final int WALLS_NE_START = 0x000;
	private static final int WALLS_SW_START = 0x100;
	private static final int BACKDROP_START = 0x200;
	private static final int WALLS_FLAGS_START = 0x300;

	private DungeonSquare[][] map;

	public DungeonMap(ByteBuffer data) {
		data.rewind();
		int geoId = data.getShort() & 0xFFFF;
		if (geoId != 4353) {
			throw new IllegalArgumentException("data is not a valid geo dungeon map");
		}

		data = data.slice().order(ByteOrder.LITTLE_ENDIAN);

		map = new DungeonSquare[16][16];
		for (int y = 0; y < 16; y++) {
			int stride = y << 4;
			for (int x = 0; x < 16; x++) {
				Map<Direction, Integer> wallTypes = new EnumMap<>(Direction.class);
				wallTypes.put(Direction.NORTH, (data.get(WALLS_NE_START + stride + x) & 0xF0) >> 4);
				wallTypes.put(Direction.EAST, data.get(WALLS_NE_START + stride + x) & 0x0F);
				wallTypes.put(Direction.SOUTH, (data.get(WALLS_SW_START + stride + x) & 0xF0) >> 4);
				wallTypes.put(Direction.WEST, data.get(WALLS_SW_START + stride + x) & 0x0F);
				int backdrop = data.get(BACKDROP_START + stride + x) & 0xFF;
				Map<Direction, Integer> doorFlags = new EnumMap<>(Direction.class);
				doorFlags.put(Direction.WEST, (data.get(WALLS_FLAGS_START + stride + x) & 0xC0) >> 6);
				doorFlags.put(Direction.SOUTH, (data.get(WALLS_FLAGS_START + stride + x) & 0x30) >> 4);
				doorFlags.put(Direction.EAST, (data.get(WALLS_FLAGS_START + stride + x) & 0x0C) >> 2);
				doorFlags.put(Direction.NORTH, data.get(WALLS_FLAGS_START + stride + x) & 0x03);
				map[x][y] = new DungeonSquare(wallTypes, backdrop, doorFlags);
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

	private static class DungeonSquare {
		private Map<Direction, Integer> wallTypes;
		private Map<Direction, Integer> doorFlags;
		private int backdrop;

		private DungeonSquare(Map<Direction, Integer> wallTypes, int backdrop, Map<Direction, Integer> doorFlags) {
			this.wallTypes = wallTypes;
			this.backdrop = backdrop;
			this.doorFlags = doorFlags;
		}

		private int getWall(Direction d) {
			return wallTypes.get(d);
		}

		private int getBackdrop() {
			return backdrop;
		}

		private boolean isDoor(Direction d) {
			return doorFlags.get(d) > 0;
		}

		private boolean isBashableDoor(Direction d) {
			return doorFlags.get(d) == 3;
		}
	}

	public enum Direction {
		NORTH(0, 0, -1), EAST(2, 1, 0), SOUTH(4, 0, 1), WEST(6, -1, 0);
		private int id;
		private int deltaX;
		private int deltaY;

		private Direction(int id, int deltaX, int deltaY) {
			this.id = id;
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

		public int getId() {
			return id;
		}

		public int getDeltaX() {
			return deltaX;
		}

		public int getDeltaY() {
			return deltaY;
		}

		public static Direction withId(int id) {
			return Direction.values()[id >> 1];
		}
	}
}
