package ui.shared.resource;

import javax.annotation.Nonnull;

import data.ContentType;

public class IdTypeResource {
	private final int id;
	private final ContentType type;

	public IdTypeResource(int id, @Nonnull ContentType type) {
		this.id = id;
		this.type = type;
	}

	public int getId() {
		return id;
	}

	public ContentType getType() {
		return type;
	}

	@Override
	public String toString() {
		return Integer.toString(id);
	}
}
