package ui;

import static data.content.DAXContentType.BACK;
import static data.content.DAXContentType.SPRIT;
import static ui.ImageResource.SKY_CLOUD;
import static ui.ImageResource.SKY_STREET;
import static ui.ImageResource.SKY_SUN;
import static ui.ImageResource.SPACE_BACKGROUND;
import static ui.ImageResource.SPACE_SYMBOLS;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import data.content.DungeonMap.VisibleWalls;
import engine.ViewDungeonPosition;
import engine.ViewOverlandPosition;
import engine.ViewSpacePosition;
import engine.ViewSpacePosition.Celestial;
import types.GoldboxString;

public class UIResources {
	private static final int[] SKY_COLOURS = new int[] { //
		0x00, 0x0F, 0x04, 0x0B, 0x0D, 0x02, 0x09, 0x0E, 0x00, 0x0F, 0x04, 0x0B, 0x0D, 0x02, 0x09, 0x0E //
	};

	// Elements to render 3D dungeons, only available in dungeons
	private Optional<DungeonResources> dungeonResources = Optional.empty();

	// Elements to render the position on the overland map
	private Optional<OverlandResources> overlandResources = Optional.empty();

	// Elements to render the space map and ship combat, only available in space
	private Optional<SpaceResources> spaceResources = Optional.empty();

	// The flavor picture(s), can be a small or big picture
	// Also used for the title pictures
	private Optional<ImageResource> pic = Optional.empty();
	private int picIndex = 0;

	// The story text
	private Optional<List<Byte>> charList = Optional.empty();
	private int charStop = 0;

	// the status text
	private Optional<GoldboxString> statusLine = Optional.empty();

	// a game menu
	private Optional<Menu> menu = Optional.empty();

	private UIResourceConfiguration config;
	private UIResourceManager resman;

	public UIResources(@Nonnull UIResourceConfiguration config, @Nonnull UIResourceManager resman) {
		this.config = config;
		this.resman = resman;
	}

	public void addChars(@Nonnull List<Byte> addedChars) {
		if (charList.isPresent()) {
			charList.get().addAll(addedChars);
		} else {
			setCharList(addedChars);
		}
	}

	public void clearPic() {
		pic = Optional.empty();
	}

	@Nonnull
	public List<BufferedImage> getBorderSymbols() {
		return resman.getFrames();
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
	public List<BufferedImage> getFont(@Nonnull FontType type) {
		return resman.getFont(type);
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
		return pic.map(p -> resman.getImageResource(p).get(picIndex));
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
			if (picIndex + 1 < resman.getImageResource(p).size())
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

	public void setDungeonResources(@Nonnull ViewDungeonPosition position, @Nonnull VisibleWalls visibleWalls, @Nonnull DungeonResource res) {
		this.dungeonResources = Optional.of(new DungeonResources(position, visibleWalls, res));
	}

	public void setMenu(@Nullable Menu menu) {
		this.menu = Optional.ofNullable(menu);
	}

	public void setOverlandResources(@Nonnull ViewOverlandPosition position, int mapId) {
		this.overlandResources = Optional.of(new OverlandResources(position, mapId));
	}

	public void setPic(@Nonnull ImageResource r) {
		this.pic = Optional.of(r);
		this.picIndex = 0;
	}

	public void setSpaceResources(@Nonnull ViewSpacePosition position) {
		this.spaceResources = Optional.of(new SpaceResources(position));
	}

	public void setStatusLine(@Nullable GoldboxString statusLine) {
		this.statusLine = Optional.ofNullable(statusLine);
	}

	public class DungeonResources {
		private ViewDungeonPosition position;
		private GoldboxString positionText;

		private VisibleWalls visibleWalls;

		private DungeonResource res;
		private List<ImageResource> back;

		private Optional<ImageResource> sprite = Optional.empty();
		private int spriteIndex = 0;

		DungeonResources(@Nonnull ViewDungeonPosition position, @Nonnull VisibleWalls visibleWalls, @Nonnull DungeonResource res) {
			this.position = position;
			this.positionText = new GoldboxStringPosition(position);
			this.visibleWalls = visibleWalls;
			this.res = res;
			initBackgrounds();
		}

		private void initBackgrounds() {
			switch (getBackdropMode()) {
				case COLOR:
					// no image resources
					break;
				case SKY:
					this.back = Arrays.asList(SKY_CLOUD, SKY_SUN, SKY_STREET);
					break;
				case SKYGRND:
					// TODO
					break;
				case SPACE:
					this.back = Arrays.asList( //
						new ImageResource(128 + res.getId1(), BACK), //
						new ImageResource(res.getId1(), BACK));
					break;
				case GEO2:
					// TODO
					break;
			}
		}

		public void advanceSprite() {
			if (spriteAdvancementPossible()) {
				spriteIndex--;
			}
		}

		public void clearSprite() {
			sprite = Optional.empty();
		}

		@Nonnull
		public BackdropMode getBackdropMode() {
			return config.getBackdropMode();
		}

		@Nonnull
		public BufferedImage getBackdrop(int index) {
			return resman.getImageResource(back.get(index)).get(0);
		}

		public boolean isOutside() {
			return position.getBackdropIndex() == 0;
		}

		public GoldboxString getPositionText() {
			return positionText;
		}

		public int getSkyColorOutdoors() {
			return SKY_COLOURS[position.getSkyColorOutdoors()];
		}

		public int getSkyColorIndoors() {
			return SKY_COLOURS[position.getSkyColorIndoors()];
		}

		@Nonnull
		public Optional<BufferedImage> getSprite() {
			return sprite.map(s -> resman.getImageResource(s).get(spriteIndex));
		}

		@Nonnull
		public VisibleWalls getVisibleWalls() {
			return visibleWalls;
		}

		@Nonnull
		public List<DungeonWall> getWalls() {
			return resman.getWallResource(res);
		}

		public void setSprite(int spriteId, int spriteIndex) {
			this.sprite = Optional.of(new ImageResource(spriteId, SPRIT));
			this.spriteIndex = spriteIndex;
		}

		public boolean spriteAdvancementPossible() {
			return spriteIndex > 0;
		}
	}

	public class OverlandResources {
		private ViewOverlandPosition position;
		private ImageResource map;

		OverlandResources(@Nonnull ViewOverlandPosition position, int mapId) {
			this.position = position;
			this.map = new ImageResource(mapId, null);
		}

		@Nonnull
		public BufferedImage getCursor() {
			return resman.getOverlandCursor();
		}

		public int getCursorX() {
			return position.getOverlandX();
		}

		public int getCursorY() {
			return position.getOverlandY();
		}

		@Nonnull
		public BufferedImage getMap() {
			return resman.getImageResource(map).get(0);
		}
	}

	public class SpaceResources {
		private ViewSpacePosition position;
		private GoldboxString statusLine;

		SpaceResources(@Nonnull ViewSpacePosition position) {
			this.position = position;
			this.statusLine = new GoldboxStringFuel(position);
		}

		@Nonnull
		public BufferedImage getBackground() {
			return resman.getImageResource(SPACE_BACKGROUND).get(0);
		}

		@Nonnull
		public BufferedImage getSun() {
			return resman.getImageResource(SPACE_SYMBOLS).get(0);
		}

		@Nonnull
		public BufferedImage getCelestial(@Nonnull Celestial c) {
			switch (c) {
				case MERKUR:
					// TODO 1-2
					return resman.getImageResource(SPACE_SYMBOLS).get(2);
				case VENUS:
					// TODO 3-6
					return resman.getImageResource(SPACE_SYMBOLS).get(3);
				case EARTH:
					// TODO 7-14
					return resman.getImageResource(SPACE_SYMBOLS).get(7);
				case MARS:
					// TODO 15-30
					return resman.getImageResource(SPACE_SYMBOLS).get(27);
				case CERES:
					// TODO 59-86
					return resman.getImageResource(SPACE_SYMBOLS).get(59);
				default:
					// TODO 31-58
					return resman.getImageResource(SPACE_SYMBOLS).get(40);
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
			return resman.getImageResource(SPACE_SYMBOLS).get(87 + position.getSpaceDir().ordinal());
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
