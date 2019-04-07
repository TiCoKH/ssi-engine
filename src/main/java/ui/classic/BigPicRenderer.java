package ui.classic;

import static ui.UIFrame.BIGPIC;

import java.awt.Graphics2D;

import javax.annotation.Nonnull;

import ui.UIResources;
import ui.UISettings;

public class BigPicRenderer extends StoryRenderer {

	public BigPicRenderer(@Nonnull UIResources resources, @Nonnull UISettings settings, @Nonnull FrameRenderer frameRenderer) {
		super(resources, settings, frameRenderer);
	}

	@Override
	public void render(Graphics2D g2d) {
		renderFrame(g2d, BIGPIC);
		renderPicture(g2d, 1);
		renderMenuOrTextStatus(g2d);
	}
}
