package ui.classic;

import static ui.shared.UIFrame.NONE;

import java.awt.Graphics2D;

import javax.annotation.Nonnull;

import ui.UISettings;

public class TitleRenderer extends AbstractRenderer {

	public TitleRenderer(@Nonnull RendererState resources, @Nonnull UISettings settings, @Nonnull AbstractFrameRenderer frameRenderer) {
		super(resources, settings, frameRenderer);
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
		renderFrame(g2d, NONE);
		renderTitle(g2d);
		renderMenuOrTextStatus(g2d);
	}

	private void renderTitle(@Nonnull Graphics2D g2d) {
		state.getPic().ifPresent(title -> {
			int x = (settings.zoom(320) - title.getWidth(null)) / 2;
			int y = (settings.zoom(200) - title.getHeight(null)) / 2;
			g2d.drawImage(title, x, y, null);
		});
	}
}
