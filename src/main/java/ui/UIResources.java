package ui;

import static data.content.DAXContentType.BACK;
import static data.content.DAXContentType.SPRIT;
import static data.content.DAXContentType._8X8D;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import data.content.DAXContentType;
import data.content.DungeonMap.VisibleWalls;
import data.content.WallDef;
import engine.ViewDungeonPosition;
import engine.ViewOverlandPosition;
import engine.ViewSpacePosition;
import engine.ViewSpacePosition.Celestial;
import types.GoldboxString;

public class UIResources {

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

	private UIResourceManager resman;

	public UIResources(@Nonnull UIResourceManager resman) {
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
		return resman.getBorders();
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
		return pic.map(p -> resman.getImageResource(p.getId(), p.getType()).get(picIndex));
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
			if (picIndex + 1 < resman.getImageResource(p.getId(), p.getType()).size())
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

	public void setDungeonResources(@Nonnull ViewDungeonPosition position, @Nonnull VisibleWalls visibleWalls, int wallId, int backId) {
		this.dungeonResources = Optional.of(new DungeonResources(position, visibleWalls, wallId, backId));
	}

	public void setMenu(@Nullable Menu menu) {
		this.menu = Optional.ofNullable(menu);
	}

	public void setOverlandResources(@Nonnull ViewOverlandPosition position, int mapId) {
		this.overlandResources = Optional.of(new OverlandResources(position, mapId));
	}

	public void setPic(int id, @Nullable DAXContentType type) {
		this.pic = Optional.of(new ImageResource(id, type));
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

		private int wallsIds;
		private int backId;

		private Optional<ImageResource> sprite = Optional.empty();
		private int spriteIndex = 0;

		DungeonResources(@Nonnull ViewDungeonPosition position, @Nonnull VisibleWalls visibleWalls, int wallsId, int backId) {
			this.position = position;
			this.positionText = new GoldboxStringPosition(position);
			this.visibleWalls = visibleWalls;
			this.wallsIds = wallsId;
			this.backId = backId;
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
		public BufferedImage getBackdrop() {
			if (position.getBackdropIndex() == 0)
				return resman.getImageResource(128 + backId, BACK).get(0);
			return resman.getImageResource(backId, BACK).get(0);
		}

		public GoldboxString getPositionText() {
			return positionText;
		}

		@Nonnull
		public Optional<BufferedImage> getSprite() {
			return sprite.map(s -> resman.getImageResource(s.getId(), s.getType()).get(spriteIndex));
		}

		@Nonnull
		public VisibleWalls getVisibleWalls() {
			return visibleWalls;
		}

		@Nonnull
		public Optional<WallDef> getWalls() {
			return Optional.ofNullable(resman.getWalldef(wallsIds));
		}

		@Nonnull
		public List<BufferedImage> getWallSymbols() {
			return resman.getImageResource(wallsIds, _8X8D);
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
		private int mapId;

		OverlandResources(@Nonnull ViewOverlandPosition position, int mapId) {
			this.position = position;
			this.mapId = mapId;
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
			return resman.getImageResource(mapId, null).get(0);
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
			return resman.getSpaceBackground();
		}

		@Nonnull
		public BufferedImage getSun() {
			return resman.getSpaceSymbols().get(0);
		}

		@Nonnull
		public BufferedImage getCelestial(@Nonnull Celestial c) {
			switch (c) {
				case MERKUR:
					// TODO 1-2
					return resman.getSpaceSymbols().get(2);
				case VENUS:
					// TODO 3-6
					return resman.getSpaceSymbols().get(3);
				case EARTH:
					// TODO 7-14
					return resman.getSpaceSymbols().get(7);
				case MARS:
					// TODO 15-30
					return resman.getSpaceSymbols().get(27);
				case CERES:
					// TODO 59-86
					return resman.getSpaceSymbols().get(59);
				default:
					// TODO 31-58
					return resman.getSpaceSymbols().get(40);
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
			return resman.getSpaceSymbols().get(87 + position.getSpaceDir().ordinal());
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
