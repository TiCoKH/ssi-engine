package ui;

import static ui.ScaleMethod.NONE;

import javax.annotation.Nonnull;

public class UISettings {
	private int zoom;
	private ScaleMethod method;

	public UISettings() {
		zoom = 4;
		method = NONE;
	}

	@Nonnull
	public ScaleMethod getMethod() {
		return method;
	}

	public void setMethod(@Nonnull ScaleMethod method) {
		this.method = method;
	}

	public int getZoom() {
		return zoom;
	}

	public void setZoom(int zoom) {
		this.zoom = zoom;
	}

	public int zoom(int value) {
		return getZoom() * value;
	}

	public int zoom8(int value) {
		return getZoom() * 8 * value;
	}

}
