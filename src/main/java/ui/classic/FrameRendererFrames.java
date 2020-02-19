package ui.classic;

import static data.image.ImageContentProperties.X_OFFSET;
import static data.image.ImageContentProperties.Y_OFFSET;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.annotation.Nonnull;

import ui.UISettings;
import ui.shared.UIFrame;
import ui.shared.resource.UIResourceConfiguration;
import ui.shared.resource.UIResourceManager;

public class FrameRendererFrames extends AbstractFrameRenderer {

	public FrameRendererFrames(UIResourceConfiguration config, UIResourceManager resman, UISettings settings) {
		super(config, resman, settings);
	}

	@Override
	protected void renderFrame(@Nonnull Graphics2D g2d, @Nonnull UIFrame f) {
		if (!UIFrame.NONE.equals(f)) {
			renderFramePart(g2d, parseFrameIndexes(config.getOuterFrameTop(f)));
			renderFramePart(g2d, parseFrameIndexes(config.getOuterFrameBottom(f)));
			renderFramePart(g2d, parseFrameIndexes(config.getOuterFrameLeft(f)));
			renderFramePart(g2d, parseFrameIndexes(config.getOuterFrameRight(f)));
		}

		if (UIFrame.BIGPIC.equals(f)) {
			renderFramePart(g2d, parseFrameIndexes(config.getInnerFrameHorizontal(f)), 0, 16 * 8);
		}
		if (UIFrame.SHEET.equals(f)) {
			renderFramePart(g2d, parseFrameIndexes(config.getInnerFrameHorizontal(f)), 0, 8 * 8);
			renderFramePart(g2d, parseFrameIndexes(config.getInnerFrameHorizontal(f)), 0, 16 * 8);
			renderFramePart(g2d, parseFrameIndexes(config.getInnerFrameHorizontal(f)), 0, 20 * 8);
			renderFramePart(g2d, parseFrameIndexes(config.getInnerFrameVertical(f)), 19 * 8, 8 * 8 + 5);
		}
		if (UIFrame.GAME.equals(f)) {
			renderFramePart(g2d, parseFrameIndexes(config.getInnerFrameHorizontal(f)), 0, 12 * 8);
			renderFramePart(g2d, parseFrameIndexes(config.getInnerFrameHorizontal(f)), 0, 16 * 8);
			renderFramePart(g2d, parseFrameIndexes(config.getPortraitTop()));
			renderFramePart(g2d, parseFrameIndexes(config.getPortraitLeft()));
		}
	}

	private void renderFramePart(Graphics2D g2d, int[] indexes) {
		List<BufferedImage> frames = resman.getFrames();
		for (int i = 0; i < indexes.length; i++) {
			BufferedImage part = frames.get(indexes[i]);
			int x = Math.abs((int) part.getProperty(X_OFFSET.name()));
			int y = Math.abs((int) part.getProperty(Y_OFFSET.name()));
			g2d.drawImage(part, settings.zoom(x), settings.zoom(y), null);
		}
	}

	private void renderFramePart(Graphics2D g2d, int[] indexes, int x, int y) {
		List<BufferedImage> frames = resman.getFrames();
		for (int i = 0; i < indexes.length; i++) {
			BufferedImage part = frames.get(indexes[i]);
			g2d.drawImage(part, settings.zoom(x), settings.zoom(y), null);
		}
	}
}
