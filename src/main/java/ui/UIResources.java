package ui;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UIResources {

	// Font and border symbols, always available
	private Map<FontType, List<BufferedImage>> fonts;
	private List<BufferedImage> borderSymbols;

	// Elements to render 3D dungeons, only available in dungeons
	private Optional<DungeonResources> dungeonResources = Optional.empty();

	// The flavor picture(s), can be a small or big picture
	// Also used for the title pictures
	private Optional<List<BufferedImage>> pic = Optional.empty();
	private int picIndex = 0;

	// The story text
	private Optional<List<Byte>> charList = Optional.empty();
	private int charStop = 0;

	// Elements to render on the status line
	private Optional<StatusLine> statusLine = Optional.empty();

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

	public Optional<List<Byte>> getCharList() {
		return charList;
	}

	public int getCharCount() {
		return charList.map(List::size).orElse(0);
	}

	public int getCharStop() {
		return charStop;
	}

	public Optional<DungeonResources> getDungeonResources() {
		return dungeonResources;
	}

	@Nonnull
	public List<BufferedImage> getFont(FontType type) {
		return fonts.get(type);
	}

	public Optional<BufferedImage> getPic() {
		return pic.map(p -> p.get(picIndex));
	}

	public Optional<StatusLine> getStatusLine() {
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

	public void setPic(@Nullable List<BufferedImage> pic) {
		this.pic = Optional.ofNullable(pic);
		this.picIndex = 0;
	}

	public void setStatusLine(@Nullable StatusLine statusLine) {
		this.statusLine = Optional.ofNullable(statusLine);
	}
}
