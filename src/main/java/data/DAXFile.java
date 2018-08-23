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

public class DAXFile<T extends DAXContent> {
	private Map<Integer, DAXBlock<T>> objects;

	public DAXFile(Map<Integer, DAXBlock<T>> objects) {
		this.objects = objects;
	}

	public static <T extends DAXContent> DAXFile<T> createFrom(FileChannel c, Class<T> clazz) throws IOException {
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

		Map<Integer, DAXBlock<T>> objects = new LinkedHashMap<>();

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

			ByteBuffer data = DAXBlock.uncompress(cmp, sizeRaw);

			try {
				T object = clazz.getConstructor(ByteBuffer.class).newInstance(data);
				objects.put(id, new DAXBlock<T>(id, object));
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				e.printStackTrace(System.err);
			}
		}
		return new DAXFile<>(Collections.unmodifiableMap(objects));
	}

	public T getById(int id) {
		return objects.get(id).getObject();
	}

	public Set<Integer> getIds() {
		return objects.keySet();
	}
}
