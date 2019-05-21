package ui.classic;

import static ui.FontType.GAME_NAME;
import static ui.FontType.INTENSE;
import static ui.FontType.NORMAL;
import static ui.FontType.SHORTCUT;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.annotation.Nonnull;

import types.GoldboxString;
import types.MenuType;
import ui.FontType;
import ui.UIFrame;
import ui.UIResources;
import ui.UISettings;

public abstract class AbstractRenderer {

	protected UIResources resources;
	protected UISettings settings;

	private FrameRenderer frameRenderer;

	public AbstractRenderer(@Nonnull UIResources resources, @Nonnull UISettings settings, @Nonnull FrameRenderer frameRenderer) {
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

	protected void renderChar(@Nonnull Graphics2D g2d, int x, int y, byte c, @Nonnull FontType textFont) {
		BufferedImage ci = resources.getFont(textFont).get(c);
		renderImage(g2d, ci, x, y);
	}

	protected void renderText(@Nonnull Graphics2D g2d) {
		resources.getCharList().ifPresent(text -> {
			int charStop = resources.getCharStop();
			if (!text.isEmpty() && charStop != 0) {
				for (int pos = 0; pos < charStop; pos++) {
					int x = getTextStartX() + (pos % getLineWidth());
					int y = getTextStartY() + (pos / getLineWidth());
					renderChar(g2d, x, y, text.get(pos), NORMAL);
				}
			}
		});
	}

	protected void renderStatus(@Nonnull Graphics2D g2d) {
		resources.getStatusLine().ifPresent(status -> {
			for (int pos = 0; pos < status.getLength(); pos++) {
				renderChar(g2d, pos, 24, status.getChar(pos), NORMAL);
			}
		});
	}

	protected void renderMenu(@Nonnull Graphics2D g2d) {
		resources.getMenu().ifPresent(menu -> {
			switch (menu.getType()) {
				case HORIZONTAL:
					menu.getDescription().ifPresent(desc -> {
						for (int pos = 0; pos < desc.getLength(); pos++) {
							renderChar(g2d, pos, 24, desc.getChar(pos), GAME_NAME);
						}
					});
					int menuStart = menu.getDescription().map(desc -> desc.getLength() + 1).orElse(0);
					for (int i = 0; i < menu.getItemCount(); i++) {
						GoldboxString menuName = menu.getMenuItem(i);
						for (int charIndex = 0; charIndex < menuName.getLength(); charIndex++) {
							renderChar(g2d, menuStart + charIndex, 24, menuName.getChar(charIndex),
								menu.isSelected(i) ? INTENSE : charIndex == 0 ? SHORTCUT : NORMAL);
						}
						menuStart += menuName.getLength() + 1;
					}
					break;
				case VERTICAL:
					menu.getDescription().ifPresent(desc -> {
						for (int pos = 0; pos < desc.getLength(); pos++) {
							renderChar(g2d, 1 + pos, 17, desc.getChar(pos), NORMAL);
						}
					});
					int firstLine = menu.getDescription().map(desc -> 18).orElse(17);
					for (int i = 0; i < menu.getItemCount(); i++) {
						GoldboxString menuName = menu.getMenuItem(i);
						for (int pos = 0; pos < menuName.getLength(); pos++) {
							renderChar(g2d, 1 + pos, firstLine + i, menuName.getChar(pos),
								menu.isSelected(i) ? INTENSE : pos == 0 ? SHORTCUT : NORMAL);
						}
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
