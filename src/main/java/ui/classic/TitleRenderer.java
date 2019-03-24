package ui.classic;

import java.awt.Graphics2D;

import javax.annotation.Nonnull;

import ui.UIResources;
import ui.UISettings;

public class TitleRenderer extends AbstractRenderer {

	public TitleRenderer(@Nonnull UIResources resources, @Nonnull UISettings settings) {
		super(resources, settings);
	}

	@Override
	public int getLineWidth() {
		return 0;
	}

	@Override
	public int getTextStartX() {
		return 0;
	}

	@Override
	public int getTextStartY() {
		return 0;
	}

	@Override
	public void render(@Nonnull Graphics2D g2d) {
		renderTitle(g2d);
		renderMenuOrTextStatus(g2d);
	}

	private void renderTitle(@Nonnull Graphics2D g2d) {
		resources.getPic().ifPresent(title -> {
			int x = (settings.zoom(320) - title.getWidth(null)) / 2;
			int y = (settings.zoom(200) - title.getHeight(null)) / 2;
			g2d.drawImage(title, x, y, null);
		});
	}
}
