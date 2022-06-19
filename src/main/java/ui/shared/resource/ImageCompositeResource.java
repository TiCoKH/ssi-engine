package ui.shared.resource;

import static io.vavr.API.Map;

import java.awt.Point;
import java.util.Arrays;
import java.util.Optional;

import javax.annotation.Nonnull;

import io.vavr.collection.Map;

import data.ContentType;

public class ImageCompositeResource extends ImageResource {
	private ImageResource[] r;
	private Map<ImageResource, Point> offsetMap = Map();

	public ImageCompositeResource(@Nonnull ImageResource r) {
		this(r, 0, 0);
	}

	public ImageCompositeResource(@Nonnull ImageResource r, int x, int y) {
		super(r.getId(), r.getType());

		this.r = new ImageResource[1];
		this.r[0] = r;

		offsetMap = offsetMap.put(r, new Point(x, y));
	}

	public ImageCompositeResource(@Nonnull ImageResource r1, int x1, int y1, @Nonnull ImageResource r2, int x2,
		int y2) {
		super(r1.getId(), r1.getType());

		this.r = new ImageResource[2];
		this.r[0] = r1;
		this.r[1] = r2;

		offsetMap = offsetMap.put(r1, new Point(x1, y1));
		offsetMap = offsetMap.put(r2, new Point(x2, y2));
	}

	public int getLength() {
		return r.length;
	}

	public ImageResource get(int index) {
		return r[index];
	}

	public Optional<String> getFilename(int index) {
		return r[index].getFilename();
	}

	public int getId(int index) {
		return r[index].getId();
	}

	public ContentType getType(int index) {
		return r[index].getType();
	}

	public Point getOffset(int index) {
		return offsetMap.get(r[index]).get();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Arrays.hashCode(r);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof ImageCompositeResource)) {
			return false;
		}
		ImageCompositeResource other = (ImageCompositeResource) obj;
		return Arrays.equals(r, other.r);
	}

	@Override
	public String toString() {
		return "ImageCompositeResource [r=" + Arrays.toString(r) + "]";
	}
}
