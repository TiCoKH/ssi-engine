package ui;

import java.util.Objects;

import javax.annotation.Nullable;

import data.content.DAXContentType;

public class ImageResource {
	private final int id;
	private final DAXContentType type;

	public ImageResource(int id, @Nullable DAXContentType type) {
		this.id = id;
		this.type = type;
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
		if (obj == null || !(obj instanceof ImageResource)) {
			return false;
		}
		ImageResource other = (ImageResource) obj;
		return id == other.id && type == other.type;
	}

	@Override
	public String toString() {
		return "ImageResource [id=" + id + ", type=" + type + "]";
	}

}
