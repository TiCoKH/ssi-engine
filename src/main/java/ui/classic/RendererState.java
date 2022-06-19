package ui.classic;

import static data.ContentType.BACK;
import static data.ContentType.SPRIT;
import static io.vavr.API.Seq;
import static ui.shared.resource.ImageResource.SKY_CLOUD;
import static ui.shared.resource.ImageResource.SKY_STREET;
import static ui.shared.resource.ImageResource.SKY_SUN;
import static ui.shared.resource.ImageResource.SPACE_BACKGROUND;
import static ui.shared.resource.ImageResource.SPACE_SYMBOLS;

import java.awt.image.BufferedImage;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.vavr.collection.Seq;

import data.dungeon.DungeonMap.Direction;
import data.dungeon.DungeonMap.VisibleWalls;
import shared.GoldboxString;
import shared.GoldboxStringPart;
import shared.ViewDungeonPosition;
import shared.ViewGlobalData;
import shared.ViewOverlandPosition;
import shared.ViewSpacePosition;
import shared.ViewSpacePosition.Celestial;
import ui.shared.BackdropMode;
import ui.shared.Menu;
import ui.shared.dungeon.DungeonWall;
import ui.shared.resource.DungeonMapResource;
import ui.shared.resource.DungeonResource;
import ui.shared.resource.ImageResource;
import ui.shared.resource.UIResourceConfiguration;
import ui.shared.resource.UIResourceManager;
import ui.shared.text.GoldboxStringFuel;
import ui.shared.text.GoldboxStringPosition;
import ui.shared.text.StoryText;

public class RendererState {
	private static final int[] SKY_COLOURS = new int[] { //
		0x00, 0x0F, 0x04, 0x0B, 0x0D, 0x02, 0x09, 0x0E, 0x00, 0x0F, 0x04, 0x0B, 0x0D, 0x02, 0x09, 0x0E //
	};

	// Elements to render 3D dungeons, only available in dungeons
	private Optional<DungeonResources> dungeonResources = Optional.empty();

	// Elements to render the position on the overland map
	private Optional<OverlandResources> overlandResources = Optional.empty();

	// Elements to render the space map and ship combat, only available in space
	private Optional<SpaceResources> spaceResources = Optional.empty();

	// global info like party members or time
	private Optional<ViewGlobalData> globalData = Optional.empty();

	// The flavor picture(s), can be a small or big picture
	// Also used for the title pictures
	private Optional<ImageResource> pic = Optional.empty();
	private int picIndex = 0;

	// The story text
	private StoryText storyText = new StoryText();

	// the status text
	private Optional<GoldboxString> statusLine = Optional.empty();

	// a game menu
	private Optional<Menu> menu = Optional.empty();

	private UIResourceConfiguration config;
	private UIResourceManager resman;

	public RendererState(@Nonnull UIResourceConfiguration config, @Nonnull UIResourceManager resman) {
		this.config = config;
		this.resman = resman;
	}

	public void reset() {
		clearDungeonResources();
		clearOverlandResources();
		clearSpaceResources();
		clearPic();
		storyText.resetText();
		statusLine = Optional.empty();
		menu = Optional.empty();
	}

	public void clearDungeonResources() {
		dungeonResources = Optional.empty();
	}

	public void clearOverlandResources() {
		overlandResources = Optional.empty();
	}

	public void clearSpaceResources() {
		spaceResources = Optional.empty();
	}

	public void clearPic() {
		pic = Optional.empty();
		picIndex = 0;
	}

	@Nonnull
	public Optional<DungeonResources> getDungeonResources() {
		return dungeonResources;
	}

	@Nonnull
	public Optional<ViewGlobalData> getGlobalData() {
		return globalData;
	}

	@Nonnull
	public Seq<BufferedImage> getMisc() {
		return resman.getMisc();
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

	public StoryText getStoryText() {
		return storyText;
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

	public void setDungeonResources(@Nonnull ViewDungeonPosition position, @Nullable VisibleWalls visibleWalls,
		@Nullable int[][] map, @Nonnull DungeonResource res) {

		this.dungeonResources = Optional.of(new DungeonResources(position, visibleWalls, map, res));
	}

	public void setGlobalData(@Nonnull ViewGlobalData globalData) {
		this.globalData = Optional.of(globalData);
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

		private Optional<VisibleWalls> visibleWalls;
		private Optional<DungeonMapResource> map;

		private DungeonResource res;
		private Seq<ImageResource> back;

		private Optional<ImageResource> sprite = Optional.empty();
		private int spriteIndex = 0;

		private boolean showAreaMap;

		// runic text in Pool of Radiance
		private Seq<GoldboxStringPart> runicText = Seq();

		DungeonResources(@Nonnull ViewDungeonPosition position, @Nullable VisibleWalls visibleWalls,
			@Nullable int[][] map, @Nonnull DungeonResource res) {

			this.position = position;
			this.positionText = new GoldboxStringPosition(position);
			this.visibleWalls = Optional.ofNullable(visibleWalls);
			this.res = res;

			if (map != null)
				this.map = Optional.of(new DungeonMapResource(map, canShowDungeon() ? null : res));
			else
				this.map = Optional.empty();
			this.showAreaMap = !canShowDungeon();

			initBackgrounds();
		}

		private void initBackgrounds() {
			switch (getBackdropMode()) {
				case COLOR:
					// no image resources
					break;
				case SKY:
					this.back = Seq(SKY_CLOUD, SKY_SUN, SKY_STREET);
					break;
				case SKYGRND:
					// TODO
					break;
				case SPACE:
					this.back = Seq( //
						new ImageResource(128 + res.getIds()[0], BACK), //
						new ImageResource(res.getIds()[0], BACK));
					break;
				case GEO2:
					// TODO
					break;
			}
		}

		public void addRunicText(GoldboxStringPart text) {
			runicText = runicText.append(text);
		}

		public boolean isShowRunicText() {
			return !runicText.isEmpty();
		}

		@Nonnull
		public Seq<GoldboxStringPart> getRunicText() {
			return runicText;
		}

		public void advanceSprite() {
			if (spriteAdvancementPossible()) {
				spriteIndex--;
			}
		}

		public boolean canShowDungeon() {
			return visibleWalls.isPresent();
		}

		public boolean canShowAreaMap() {
			return map.isPresent();
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

		public boolean isShowAreaMap() {
			return showAreaMap;
		}

		@Nonnull
		public Optional<BufferedImage> getMap() {
			return map.map(m -> resman.getMapResource(m));
		}

		public BufferedImage getMapArrow() {
			return getMisc().get(config.getMiscArrowIndex() + getPositionDirection().ordinal());
		}

		public int getMapWidth() {
			return map.map(DungeonMapResource::getMapWidth).orElse(0);
		}

		public int getMapHeight() {
			return map.map(DungeonMapResource::getMapHeight).orElse(0);
		}

		public GoldboxString getPositionText() {
			return positionText;
		}

		public int getPositionX() {
			if (position.getExtendedDungeonX() != 255) {
				return position.getExtendedDungeonX();
			}
			return position.getDungeonX();
		}

		public int getPositionY() {
			if (position.getExtendedDungeonX() != 255) {
				return position.getExtendedDungeonY();
			}
			return position.getDungeonY();
		}

		public Direction getPositionDirection() {
			return position.getDungeonDir();
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
		public Optional<VisibleWalls> getVisibleWalls() {
			return visibleWalls;
		}

		@Nonnull
		public Seq<DungeonWall> getWalls() {
			return resman.getWallResource(res);
		}

		public void setSprite(int spriteId, int spriteIndex) {
			this.sprite = Optional.of(new ImageResource(spriteId, SPRIT));
			this.spriteIndex = spriteIndex;
		}

		public boolean spriteAdvancementPossible() {
			return spriteIndex > 0;
		}

		public void toggleShowAreaMap() {
			if (showAreaMap && visibleWalls.isPresent() || !showAreaMap && map.isPresent()) {
				showAreaMap = !showAreaMap;
			}
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
