package ui.classic;

import static shared.FontColor.GAME_NAME;
import static shared.FontColor.INTENSE;
import static shared.FontColor.NORMAL;
import static shared.FontColor.SHORTCUT;
import static shared.GoldboxStringPart.PartType.COLOR;
import static shared.GoldboxStringPart.PartType.LINE_BREAK;
import static shared.GoldboxStringPart.PartType.SPACE;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import shared.FontColor;
import shared.GoldboxString;
import shared.GoldboxStringPart;
import shared.MenuType;
import ui.StoryText;
import ui.UIFrame;
import ui.UIResources;
import ui.UISettings;

public abstract class AbstractRenderer {

	protected UIResources resources;
	protected UISettings settings;

	private AbstractFrameRenderer frameRenderer;

	protected AbstractRenderer(@Nonnull UIResources resources, @Nonnull UISettings settings, @Nonnull AbstractFrameRenderer frameRenderer) {
		this.resources = resources;
		this.settings = settings;
		this.frameRenderer = frameRenderer;
	}

	public abstract int getLineWidth();

	public abstract int getTextStartX();

	public abstract int getTextStartY();

	public abstract void render(@Nonnull Graphics2D g2d);

	protected void renderFrame(@Nonnull Graphics2D g2d, @Nonnull UIFrame layout) {
		frameRenderer.render(g2d, layout);
	}

	protected void renderString(@Nonnull Graphics2D g2d, @Nonnull GoldboxString str, int xStart, int y, @Nonnull FontColor color) {
		renderString(g2d, str, xStart, y, color, color);
	}

	protected void renderString(@Nonnull Graphics2D g2d, @Nonnull GoldboxString str, int xStart, int y, @Nonnull FontColor firstCharcolor,
		@Nonnull FontColor color) {

		for (int pos = 0; pos < str.getLength(); pos++) {
			renderChar(g2d, xStart + pos, y, str.getChar(pos), pos == 0 ? firstCharcolor : color);
		}
	}

	protected void renderChar(@Nonnull Graphics2D g2d, int x, int y, int c, @Nonnull FontColor textFont) {
		BufferedImage ci = resources.getFont(textFont).get(c);
		renderImage(g2d, ci, x, y);
	}

	protected void renderText(@Nonnull Graphics2D g2d) {
		StoryText st = resources.getStoryText();
		st.getTextList().ifPresent(
			text -> renderText(g2d, text, getTextStartX(), getTextStartY(), getLineWidth(), st.getCharStop(), st.getDefaultTextColor(), 0));
	}

	protected int renderText(@Nonnull Graphics2D g2d, List<GoldboxStringPart> text, int startX, int StartY, int lineLength, int charStop,
		Optional<FontColor> initialColor, int initialOffset) {

		int line = 0;
		int pos = initialOffset;
		int index = 0;
		int renderCount = 0;
		Optional<FontColor> fc = initialColor;
		while (index < text.size() && (charStop == -1 || renderCount < charStop)) {
			GoldboxStringPart tp = text.get(index++);
			if (COLOR.equals(tp.getType())) {
				fc = tp.getFontColor();
				continue;
			}
			if (LINE_BREAK.equals(tp.getType()) || tp.getLength() > lineLength - pos) {
				line++;
				pos = 0;
			}
			if (SPACE.equals(tp.getType()) && pos == 0) {
				continue;
			}
			int i = 0;
			while ((charStop == -1 || renderCount < charStop) && i < tp.getLength()) {
				int x = startX + pos;
				int y = StartY + line;
				int c = tp.getChar(i++);
				FontColor fontColor = tp.getFontColor().orElse(fc.orElse(NORMAL));
				if (c < 0) {
					c += resources.getFont(fontColor).size();
				}
				renderChar(g2d, x, y, c, fontColor);
				renderCount++;
				pos++;
			}
		}
		return line;
	}

	protected void renderStatus(@Nonnull Graphics2D g2d) {
		resources.getStatusLine().ifPresent(status -> {
			renderString(g2d, status, 0, 24, NORMAL);
		});
	}

	protected void renderMenu(@Nonnull Graphics2D g2d) {
		resources.getMenu().ifPresent(menu -> {
			switch (menu.getType()) {
				case HORIZONTAL:
					menu.getDescription().ifPresent(desc -> {
						renderString(g2d, desc, 0, 24, GAME_NAME);
					});
					int menuStart = menu.getDescription().map(desc -> desc.getLength() + 1).orElse(0);
					for (int i = 0; i < menu.getItemCount(); i++) {
						GoldboxString menuName = menu.getMenuItem(i);
						renderString(g2d, menuName, menuStart, 24, //
							menu.isSelected(i) ? INTENSE : SHORTCUT, //
							menu.isSelected(i) ? INTENSE : NORMAL);
						menuStart += menuName.getLength() + 1;
					}
					break;
				case VERTICAL:
					menu.getDescription().ifPresent(desc -> {
						renderString(g2d, desc, 1, 17, NORMAL);
					});
					int firstLine = menu.getDescription().map(desc -> 18).orElse(17);
					for (int i = 0; i < menu.getItemCount(); i++) {
						renderString(g2d, menu.getMenuItem(i), 1, firstLine + i, //
							menu.isSelected(i) ? INTENSE : SHORTCUT, //
							menu.isSelected(i) ? INTENSE : NORMAL);
					}
					break;
				case PROGRAM:
					break;
			}
		});
	}

	protected void renderMenuOrTextStatus(@Nonnull Graphics2D g2d) {
		if (resources.getMenu().filter(menu -> menu.getType() == MenuType.VERTICAL).isPresent())
			renderMenu(g2d);
		else
			renderText(g2d);
		if (resources.getMenu().filter(menu -> menu.getType() == MenuType.HORIZONTAL).isPresent())
			renderMenu(g2d);
		else
			renderStatus(g2d);
	}

	protected void renderImage(@Nonnull Graphics2D g2d, @Nonnull BufferedImage image, int x, int y) {
		g2d.drawImage(image, settings.zoom8(x), settings.zoom8(y), null);
	}
}
