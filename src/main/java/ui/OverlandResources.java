package ui;

import java.awt.image.BufferedImage;

import javax.annotation.Nonnull;

import engine.ViewOverlandPosition;

public class OverlandResources {
	private ViewOverlandPosition position;
	private BufferedImage map;
	private BufferedImage cursor;

	public OverlandResources(@Nonnull ViewOverlandPosition position, @Nonnull BufferedImage map, @Nonnull BufferedImage cursor) {
		this.position = position;
		this.map = map;
		this.cursor = cursor;
	}

	public BufferedImage getCursor() {
		return cursor;
	}

	public int getCursorX() {
		return position.getOverlandX();
	}

	public int getCursorY() {
		return position.getOverlandY();
	}

	public BufferedImage getMap() {
		return map;
	}
}
