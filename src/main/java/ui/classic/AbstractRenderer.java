package ui.classic;

import static ui.BorderSymbols.EM;
import static ui.FontType.GAME_NAME;
import static ui.FontType.INTENSE;
import static ui.FontType.NORMAL;
import static ui.FontType.SHORTCUT;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.annotation.Nonnull;

import types.GoldboxString;
import ui.BorderSymbols;
import ui.FontType;
import ui.Menu.MenuType;
import ui.UIResources;
import ui.UISettings;

public abstract class AbstractRenderer {

	protected UIResources resources;
	protected UISettings settings;

	public AbstractRenderer(@Nonnull UIResources resources, @Nonnull UISettings settings) {
		this.resources = resources;
		this.settings = settings;
	}

	public abstract int getLineWidth();

	public abstract int getTextStartX();

	public abstract int getTextStartY();

	public abstract void render(@Nonnull Graphics2D g2d);

	protected void renderBorders(@Nonnull Graphics2D g2d, @Nonnull ClassicBorders layout) {
		for (int y = 0; y < 24; y++) {
			BorderSymbols[] row = layout.getSymbols()[y];
			for (int x = 0; x < 40; x++) {
				if (x >= row.length || row[x] == EM) {
					continue;
				}
				BufferedImage s = resources.getBorderSymbols().get(row[x].getIndex());
				renderImage(g2d, s, x, y);
			}
		}
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
		Image scaled = image.getScaledInstance(zoom(image.getWidth()), zoom(image.getHeight()), 0);
		g2d.drawImage(scaled, zoom8(x), zoom8(y), null);
	}

	protected int zoom(int pos) {
		return settings.getZoom() * pos;
	}

	protected int zoom8(int pos) {
		return settings.getZoom() * 8 * pos;
	}
}
