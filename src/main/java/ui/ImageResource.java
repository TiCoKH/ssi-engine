package ui;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import data.content.DAXContentType;

public class ImageResource {
	private Optional<String> filename;
	private final int id;
	private final DAXContentType type;

	public ImageResource(int id, @Nullable DAXContentType type) {
		this.filename = Optional.empty();
		this.id = id;
		this.type = type;
	}

	public ImageResource(@Nonnull String filename, int id, @Nonnull DAXContentType type) {
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

	public DAXContentType getType() {
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
		return "ImageResource [filename=" + filename.orElse("") + ", id=" + id + ", type=" + type + "]";
	}
}
