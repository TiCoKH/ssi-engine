package ui.classic;

import static ui.classic.ClassicBorders.BIGPIC;

import java.awt.Graphics2D;

import javax.annotation.Nonnull;

import ui.UIResources;
import ui.UISettings;

public class BigPicRenderer extends StoryRenderer {

	public BigPicRenderer(@Nonnull UIResources resources, @Nonnull UISettings setting) {
		super(resources, setting);
	}

	@Override
	public void render(Graphics2D g2d) {
		renderBorders(g2d, BIGPIC);
		renderPicture(g2d, 1);
		renderMenuOrTextStatus(g2d);
	}
}
