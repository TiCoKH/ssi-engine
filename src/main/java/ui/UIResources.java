package ui;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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

	public void setDungeonResources(@Nonnull DungeonResources dungeonRes) {
		this.dungeonResources = Optional.of(dungeonRes);
	}

	public void setMenu(@Nullable Menu menu) {
		this.menu = Optional.ofNullable(menu);
	}

	public void setOverlandResources(@Nonnull OverlandResources overlandResources) {
		this.overlandResources = Optional.of(overlandResources);
	}

	public void setPic(@Nullable List<BufferedImage> pic) {
		this.pic = Optional.ofNullable(pic);
		this.picIndex = 0;
	}

	public void setSpaceResources(@Nonnull SpaceResources spaceResources) {
		this.spaceResources = Optional.of(spaceResources);
	}

	public void setStatusLine(@Nullable GoldboxString statusLine) {
		this.statusLine = Optional.ofNullable(statusLine);
	}
}
