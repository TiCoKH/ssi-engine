package ui;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import data.content.DungeonMap.VisibleWalls;
import data.content.WallDef;
import engine.ViewDungeonPosition;
import engine.ViewOverlandPosition;
import engine.ViewSpacePosition;
import engine.ViewSpacePosition.Celestial;
import types.GoldboxString;

public class UIResources {

	// Font and border symbols, always available
	private Map<FontType, List<BufferedImage>> fonts;
	private List<BufferedImage> borderSymbols;

	// Elements to render 3D dungeons, only available in dungeons
	private Optional<DungeonResources> dungeonResources = Optional.empty();

	// Elements to render the position on the overland map
	private Optional<OverlandResources> overlandResources = Optional.empty();

	// Elements to render the space map and ship combat, only available in space
	private Optional<SpaceResources> spaceResources = Optional.empty();

	// The flavor picture(s), can be a small or big picture
	// Also used for the title pictures
	private Optional<List<BufferedImage>> pic = Optional.empty();
	private int picIndex = 0;

	// The story text
	private Optional<List<Byte>> charList = Optional.empty();
	private int charStop = 0;

	// the status text
	private Optional<GoldboxString> statusLine = Optional.empty();

	// a game menu
	private Optional<Menu> menu = Optional.empty();

	public UIResources(@Nonnull Map<FontType, List<BufferedImage>> fonts, @Nonnull List<BufferedImage> borderSymbols) {
		this.fonts = fonts;
		this.borderSymbols = borderSymbols;
	}

	public void addChars(@Nonnull List<Byte> addedChars) {
		if (charList.isPresent()) {
			charList.get().addAll(addedChars);
		} else {
			setCharList(addedChars);
		}
	}

	@Nonnull
	public List<BufferedImage> getBorderSymbols() {
		return borderSymbols;
	}

	@Nonnull
	public Optional<List<Byte>> getCharList() {
		return charList;
	}

	public int getCharCount() {
		return charList.map(List::size).orElse(0);
	}

	public int getCharStop() {
		return charStop;
	}

	@Nonnull
	public Optional<DungeonResources> getDungeonResources() {
		return dungeonResources;
	}

	@Nonnull
	public List<BufferedImage> getFont(FontType type) {
		return fonts.get(type);
	}

	@Nonnull
	public Optional<Menu> getMenu() {
		return menu;
	}

	@Nonnull
	public Optional<OverlandResources> getOverlandResources() {
		return overlandResources;
	}

	@Nonnull
	public Optional<BufferedImage> getPic() {
		return pic.map(p -> p.get(picIndex));
	}

	@Nonnull
	public Optional<SpaceResources> getSpaceResources() {
		return spaceResources;
	}

	@Nonnull
	public Optional<GoldboxString> getStatusLine() {
		return statusLine;
	}

	public boolean hasCharStopReachedLimit() {
		return charList.map(cl -> charStop == cl.size()).orElse(true);
	}

	public void incCharStop() {
		charList.ifPresent(cl -> {
			if (charStop < cl.size())
				charStop++;
		});
	}

	public void incPicIndex() {
		pic.ifPresent(p -> {
			if (picIndex + 1 < p.size())
				picIndex++;
			else
				picIndex = 0;
		});
	}

	public boolean preferSprite() {
		return dungeonResources.map(r -> r.getSprite().map(s -> true).orElse(false)).orElse(false);
	}

	public void setCharList(@Nullable List<Byte> charList) {
		this.charList = Optional.ofNullable(charList);
		this.charStop = 0;
	}

	public void setDungeonResources(@Nonnull ViewDungeonPosition position, @Nonnull VisibleWalls visibleWalls, @Nonnull WallDef walls,
		@Nonnull List<BufferedImage> wallSymbols, @Nonnull List<BufferedImage> backdrops) {

		this.dungeonResources = Optional.of(new DungeonResources(position, visibleWalls, walls, wallSymbols, backdrops));
	}

	public void setMenu(@Nullable Menu menu) {
		this.menu = Optional.ofNullable(menu);
	}

	public void setOverlandResources(@Nonnull ViewOverlandPosition position, @Nonnull BufferedImage map, @Nonnull BufferedImage cursor) {
		this.overlandResources = Optional.of(new OverlandResources(position, map, cursor));
	}

	public void setPic(@Nullable List<BufferedImage> pic) {
		this.pic = Optional.ofNullable(pic);
		this.picIndex = 0;
	}

	public void setSpaceResources(@Nonnull ViewSpacePosition position, @Nonnull BufferedImage background, @Nonnull List<BufferedImage> symbols) {
		this.spaceResources = Optional.of(new SpaceResources(position, background, symbols));
	}

	public void setStatusLine(@Nullable GoldboxString statusLine) {
		this.statusLine = Optional.ofNullable(statusLine);
	}

	public class DungeonResources {
		private ViewDungeonPosition position;
		private GoldboxString positionText;

		private VisibleWalls visibleWalls;

		private WallDef walls;
		private List<BufferedImage> wallSymbols;

		private List<BufferedImage> backdrops;

		private Optional<List<BufferedImage>> sprite = Optional.empty();
		private int spriteIndex = 0;

		DungeonResources(@Nonnull ViewDungeonPosition position, @Nonnull VisibleWalls visibleWalls, @Nonnull WallDef walls,
			@Nonnull List<BufferedImage> wallSymbols, @Nonnull List<BufferedImage> backdrops) {

			this.position = position;
			this.positionText = new GoldboxStringPosition(position);
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

		public GoldboxString getPositionText() {
			return positionText;
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

	public class OverlandResources {
		private ViewOverlandPosition position;
		private BufferedImage map;
		private BufferedImage cursor;

		OverlandResources(@Nonnull ViewOverlandPosition position, @Nonnull BufferedImage map, @Nonnull BufferedImage cursor) {
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

	public class SpaceResources {
		private ViewSpacePosition position;

		private BufferedImage background;
		private List<BufferedImage> symbols;

		private GoldboxString statusLine;

		SpaceResources(@Nonnull ViewSpacePosition position, @Nonnull BufferedImage background, @Nonnull List<BufferedImage> symbols) {
			if (symbols.size() != 95) {
				throw new IllegalArgumentException("space symbols does not contain 95 images.");
			}
			this.position = position;
			this.background = background;
			this.symbols = symbols;
			this.statusLine = new GoldboxStringFuel(position);
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

		public GoldboxString getStatusLine() {
			return statusLine;
		}
	}
}
