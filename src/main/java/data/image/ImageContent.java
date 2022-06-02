package data.image;

import java.awt.image.BufferedImage;
import java.util.function.Consumer;
import java.util.function.Function;

import io.vavr.collection.Array;
import io.vavr.collection.Seq;

import data.Content;

public abstract class ImageContent extends Content {
	protected Seq<BufferedImage> images = Array.empty();

	public void forEach(Consumer<? super BufferedImage> action) {
		images.forEach(action);
	}

	public <U> Seq<U> flatMap(Function<? super BufferedImage, ? extends Iterable<? extends U>> mapper) {
		return images.flatMap(mapper);
	}

	public BufferedImage get(int index) {
		return images.get(index);
	}

	public <U> Seq<U> map(Function<? super BufferedImage, ? extends U> mapper) {
		return images.map(mapper);
	}

	public int size() {
		return images.size();
	}

	public Seq<BufferedImage> subList(int arg0, int arg1) {
		return images.subSequence(arg0, arg1);
	}

	public Seq<BufferedImage> toSeq() {
		return images;
	}
}
