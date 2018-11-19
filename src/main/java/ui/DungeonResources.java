package ui;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import data.content.DungeonMap.VisibleWalls;
import data.content.WallDef;
import engine.ViewDungeonPosition;

public class DungeonResources {
	private ViewDungeonPosition position;

	private VisibleWalls visibleWalls;

	private WallDef walls;
	private List<BufferedImage> wallSymbols;

	private List<BufferedImage> backdrops;

	private Optional<List<BufferedImage>> sprite = Optional.empty();
	private int spriteIndex = 0;

	public DungeonResources(@Nonnull ViewDungeonPosition position, @Nonnull VisibleWalls visibleWalls, @Nonnull WallDef walls,
		@Nonnull List<BufferedImage> wallSymbols, @Nonnull List<BufferedImage> backdrops) {

		this.position = position;
		this.visibleWalls = visibleWalls;
		this.walls = walls;
		this.wallSymbols = wallSymbols;
		this.backdrops = backdrops;
	}

	public void advanceSprite() {
		if (spriteAdvancementPossible()) {
			spriteIndex--;
		}
	}

	@Nonnull
	public BufferedImage getBackdrop() {
		return backdrops.get(position.getBackdropIndex());
	}

	@Nonnull
	public String getPosition() {
		if (position.getExtendedDungeonX() != 255) {
			return position.getExtendedDungeonX() + "," + position.getExtendedDungeonY() + " " + position.getDungeonDir().name().charAt(0);
		}
		return position.getDungeonX() + "," + position.getDungeonY() + " " + position.getDungeonDir().name().charAt(0);
	}

	@Nonnull
	public Optional<BufferedImage> getSprite() {
		return sprite.map(s -> s.get(spriteIndex));
	}

	@Nonnull
	public VisibleWalls getVisibleWalls() {
		return visibleWalls;
	}

	@Nonnull
	public WallDef getWalls() {
		return walls;
	}

	@Nonnull
	public List<BufferedImage> getWallSymbols() {
		return wallSymbols;
	}

	public void setSprite(@Nullable List<BufferedImage> sprite, int spriteIndex) {
		this.sprite = Optional.ofNullable(sprite);
		this.spriteIndex = spriteIndex;
	}

	public boolean spriteAdvancementPossible() {
		return spriteIndex > 0;
	}
}
