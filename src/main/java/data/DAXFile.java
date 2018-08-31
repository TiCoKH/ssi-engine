package data;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

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

		ByteBuffer file = ByteBuffer.allocate((int) c.size()).order(ByteOrder.LITTLE_ENDIAN);
		try {
			c.read(file);
		} finally {
			c.close();
		}

		short byteCount = file.getShort(0);
		int headerCount = byteCount / 9;

		Map<Integer, DAXBlock> blocks = new LinkedHashMap<>();

		for (int i = 0; i < headerCount; i++) {
			int headerStart = 2 + (i * 9);
			int id = file.get(headerStart) & 0xFF;
			int offset = file.getInt(headerStart + 1);
			int sizeRaw = file.getShort(headerStart + 5) & 0xFFFF;
			int sizeCmp = file.getShort(headerStart + 7) & 0xFFFF;

			file.position(2 + byteCount + offset);
			ByteBuffer cmp = file.slice();
			cmp.order(ByteOrder.LITTLE_ENDIAN);
			cmp.limit(sizeCmp);

			blocks.put(id, new DAXBlock(id, cmp, sizeRaw));
		}
		return new DAXFile(Collections.unmodifiableMap(blocks));
	}

	public <T extends DAXContent> T getById(int id, Class<T> clazz) {
		DAXBlock b = blocks.get(id);
		if (b != null) {
			try {
				return clazz.getConstructor(ByteBuffer.class).newInstance(b.getUncompressed());
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
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
