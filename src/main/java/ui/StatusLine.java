package ui;

import static ui.FontType.NORMAL;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import engine.opcodes.EclString;

public class StatusLine {
	private Optional<EclString> text;
	private FontType textFont;
	private Optional<List<EclString>> menu;
	private int menuIndex;

	public StatusLine(@Nonnull Optional<String> text, @Nonnull FontType textFont, @Nonnull Optional<List<String>> menu) {
		this.text = text.map(EclString::new);
		this.textFont = textFont;
		this.menu = Optional.ofNullable(menu.map(l -> l.stream().map(EclString::new).collect(Collectors.toList())).orElse(null));
		this.menuIndex = 0;
	}

	public static StatusLine of(@Nonnull List<String> menu) {
		return new StatusLine(Optional.empty(), NORMAL, Optional.of(menu));
	}

	public static StatusLine of(@Nullable String text, @Nullable FontType font, @Nonnull List<String> menu) {
		return new StatusLine(Optional.ofNullable(text), font == null ? NORMAL : font, Optional.of(menu));
	}

	public static StatusLine of(@Nonnull String text) {
		return new StatusLine(Optional.of(text), NORMAL, Optional.empty());
	}

	public static StatusLine of(@Nonnull String text, @Nonnull FontType font) {
		return new StatusLine(Optional.of(text), font, Optional.empty());
	}

	public Optional<EclString> getText() {
		return text;
	}

	public FontType getTextFont() {
		return textFont;
	}

	public Optional<List<EclString>> getMenu() {
		return menu;
	}

	public int getMenuIndex() {
		return menuIndex;
	}

	public void setMenuIndex(int menuIndex) {
		this.menuIndex = menuIndex;
	}
}
