package data;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

import java.nio.ByteBuffer;

import data.content.DAXContent;

public class DAXBlock<T extends DAXContent> {
	private int id;
	private T object;

	public DAXBlock(int id, T object) {
		this.id = id;
		this.object = object;
	}

	public int getId() {
		return id;
	}

	public T getObject() {
		return object;
	}

	public static ByteBuffer uncompress(ByteBuffer compressed, int sizeRaw) {
		ByteBuffer result = ByteBuffer.allocate(sizeRaw).order(LITTLE_ENDIAN);

		int in = 0;
		int out = 0;
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
