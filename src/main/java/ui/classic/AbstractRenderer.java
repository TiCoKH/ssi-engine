package ui.classic;

import static shared.FontColor.GAME_NAME;
import static shared.FontColor.INTENSE;
import static shared.FontColor.NORMAL;
import static shared.FontColor.SHORTCUT;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.annotation.Nonnull;

import shared.FontColor;
import shared.GoldboxString;
import ui.UISettings;
import ui.shared.Menu;
import ui.shared.UIFrame;
import ui.shared.resource.UIResourceManager;

public abstract class AbstractRenderer {

	protected UISettings settings;
	protected UIResourceManager resman;

	private AbstractFrameRenderer frameRenderer;

	protected AbstractRenderer(@Nonnull UISettings settings, @Nonnull UIResourceManager resman, @Nonnull AbstractFrameRenderer frameRenderer) {
		this.settings = settings;
		this.resman = resman;
		this.frameRenderer = frameRenderer;
	}

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
		BufferedImage ci = resman.getFont(textFont).get(c);
		renderImage(g2d, ci, x, y);
	}

	protected void renderImage(@Nonnull Graphics2D g2d, @Nonnull BufferedImage image, int x, int y) {
		g2d.drawImage(image, settings.zoom8(x), settings.zoom8(y), null);
	}

	protected void renderHorizontalMenu(@Nonnull Graphics2D g2d, @Nonnull Menu menu) {
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
	}
}
