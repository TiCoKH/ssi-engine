package ui.shared.resource;

import static data.ContentType.BACK;
import static data.ContentType.BIGPIC;
import static data.ContentType._8X8D;
import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import data.ContentType;

public class ImageResource {
	public static final ImageResource SPACE_SYMBOLS = new ImageResource("8X8D0.DAX", 1, _8X8D);
	public static final ImageResource SPACE_BACKGROUND = new ImageResource("SHIPS.DAX", 128, BIGPIC);

	public static final ImageResource SKY_CLOUD = new ImageResource("SKY.DAX", 250, BACK);
	public static final ImageResource SKY_SUN = new ImageResource("SKY.DAX", 251, BACK);
	public static final ImageResource SKY_STREET = new ImageResource("SKY.DAX", 252, BACK);

	private Optional<String> filename;
	private final int id;
	private final ContentType type;

	public ImageResource(int id, @Nullable ContentType type) {
		this.filename = Optional.empty();
		this.id = id;
		this.type = type;
	}

	public ImageResource(@Nonnull String filename, int id, @Nonnull ContentType type) {
		this.filename = Optional.of(filename);
		this.id = id;
		this.type = requireNonNull(type);
	}

	public Optional<String> getFilename() {
		return filename;
	}

	public int getId() {
		return id;
	}

	public ContentType getType() {
		return type;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, type);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ImageResource)) {
			return false;
		}
		ImageResource other = (ImageResource) obj;
		return filename.equals(other.filename) && id == other.id && type == other.type;
	}

	@Override
	public String toString() {
		return filename.map(fn -> fn + ", ").orElse("") + id;
	}
}
