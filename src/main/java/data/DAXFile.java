package data;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

import data.content.DAXContent;

public class DAXFile<T extends DAXContent> implements Iterable<DAXBlock<T>> {
	private List<DAXBlock<T>> objects;

	public DAXFile(List<DAXBlock<T>> objects) {
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

		List<DAXBlock<T>> objects = new ArrayList<>();

		for (int i = 0; i < headerCount; i++) {
			int headerStart = 2 + (i * 9);
			byte id = file.get(headerStart);
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
				objects.add(new DAXBlock<T>(id, object));
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				e.printStackTrace(System.err);
			}
		}
		return new DAXFile<>(objects);
	}

	public boolean contains(Object o) {
		return objects.contains(o);
	}

	public boolean containsAll(Collection<?> c) {
		return objects.containsAll(c);
	}

	public void forEach(Consumer<? super DAXBlock<T>> action) {
		objects.forEach(action);
	}

	public DAXBlock<T> get(int index) {
		return objects.get(index);
	}

	public int indexOf(Object o) {
		return objects.indexOf(o);
	}

	public boolean isEmpty() {
		return objects.isEmpty();
	}

	public Iterator<DAXBlock<T>> iterator() {
		return objects.iterator();
	}

	public int lastIndexOf(Object o) {
		return objects.lastIndexOf(o);
	}

	public ListIterator<DAXBlock<T>> listIterator() {
		return objects.listIterator();
	}

	public ListIterator<DAXBlock<T>> listIterator(int index) {
		return objects.listIterator(index);
	}

	public Stream<DAXBlock<T>> parallelStream() {
		return objects.parallelStream();
	}

	public int size() {
		return objects.size();
	}

	public Spliterator<DAXBlock<T>> spliterator() {
		return objects.spliterator();
	}

	public Stream<DAXBlock<T>> stream() {
		return objects.stream();
	}

	public List<DAXBlock<T>> subList(int fromIndex, int toIndex) {
		return objects.subList(fromIndex, toIndex);
	}

	public Object[] toArray() {
		return objects.toArray();
	}

	public <T> T[] toArray(T[] a) {
		return objects.toArray(a);
	}

}
