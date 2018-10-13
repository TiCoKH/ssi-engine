package ui.classic;

import java.awt.Graphics2D;
import java.awt.Image;

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
	public void render(@Nonnull Graphics2D g2d) {
		renderTitle(g2d);
		renderStatus(g2d);
	}

	private void renderTitle(@Nonnull Graphics2D g2d) {
		resources.getPic().ifPresent(title -> {
			int x = (320 - title.getWidth()) / 2;
			int y = (200 - title.getHeight()) / 2;
			Image scaled = title.getScaledInstance(zoom(title.getWidth()), zoom(title.getHeight()), 0);
			g2d.drawImage(scaled, zoom(x), zoom(y), null);
		});
	}
}
