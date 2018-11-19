package ui.classic;

import static ui.classic.ClassicBorders.BIGPIC;

import java.awt.Graphics2D;

import javax.annotation.Nonnull;

import ui.UIResources;
import ui.UISettings;

public class OverlandMapRenderer extends StoryRenderer {

	public OverlandMapRenderer(@Nonnull UIResources resources, @Nonnull UISettings setting) {
		super(resources, setting);
	}

	@Override
	public void render(@Nonnull Graphics2D g2d) {
		renderBorders(g2d, BIGPIC);
		renderMap(g2d);
		renderText(g2d);
		renderStatus(g2d);
	}

	protected void renderMap(@Nonnull Graphics2D g2d) {
		resources.getOverlandResources().ifPresent(r -> {
			renderImage(g2d, r.getMap(), 1, 1);
			renderImage(g2d, r.getCursor(), 1 + r.getCursorX(), 1 + r.getCursorY());
		});
	}
}
