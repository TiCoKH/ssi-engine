package data;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

import java.nio.ByteBuffer;

public class DAXBlock {
	private int id;
	private ByteBuffer data;
	private int sizeRaw;

	public DAXBlock(int id, ByteBuffer data, int sizeRaw) {
		this.id = id;
		this.data = data;
		this.sizeRaw = sizeRaw;
	}

	public int getId() {
		return id;
	}

	public ByteBuffer getData() {
		return data;
	}

	public ByteBuffer getUncompressed() {
		return uncompress(data, sizeRaw);
	}

	public int getSizeRaw() {
		return sizeRaw;
	}

	public static ByteBuffer uncompress(ByteBuffer compressed, int sizeRaw) {
		ByteBuffer result = ByteBuffer.allocate(sizeRaw).order(LITTLE_ENDIAN);

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
