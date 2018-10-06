package data;

import common.ByteBufferWrapper;

public class DAXBlock {
	private int id;
	private ByteBufferWrapper data;
	private int sizeRaw;

	public DAXBlock(int id, ByteBufferWrapper data, int sizeRaw) {
		this.id = id;
		this.data = data;
		this.sizeRaw = sizeRaw;
	}

	public int getId() {
		return id;
	}

	public ByteBufferWrapper getData() {
		return data;
	}

	public ByteBufferWrapper getUncompressed() {
		return uncompress(data, sizeRaw);
	}

	public int getSizeRaw() {
		return sizeRaw;
	}

	public static ByteBufferWrapper uncompress(ByteBufferWrapper compressed, int sizeRaw) {
		ByteBufferWrapper result = ByteBufferWrapper.allocateLE(sizeRaw);

		int in = 0;
		int out = 0;
		compressed.rewind();
		while (in < compressed.limit()) {
			byte next = compressed.get(in++);
			int count = Math.abs(next);
			if (next >= 0) {
				for (int i = 0; i < 1 + count; i++) {
					result.put(out++, compressed.get(in++));
				}
			} else {
				byte repeat = compressed.get(in++);
				for (int i = 0; i < count; i++) {
					result.put(out++, repeat);
				}
			}
		}
		return result;
	}
}
