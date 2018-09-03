package data.content;

import java.nio.ByteBuffer;

public class DungeonMap extends DAXContent {
	private static final int WALLS_NE_START = 0x000;
	private static final int WALLS_SW_START = 0x100;
	private static final int WALLS_X2_START = 0x200;
	private static final int WALLS_X3_START = 0x300;

	public static final int WALL_NORTH_CLOSE = 0;
	public static final int WALL_EAST_CLOSE = 1;
	public static final int WALL_SOUTH_CLOSE = 2;
	public static final int WALL_WEST_CLOSE = 3;
	public static final int WALL_NORTH_FAR = 5;
	public static final int WALL_EAST_FAR = 6;
	public static final int WALL_SOUTH_FAR = 7;
	public static final int WALL_WEST_FAR = 8;

	private DungeonSquare[][] map;

	public DungeonMap(ByteBuffer data) {
		data.rewind();
		int geoId = data.getShort() & 0xFFFF;
		if (geoId != 4353) {
			throw new IllegalArgumentException("data is not a valid geo dungeon map");
		}

		map = new DungeonSquare[16][16];
		for (int y = 0; y < 16; y++) {
			int stride = y << 4;
			for (int x = 0; x < 16; x++) {
				int[] walls = new int[9];
				walls[0] = (data.get(WALLS_NE_START + stride + x) & 0xF0) >> 4;
				walls[1] = (data.get(WALLS_NE_START + stride + x) & 0x0F);
				walls[2] = (data.get(WALLS_SW_START + stride + x) & 0xF0) >> 4;
				walls[3] = (data.get(WALLS_SW_START + stride + x) & 0x0F);
				walls[4] = (data.get(WALLS_X2_START + stride + x) & 0xFF);
				walls[5] = (data.get(WALLS_X3_START + stride + x) & 0xC0) >> 6;
				walls[6] = (data.get(WALLS_X3_START + stride + x) & 0x30) >> 4;
				walls[7] = (data.get(WALLS_X3_START + stride + x) & 0x0C) >> 2;
				walls[8] = (data.get(WALLS_X3_START + stride + x) & 0x03);
				map[x][y] = new DungeonSquare(walls);
			}
		}
	}

	public int getWall(int x, int y, int type) {
		return map[x][y].getWall(type);
	}

	private static class DungeonSquare {
		private int[] walls;

		private DungeonSquare(int[] walls) {
			this.walls = walls;
		}

		private int getWall(int type) {
			return walls[type];
		}
	}
}
