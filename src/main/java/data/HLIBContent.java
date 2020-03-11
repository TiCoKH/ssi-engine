package data;

import javax.annotation.Nonnull;

import common.ByteBufferWrapper;

public interface HLIBContent {

	static ByteBufferWrapper uncompress(@Nonnull ByteBufferWrapper compressed, int sizeRaw) {
		ByteBufferWrapper result = ByteBufferWrapper.allocateLE(sizeRaw);

		int in = 0;
		int out = 0;
		compressed.rewind();
		while (in < compressed.limit() && out < result.limit()) {
			byte next = compressed.get(in++);
			int count = Math.abs(next);
			if (next >= 0) {
				for (int i = 0; i <= count; i++) {
					result.put(out++, compressed.get(in++));
				}
			} else {
				byte repeat = compressed.get(in++);
				for (int i = 0; i <= count; i++) {
					result.put(out++, repeat);
				}
			}
		}
		return result;
	}

}
