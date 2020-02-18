package ui.classic;

import static ui.UIFrame.GAME;

import java.awt.Graphics2D;

import javax.annotation.Nonnull;

import ui.UIResources;
import ui.UISettings;

public class StoryRenderer extends AbstractRenderer {
	protected static final int TEXT_START_X = 1;
	protected static final int TEXT_START_Y = 17;
	protected static final int TEXT_LINE_WIDTH = 38;

	public StoryRenderer(@Nonnull UIResources resources, @Nonnull UISettings settings, @Nonnull AbstractFrameRenderer frameRenderer) {
		super(resources, settings, frameRenderer);
	}

	@Override
	public int getLineWidth() {
		return TEXT_LINE_WIDTH;
	}

	@Override
	public int getTextStartX() {
		return TEXT_START_X;
	}

	@Override
	public int getTextStartY() {
		return TEXT_START_Y;
	}

	@Override
	public void render(@Nonnull Graphics2D g2d) {
		renderFrame(g2d, GAME);
		renderPicture(g2d, 3);
		renderMenuOrTextStatus(g2d);
	}

	protected void renderPicture(@Nonnull Graphics2D g2d, int start) {
		resources.getPic().ifPresent(pic -> {
			renderImage(g2d, pic, start, start);
		});
	}
}
