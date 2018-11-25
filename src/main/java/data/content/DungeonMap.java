package data.content;

import java.util.EnumMap;
import java.util.Map;

import javax.annotation.Nonnull;

import common.ByteBufferWrapper;
import data.content.WallDef.WallDistance;
import data.content.WallDef.WallPlacement;

public class DungeonMap extends DAXContent {
	private static final int WALLS_NE_START = 0x000;
	private static final int WALLS_SW_START = 0x100;
	private static final int SQUARE_INFO_START = 0x200;
	private static final int WALLS_FLAGS_START = 0x300;

	private DungeonSquare[][] map;

	public DungeonMap(@Nonnull ByteBufferWrapper data, @Nonnull DAXContentType type) {
		data.getUnsignedShort(); // GEO ID, different for each game
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

	public boolean couldExit(int x, int y, Direction d) {
		DungeonSquare square = map[x][y];
		int newX = x + d.getDeltaX();
		int newY = y + d.getDeltaY();
		return square.isDoor(d) && (newX < 0 || newX > 15 || newY < 0 || newY > 15);
	}

	public void visibleWallsAt(@Nonnull VisibleWalls vwalls, int x, int y, @Nonnull Direction o) {
		vwalls.closeLeft[0] = wallIndexAt(x, y, o.getLeft());

		vwalls.closeRight[0] = wallIndexAt(x, y, o.getRight());

		vwalls.closeAhead[0] = //
			wallIndexAt(x + o.getLeft().getDeltaX(), y + o.getLeft().getDeltaY(), o);
		vwalls.closeAhead[1] = wallIndexAt(x, y, o);
		vwalls.closeAhead[2] = //
			wallIndexAt(x + o.getRight().getDeltaX(), y + o.getRight().getDeltaY(), o);

		vwalls.medLeft[0] = //
			wallIndexAt(x + o.getDeltaX() + o.getLeft().getDeltaX(), y + o.getDeltaY() + o.getLeft().getDeltaY(), o.getLeft());
		vwalls.medLeft[1] = //
			wallIndexAt(x + o.getDeltaX(), y + o.getDeltaY(), o.getLeft());

		vwalls.medRight[0] = //
			wallIndexAt(x + o.getDeltaX(), y + o.getDeltaY(), o.getRight());
		vwalls.medRight[1] = //
			wallIndexAt(x + o.getDeltaX() + o.getRight().getDeltaX(), y + o.getDeltaY() + o.getRight().getDeltaY(), o.getRight());

		vwalls.medAhead[0] = //
			wallIndexAt(x + o.getDeltaX() + 2 * o.getLeft().getDeltaX(), y + o.getDeltaY() + 2 * o.getLeft().getDeltaY(), o);
		vwalls.medAhead[1] = //
			wallIndexAt(x + o.getDeltaX() + o.getLeft().getDeltaX(), y + o.getDeltaY() + o.getLeft().getDeltaY(), o);
		vwalls.medAhead[2] = //
			wallIndexAt(x + o.getDeltaX(), y + o.getDeltaY(), o);
		vwalls.medAhead[3] = //
			wallIndexAt(x + o.getDeltaX() + o.getRight().getDeltaX(), y + o.getDeltaY() + o.getRight().getDeltaY(), o);
		vwalls.medAhead[4] = //
			wallIndexAt(x + o.getDeltaX() + 2 * o.getRight().getDeltaX(), y + o.getDeltaY() + 2 * o.getRight().getDeltaY(), o);

		vwalls.farLeft[0] = //
			wallIndexAt(x + 2 * o.getDeltaX() + 2 * o.getLeft().getDeltaX(), y + 2 * o.getDeltaY() + 2 * o.getLeft().getDeltaY(), o.getLeft());
		vwalls.farLeft[1] = //
			wallIndexAt(x + 2 * o.getDeltaX() + 1 * o.getLeft().getDeltaX(), y + 2 * o.getDeltaY() + 1 * o.getLeft().getDeltaY(), o.getLeft());
		vwalls.farLeft[2] = //
			wallIndexAt(x + 2 * o.getDeltaX(), y + 2 * o.getDeltaY(), o.getLeft());

		vwalls.farRight[0] = //
			wallIndexAt(x + 2 * o.getDeltaX(), y + 2 * o.getDeltaY(), o.getRight());
		vwalls.farRight[1] = //
			wallIndexAt(x + 2 * o.getDeltaX() + 1 * o.getRight().getDeltaX(), y + 2 * o.getDeltaY() + 1 * o.getRight().getDeltaY(), o.getRight());
		vwalls.farRight[2] = //
			wallIndexAt(x + 2 * o.getDeltaX() + 2 * o.getRight().getDeltaX(), y + 2 * o.getDeltaY() + 2 * o.getRight().getDeltaY(), o.getRight());

		vwalls.farAhead[0] = //
			wallIndexAt(x + 2 * o.getDeltaX() + 3 * o.getLeft().getDeltaX(), y + 2 * o.getDeltaY() + 3 * o.getLeft().getDeltaY(), o);
		vwalls.farAhead[1] = //
			wallIndexAt(x + 2 * o.getDeltaX() + 2 * o.getLeft().getDeltaX(), y + 2 * o.getDeltaY() + 2 * o.getLeft().getDeltaY(), o);
		vwalls.farAhead[2] = //
			wallIndexAt(x + 2 * o.getDeltaX() + 1 * o.getLeft().getDeltaX(), y + 2 * o.getDeltaY() + 1 * o.getLeft().getDeltaY(), o);
		vwalls.farAhead[3] = //
			wallIndexAt(x + 2 * o.getDeltaX(), y + 2 * o.getDeltaY(), o);
		vwalls.farAhead[4] = //
			wallIndexAt(x + 2 * o.getDeltaX() + 1 * o.getRight().getDeltaX(), y + 2 * o.getDeltaY() + 1 * o.getRight().getDeltaY(), o);
		vwalls.farAhead[5] = //
			wallIndexAt(x + 2 * o.getDeltaX() + 2 * o.getRight().getDeltaX(), y + 2 * o.getDeltaY() + 2 * o.getRight().getDeltaY(), o);
		vwalls.farAhead[6] = //
			wallIndexAt(x + 2 * o.getDeltaX() + 3 * o.getRight().getDeltaX(), y + 2 * o.getDeltaY() + 3 * o.getRight().getDeltaY(), o);
	}

	public int wallIndexAt(int x, int y, Direction o) {
		if (x < 0 || x > 15 || y < 0 || y > 15)
			return 0;
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

	public static class VisibleWalls {
		private final int[] farAhead = new int[7];
		private final int[] farLeft = new int[3];
		private final int[] farRight = new int[3];

		private final int[] medAhead = new int[5];
		private final int[] medLeft = new int[2];
		private final int[] medRight = new int[2];

		private final int[] closeAhead = new int[3];
		private final int[] closeLeft = new int[1];
		private final int[] closeRight = new int[1];

		public int[] getVisibleWall(WallDistance dis, WallPlacement plc) {
			switch (dis) {
				case CLOSE:
					switch (plc) {
						case FOWARD:
							return closeAhead;
						case LEFT:
							return closeLeft;
						case RIGHT:
							return closeRight;
					}
				case MEDIUM:
					switch (plc) {
						case FOWARD:
							return medAhead;
						case LEFT:
							return medLeft;
						case RIGHT:
							return medRight;
					}
				case FAR:
					switch (plc) {
						case FOWARD:
							return farAhead;
						case LEFT:
							return farLeft;
						case RIGHT:
							return farRight;
					}
			}
			return null;
		}
	}
}
