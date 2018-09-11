package data.content;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class DAXImageContent extends DAXContent {
	private static final DAXImageContentConverter CONVERTER = new DAXImageContentConverter();;

	protected final List<BufferedImage> images = new ArrayList<>();

	public boolean contains(Object arg0) {
		return images.contains(arg0);
	}

	public boolean containsAll(Collection<?> arg0) {
		return images.containsAll(arg0);
	}

	public void forEach(Consumer<? super BufferedImage> arg0) {
		images.forEach(arg0);
	}

	public BufferedImage get(int arg0) {
		return images.get(arg0);
	}

	public int indexOf(Object arg0) {
		return images.indexOf(arg0);
	}

	public boolean isEmpty() {
		return images.isEmpty();
	}

	public Iterator<BufferedImage> iterator() {
		return images.iterator();
	}

	public int lastIndexOf(Object arg0) {
		return images.lastIndexOf(arg0);
	}

	public ListIterator<BufferedImage> listIterator() {
		return images.listIterator();
	}

	public ListIterator<BufferedImage> listIterator(int arg0) {
		return images.listIterator(arg0);
	}

	public Stream<BufferedImage> parallelStream() {
		return images.parallelStream();
	}

	public int size() {
		return images.size();
	}

	public Spliterator<BufferedImage> spliterator() {
		return images.spliterator();
	}

	public Stream<BufferedImage> stream() {
		return images.stream();
	}

	public List<BufferedImage> subList(int arg0, int arg1) {
		return images.subList(arg0, arg1);
	}

	public Object[] toArray() {
		return images.toArray();
	}

	public <T> T[] toArray(T[] arg0) {
		return images.toArray(arg0);
	}

	public List<BufferedImage> toList() {
		return new ArrayList<>(images);
	}

	public List<BufferedImage> withWallSymbolColor() {
		return images.stream().map(CONVERTER::asWallSymbol).collect(Collectors.toList());
	}
}
