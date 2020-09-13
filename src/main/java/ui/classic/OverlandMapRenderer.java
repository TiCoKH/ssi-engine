package ui.classic;

import static ui.shared.UIFrame.BIGPIC;

import java.awt.Graphics2D;

import javax.annotation.Nonnull;

import ui.UISettings;

public class OverlandMapRenderer extends StoryRenderer {

	public OverlandMapRenderer(@Nonnull RendererState resources, @Nonnull UISettings settings, @Nonnull AbstractFrameRenderer frameRenderer) {
		super(resources, settings, frameRenderer);
	}

	@Override
	public void render(@Nonnull Graphics2D g2d) {
		renderFrame(g2d, BIGPIC);
		renderMap(g2d);
		renderMenuOrTextStatus(g2d);
	}

	protected void renderMap(@Nonnull Graphics2D g2d) {
		state.getOverlandResources().ifPresent(r -> {
			renderImage(g2d, r.getMap(), 1, 1);
			renderImage(g2d, r.getCursor(), 1 + r.getCursorX(), 1 + r.getCursorY());
		});
	}
}
