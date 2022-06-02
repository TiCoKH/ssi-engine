package data.dungeon;

import static data.dungeon.WallDef.WallDistance.CLOSE;
import static data.dungeon.WallDef.WallDistance.FAR;
import static data.dungeon.WallDef.WallDistance.MEDIUM;
import static data.dungeon.WallDef.WallPlacement.FOWARD;
import static data.dungeon.WallDef.WallPlacement.LEFT;
import static data.dungeon.WallDef.WallPlacement.RIGHT;
import static io.vavr.API.Map;

import javax.annotation.Nonnull;

import io.vavr.collection.Map;

import common.ByteBufferWrapper;
import data.Content;
import data.ContentType;

public class WallDef extends Content {
	private WallDisplay[] walls;

	public WallDef(@Nonnull ByteBufferWrapper data, @Nonnull ContentType type) {
		int setCount = data.remaining() / 156;
		walls = new WallDisplay[setCount];
		for (int i = 0; i < setCount; i++) {
			byte[] symbols = new byte[156];
			data.get(symbols);
			walls[i] = new WallDisplay(symbols);
		}
	}

	public int getWallCount() {
		return walls.length;
	}

	public int[][] getWallDisplay(int wallIndex, WallDistance dis, WallPlacement plc) {
		if (wallIndex < 0 || wallIndex >= walls.length || dis == null || plc == null) {
			throw new IllegalArgumentException("invalid values " + wallIndex + ", " + dis + ", " + plc);
		}
		return walls[wallIndex].getDisplay(dis, plc);
	}

	public int[][] getWallDisplayFarFiller(int wallIndex) {
		if (wallIndex < 0 || wallIndex >= walls.length) {
			throw new IllegalArgumentException("invalid value for wall: " + wallIndex);
		}
		return walls[wallIndex].farFiller;
	}

	public enum WallPlacement {
		FOWARD, LEFT, RIGHT;
	}

	public enum WallDistance {
		FAR, MEDIUM, CLOSE;
	}

	private static class WallDisplay {
		private final Map<WallDistance, Map<WallPlacement, int[][]>> displayMap;

		private final int[][] farFiller;

		public WallDisplay(byte[] symbols) {
			if (symbols.length != 156) {
				throw new IllegalArgumentException("Invalid wall data");
			}

			int[][] farForward = new int[2][1];
			int[][] farLeft = new int[4][1];
			int[][] farRight = new int[4][1];
			int[][] medForward = new int[4][3];
			int[][] medLeft = new int[8][2];
			int[][] medRight = new int[8][2];
			int[][] closeForward = new int[8][7];
			int[][] closeLeft = new int[11][2];
			int[][] closeRight = new int[11][2];
			farFiller = new int[2][1];

			int offset = 0;
			offset = init(symbols, offset, farForward);
			offset = init(symbols, offset, farLeft);
			offset = init(symbols, offset, farRight);
			offset = init(symbols, offset, medForward);
			offset = init(symbols, offset, medLeft);
			offset = init(symbols, offset, medRight);
			offset = init(symbols, offset, closeForward);
			offset = init(symbols, offset, closeLeft);
			offset = init(symbols, offset, closeRight);
			offset = init(symbols, offset, farFiller);

			displayMap = Map( //
				FAR, Map(LEFT, farLeft, FOWARD, farForward, RIGHT, farRight), //
				MEDIUM, Map(LEFT, medLeft, FOWARD, medForward, RIGHT, medRight), //
				CLOSE, Map(LEFT, closeLeft, FOWARD, closeForward, RIGHT, closeRight) //
			);
		}

		private int init(byte[] symbols, int offset, int[][] display) {
			for (int y = 0; y < display.length; y++) {
				int[] row = display[y];
				for (int x = 0; x < row.length; x++) {
					row[x] = symbols[offset++] & 0xFF;
				}
			}
			return offset;
		}

		public int[][] getDisplay(WallDistance dis, WallPlacement plc) {
			return displayMap.get(dis).get().get(plc).get();
		}
	}
}
