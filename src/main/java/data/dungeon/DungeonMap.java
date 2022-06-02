package data.dungeon;

import javax.annotation.Nonnull;

import io.vavr.collection.HashMap;
import io.vavr.collection.Map;

import common.ByteBufferWrapper;
import data.Content;
import data.ContentType;
import data.dungeon.WallDef.WallDistance;
import data.dungeon.WallDef.WallPlacement;

public class DungeonMap extends Content {
	private int height;
	private int width;
	private DungeonSquare[][] map;

	protected DungeonMap(int height, int width) {
		this.height = height;
		this.width = width;
		this.map = new DungeonSquare[width][height];
	}

	public DungeonMap(@Nonnull ByteBufferWrapper data, @Nonnull ContentType type) {
		this(16, 16);

		data.getUnsignedShort(); // GEO ID, different for each game
		data = data.slice();

		readMap(data.slice());
	}

	protected void readMap(@Nonnull ByteBufferWrapper data) {
		final int WALLS_NE_START = 0 * height * width;
		final int WALLS_SW_START = 1 * height * width;
		final int SQUARE_INFO_START = 2 * height * width;
		final int WALLS_FLAGS_START = 3 * height * width;

		for (int y = 0; y < height; y++) {
			int stride = y * width;
			for (int x = 0; x < width; x++) {
				final Map<Direction, Integer> wallTypes = HashMap.of( //
					Direction.NORTH, (data.get(WALLS_NE_START + stride + x) & 0xF0) >> 4, //
					Direction.EAST, data.get(WALLS_NE_START + stride + x) & 0x0F, //
					Direction.SOUTH, (data.get(WALLS_SW_START + stride + x) & 0xF0) >> 4, //
					Direction.WEST, data.get(WALLS_SW_START + stride + x) & 0x0F //
				);
				int squareInfo = data.getUnsigned(SQUARE_INFO_START + stride + x);
				final Map<Direction, Integer> doorFlags = HashMap.of( //
					Direction.WEST, (data.get(WALLS_FLAGS_START + stride + x) & 0xC0) >> 6, //
					Direction.SOUTH, (data.get(WALLS_FLAGS_START + stride + x) & 0x30) >> 4, //
					Direction.EAST, (data.get(WALLS_FLAGS_START + stride + x) & 0x0C) >> 2, //
					Direction.NORTH, (data.get(WALLS_FLAGS_START + stride + x) & 0x03) //
				);
				map[x][y] = new DungeonSquare(wallTypes, squareInfo, doorFlags);
			}
		}
	}

	public boolean canMove(int x, int y, Direction d) {
		DungeonSquare square = map[x][y];
		if (square.getWall(d) > 0 && (!square.isDoor(d) || square.isClosedDoor(d))) {
			return false;
		}
		switch (d) {
			case NORTH:
				return y > 0;
			case EAST:
				return x < width - 1;
			case SOUTH:
				return y < height - 1;
			case WEST:
				return x > 0;
		}
		return false;
	}

	public boolean canOpenDoor(int x, int y, Direction d) {
		if (x < 0 || x >= width || y < 0 || y >= height)
			return false;

		DungeonSquare square = map[x][y];
		return square.isClosedDoor(d);
	}

	public boolean couldExit(int x, int y, Direction d) {
		if (x < 0 || x >= width || y < 0 || y >= height)
			return false;

		DungeonSquare square = map[x][y];
		int newX = x + d.getDeltaX();
		int newY = y + d.getDeltaY();
		return square.isDoor(d) && (newX < 0 || newX >= width || newY < 0 || newY >= height);
	}

	public void visibleWallsAt(@Nonnull VisibleWalls vwalls, int x, int y, @Nonnull Direction d) {
		vwalls.closeLeft[0] = wallIndexAt(x, y, d.getLeft());

		vwalls.closeRight[0] = wallIndexAt(x, y, d.getRight());

		vwalls.closeAhead[0] = //
			wallIndexAt(x + d.getLeft().getDeltaX(), y + d.getLeft().getDeltaY(), d);
		vwalls.closeAhead[1] = wallIndexAt(x, y, d);
		vwalls.closeAhead[2] = //
			wallIndexAt(x + d.getRight().getDeltaX(), y + d.getRight().getDeltaY(), d);

		vwalls.medLeft[0] = //
			wallIndexAt(x + d.getDeltaX() + d.getLeft().getDeltaX(), y + d.getDeltaY() + d.getLeft().getDeltaY(),
				d.getLeft());
		vwalls.medLeft[1] = //
			wallIndexAt(x + d.getDeltaX(), y + d.getDeltaY(), d.getLeft());

		vwalls.medRight[0] = //
			wallIndexAt(x + d.getDeltaX(), y + d.getDeltaY(), d.getRight());
		vwalls.medRight[1] = //
			wallIndexAt(x + d.getDeltaX() + d.getRight().getDeltaX(), y + d.getDeltaY() + d.getRight().getDeltaY(),
				d.getRight());

		vwalls.medAhead[0] = //
			wallIndexAt(x + d.getDeltaX() + 2 * d.getLeft().getDeltaX(),
				y + d.getDeltaY() + 2 * d.getLeft().getDeltaY(), d);
		vwalls.medAhead[1] = //
			wallIndexAt(x + d.getDeltaX() + d.getLeft().getDeltaX(), y + d.getDeltaY() + d.getLeft().getDeltaY(), d);
		vwalls.medAhead[2] = //
			wallIndexAt(x + d.getDeltaX(), y + d.getDeltaY(), d);
		vwalls.medAhead[3] = //
			wallIndexAt(x + d.getDeltaX() + d.getRight().getDeltaX(), y + d.getDeltaY() + d.getRight().getDeltaY(), d);
		vwalls.medAhead[4] = //
			wallIndexAt(x + d.getDeltaX() + 2 * d.getRight().getDeltaX(),
				y + d.getDeltaY() + 2 * d.getRight().getDeltaY(), d);

		vwalls.farLeft[0] = //
			wallIndexAt(x + 2 * d.getDeltaX() + 2 * d.getLeft().getDeltaX(),
				y + 2 * d.getDeltaY() + 2 * d.getLeft().getDeltaY(), d.getLeft());
		vwalls.farLeft[1] = //
			wallIndexAt(x + 2 * d.getDeltaX() + 1 * d.getLeft().getDeltaX(),
				y + 2 * d.getDeltaY() + 1 * d.getLeft().getDeltaY(), d.getLeft());
		vwalls.farLeft[2] = //
			wallIndexAt(x + 2 * d.getDeltaX(), y + 2 * d.getDeltaY(), d.getLeft());

		vwalls.farRight[0] = //
			wallIndexAt(x + 2 * d.getDeltaX(), y + 2 * d.getDeltaY(), d.getRight());
		vwalls.farRight[1] = //
			wallIndexAt(x + 2 * d.getDeltaX() + 1 * d.getRight().getDeltaX(),
				y + 2 * d.getDeltaY() + 1 * d.getRight().getDeltaY(), d.getRight());
		vwalls.farRight[2] = //
			wallIndexAt(x + 2 * d.getDeltaX() + 2 * d.getRight().getDeltaX(),
				y + 2 * d.getDeltaY() + 2 * d.getRight().getDeltaY(), d.getRight());

		vwalls.farAhead[0] = //
			wallIndexAt(x + 2 * d.getDeltaX() + 3 * d.getLeft().getDeltaX(),
				y + 2 * d.getDeltaY() + 3 * d.getLeft().getDeltaY(), d);
		vwalls.farAhead[1] = //
			wallIndexAt(x + 2 * d.getDeltaX() + 2 * d.getLeft().getDeltaX(),
				y + 2 * d.getDeltaY() + 2 * d.getLeft().getDeltaY(), d);
		vwalls.farAhead[2] = //
			wallIndexAt(x + 2 * d.getDeltaX() + 1 * d.getLeft().getDeltaX(),
				y + 2 * d.getDeltaY() + 1 * d.getLeft().getDeltaY(), d);
		vwalls.farAhead[3] = //
			wallIndexAt(x + 2 * d.getDeltaX(), y + 2 * d.getDeltaY(), d);
		vwalls.farAhead[4] = //
			wallIndexAt(x + 2 * d.getDeltaX() + 1 * d.getRight().getDeltaX(),
				y + 2 * d.getDeltaY() + 1 * d.getRight().getDeltaY(), d);
		vwalls.farAhead[5] = //
			wallIndexAt(x + 2 * d.getDeltaX() + 2 * d.getRight().getDeltaX(),
				y + 2 * d.getDeltaY() + 2 * d.getRight().getDeltaY(), d);
		vwalls.farAhead[6] = //
			wallIndexAt(x + 2 * d.getDeltaX() + 3 * d.getRight().getDeltaX(),
				y + 2 * d.getDeltaY() + 3 * d.getRight().getDeltaY(), d);
	}

	public int wallIndexAt(int x, int y, Direction d) {
		if (x < 0 || x >= width || y < 0 || y >= height)
			return 0;
		return map[x][y].getWall(d);
	}

	public int doorFlagsAt(int x, int y, Direction d) {
		if (x < 0 || x >= width || y < 0 || y >= height)
			return 0;
		return map[x][y].getDoorFlags(d);
	}

	public int squareInfoAt(int x, int y) {
		if (x < 0 || x >= width || y < 0 || y >= height)
			return 0;
		return map[x][y].getSquareInfo();
	}

	public void openDoor(int x, int y, Direction d) {
		if (x < 0 || x >= width || y < 0 || y >= height)
			return;

		DungeonSquare square = map[x][y];
		if (square.isClosedDoor(d)) {
			square.openDoor(d);
			DungeonSquare squareRev = map[x + d.getDeltaX()][y + d.getDeltaY()];
			squareRev.openDoor(d.getReverse());
		}
	}

	public int[][] generateWallMap() {
		int[][] result = new int[height][width];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				result[y][x] = 0;
				if (wallIndexAt(x, y, Direction.NORTH) > 0)
					result[y][x] += 1;
				if (wallIndexAt(x, y, Direction.EAST) > 0)
					result[y][x] += 2;
				if (wallIndexAt(x, y, Direction.SOUTH) > 0)
					result[y][x] += 4;
				if (wallIndexAt(x, y, Direction.WEST) > 0)
					result[y][x] += 8;
			}
		}
		return result;
	}

	public int[][] generateOverlandMap() {
		int[][] result = new int[height][width];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				result[y][x] = wallIndexAt(x, y, Direction.NORTH) * 16 + wallIndexAt(x, y, Direction.EAST);
			}
		}
		return result;
	}

	private static class DungeonSquare {
		private final Map<Direction, Integer> wallTypes;
		private Map<Direction, Integer> doorFlags;
		private int squareInfo;

		private DungeonSquare(Map<Direction, Integer> wallTypes, int squareInfo, Map<Direction, Integer> doorFlags) {
			this.wallTypes = wallTypes;
			this.squareInfo = squareInfo;
			this.doorFlags = doorFlags;
		}

		private int getWall(Direction d) {
			return wallTypes.get(d).getOrNull();
		}

		private int getSquareInfo() {
			return squareInfo;
		}

		private int getDoorFlags(Direction d) {
			return doorFlags.get(d).getOrNull();
		}

		private boolean isDoor(Direction d) {
			return doorFlags.get(d).getOrNull() > 0;
		}

		private boolean isClosedDoor(Direction d) {
			return doorFlags.get(d).getOrNull() > 1;
		}

		private boolean isBashableDoor(Direction d) {
			return doorFlags.get(d).getOrNull() == 2;
		}

		private void openDoor(Direction d) {
			doorFlags = doorFlags.put(d, 1);
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
