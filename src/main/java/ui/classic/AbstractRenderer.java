package ui.classic;

import static ui.BorderSymbols.EM;
import static ui.FontType.NORMAL;
import static ui.FontType.SHORTCUT;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.annotation.Nonnull;

import engine.opcodes.EclString;
import ui.BorderSymbols;
import ui.FontType;
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

	protected void renderStatus(@Nonnull Graphics2D g2d) {
		resources.getStatusLine().ifPresent(status -> {
			status.getText().ifPresent(text -> {
				for (int pos = 0; pos < text.getLength(); pos++) {
					renderChar(g2d, pos, 24, text.getChar(pos), status.getTextFont());
				}
			});
			status.getMenu().ifPresent(menu -> {
				int pos = status.getText().map(text -> text.getLength() + 1).orElse(0);
				for (EclString menuName : menu) {
					for (int pos2 = 0; pos2 < menuName.getLength(); pos2++) {
						renderChar(g2d, pos + pos2, 24, menuName.getChar(pos2), pos2 == 0 ? SHORTCUT : NORMAL);
					}
					pos += menuName.getLength() + 1;
				}
			});
		});
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
