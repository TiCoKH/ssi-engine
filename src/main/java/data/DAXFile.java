package data;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;

import common.ByteBufferWrapper;
import data.content.DAXContent;

public class DAXFile extends ContentFile {
	private Map<Integer, ByteBufferWrapper> blocks;

	private DAXFile(Map<Integer, ByteBufferWrapper> blocks) {
		this.blocks = blocks;
	}

	public static DAXFile createFrom(ByteBufferWrapper file) {
		int byteCount = file.getUnsignedShort(0);
		int headerCount = byteCount / 9;

		Map<Integer, ByteBufferWrapper> blocks = new LinkedHashMap<>();

		for (int i = 0; i < headerCount; i++) {
			int headerStart = 2 + (i * 9);
			int id = file.getUnsigned(headerStart);
			int offset = (int) file.getUnsignedInt(headerStart + 1);
			int sizeRaw = file.getUnsignedShort(headerStart + 5);
			int sizeCmp = file.getUnsignedShort(headerStart + 7);

			file.position(2 + byteCount + offset);

			blocks.put(id, uncompress(file.slice().limit(sizeCmp), sizeRaw));
		}
		return new DAXFile(ImmutableSortedMap.copyOf(blocks));
	}

	@Override
	public <T extends DAXContent> T getById(int id, Class<T> clazz) {
		ByteBufferWrapper b = blocks.get(id);
		if (b != null) {
			try {
				return clazz.getConstructor(ByteBufferWrapper.class).newInstance(b.rewind());
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
				e.printStackTrace(System.err);
			}
		}
		return null;
	}

	@Override
	public List<ByteBufferWrapper> getById(int id) {
		return ImmutableList.of(blocks.get(id));
	}

	@Override
	public Set<Integer> getIds() {
		return blocks.keySet();
	}

	private static ByteBufferWrapper uncompress(ByteBufferWrapper compressed, int sizeRaw) {
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
