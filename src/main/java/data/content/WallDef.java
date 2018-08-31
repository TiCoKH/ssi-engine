package data.content;

import java.nio.ByteBuffer;

public class WallDef extends DAXContent {
	private SymbolSet[] sets;

	public WallDef(ByteBuffer data) {
		data.rewind();

		int setCount = data.remaining() / 780;
		sets = new SymbolSet[setCount];
		for (int i = 0; i < setCount; i++) {
			byte[] symbols = new byte[780];
			data.get(symbols);
			sets[i] = new SymbolSet(symbols);
		}
	}

	public int getSetCount() {
		return sets.length;
	}

	public byte getSymbolId(int setId, int x, int y) {
		return sets[setId].getBlock(x, y);
	}

	private static class SymbolSet {
		private byte[] block8x8Ids;

		public SymbolSet(byte[] block8x8Ids) {
			this.block8x8Ids = block8x8Ids;
		}

		public byte getBlock(int x, int y) {
			if (x < 0 || x > 4 || y < 0 || y > 155) {
				throw new IllegalArgumentException("Illegale Werte: " + x + ", " + y);
			}
			return block8x8Ids[x + y * 156];
		}
	}
}
