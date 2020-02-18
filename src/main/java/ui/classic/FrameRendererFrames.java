package ui.classic;

import static data.image.ImageContentProperties.X_OFFSET;
import static data.image.ImageContentProperties.Y_OFFSET;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.annotation.Nonnull;

import ui.UIFrame;
import ui.UIResourceConfiguration;
import ui.UIResourceManager;
import ui.UISettings;

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
			renderFramePart(g2d, parseFrameIndexes(config.getInnerFrameHorizontal(f)), 0, 128);
		}
		if (UIFrame.GAME.equals(f)) {
			renderFramePart(g2d, parseFrameIndexes(config.getInnerFrameHorizontal(f)), 0, 96);
			renderFramePart(g2d, parseFrameIndexes(config.getInnerFrameHorizontal(f)), 0, 128);
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
