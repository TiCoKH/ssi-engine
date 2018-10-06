package data;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import common.ByteBufferWrapper;
import data.content.DAXContent;

public class DAXFile {
	private Map<Integer, DAXBlock> blocks;

	private DAXFile(Map<Integer, DAXBlock> blocks) {
		this.blocks = blocks;
	}

	public static DAXFile createFrom(FileChannel c) throws IOException {
		if (!c.isOpen()) {
			return null;
		}

		ByteBufferWrapper file = ByteBufferWrapper.allocateLE((int) c.size());
		try {
			file.readFrom(c);
		} finally {
			c.close();
		}

		int byteCount = file.getUnsignedShort(0);
		int headerCount = byteCount / 9;

		Map<Integer, DAXBlock> blocks = new LinkedHashMap<>();

		for (int i = 0; i < headerCount; i++) {
			int headerStart = 2 + (i * 9);
			int id = file.getUnsigned(headerStart);
			int offset = (int) file.getUnsignedInt(headerStart + 1);
			int sizeRaw = file.getUnsignedShort(headerStart + 5);
			int sizeCmp = file.getUnsignedShort(headerStart + 7);

			file.position(2 + byteCount + offset);
			ByteBufferWrapper cmp = file.slice().limit(sizeCmp);

			blocks.put(id, new DAXBlock(id, cmp, sizeRaw));
		}
		return new DAXFile(Collections.unmodifiableMap(blocks));
	}

	public <T extends DAXContent> T getById(int id, Class<T> clazz) {
		DAXBlock b = blocks.get(id);
		if (b != null) {
			try {
				return clazz.getConstructor(ByteBufferWrapper.class).newInstance(b.getUncompressed());
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
				e.printStackTrace(System.err);
			}
		}
		return null;
	}

	public DAXBlock getById(int id) {
		return blocks.get(id);
	}

	public Set<Integer> getIds() {
		return blocks.keySet();
	}
}
