package ui;

import java.awt.image.BufferedImage;
import java.util.List;

import javax.annotation.Nonnull;

import engine.ViewSpacePosition;
import engine.ViewSpacePosition.Celestial;

public class SpaceResources {
	private ViewSpacePosition position;

	private BufferedImage background;
	private List<BufferedImage> symbols;

	private StatusLine statusLine;

	public SpaceResources(@Nonnull ViewSpacePosition position, @Nonnull BufferedImage background, @Nonnull List<BufferedImage> symbols) {
		if (symbols.size() != 95) {
			throw new IllegalArgumentException("space symbols does not contain 95 images.");
		}
		this.position = position;
		this.background = background;
		this.symbols = symbols;
		this.statusLine = new SpaceStatusLine(position);
	}

	@Nonnull
	public BufferedImage getBackground() {
		return background;
	}

	@Nonnull
	public BufferedImage getSun() {
		return symbols.get(0);
	}

	@Nonnull
	public BufferedImage getCelestial(@Nonnull Celestial c) {
		switch (c) {
			case MERKUR:
				// TODO 1-2
				return symbols.get(2);
			case VENUS:
				// TODO 3-6
				return symbols.get(3);
			case EARTH:
				// TODO 7-14
				return symbols.get(7);
			case MARS:
				// TODO 15-30
				return symbols.get(27);
			case CERES:
				// TODO 59-86
				return symbols.get(59);
			default:
				// TODO 31-58
				return symbols.get(40);
		}
	}

	public int getCelestialX(@Nonnull Celestial c) {
		return 17 + position.getCelestialX(c);
	}

	public int getCelestialY(@Nonnull Celestial c) {
		return 1 + position.getCelestialY(c);
	}

	@Nonnull
	public BufferedImage getShip() {
		return symbols.get(87 + position.getSpaceDir().ordinal());
	}

	public int getShipX() {
		return 17 + position.getSpaceX();
	}

	public int getShipY() {
		return 1 + position.getSpaceY();
	}

	public StatusLine getStatusLine() {
		return statusLine;
	}
}
