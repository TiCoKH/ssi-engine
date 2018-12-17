package ui.resource;

import data.content.DAXContentType;

class RenderInfo {
	private DAXContentType type;
	private int id;

	public RenderInfo(DAXContentType type, int id) {
		this.type = type;
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public DAXContentType getType() {
		return type;
	}

	@Override
	public String toString() {
		return Integer.toString(id);
	}
}