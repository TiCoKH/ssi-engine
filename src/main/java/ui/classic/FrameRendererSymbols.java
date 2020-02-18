package ui.classic;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.annotation.Nonnull;

import ui.UIFrame;
import ui.UIResourceConfiguration;
import ui.UIResourceManager;
import ui.UISettings;

public class FrameRendererSymbols extends AbstractFrameRenderer {

	public FrameRendererSymbols(UIResourceConfiguration config, UIResourceManager resman, UISettings settings) {
		super(config, resman, settings);
	}

	@Override
	protected void renderFrame(@Nonnull Graphics2D g2d, @Nonnull UIFrame f) {
		switch (f) {
			case GAME:
				renderGAMEFrame(g2d);
				break;
			case SPACE:
				renderSPACEFrame(g2d);
				break;
			case BIGPIC:
				renderBIGPICFrame(g2d);
				break;
			case SCREEN:
				renderSCREENFrame(g2d);
				break;
			default:
				break;
		}
	}

	protected void renderOuterFrame(@Nonnull Graphics2D g2d, @Nonnull UIFrame f) {
		renderHorizontalFrameBorder(g2d, parseFrameIndexes(config.getOuterFrameTop(f)), 0, 0);
		renderHorizontalFrameBorder(g2d, parseFrameIndexes(config.getOuterFrameBottom(f)), 0, 23);
		renderVerticalFrameBorder(g2d, parseFrameIndexes(config.getOuterFrameLeft(f)), 0, 1);
		renderVerticalFrameBorder(g2d, parseFrameIndexes(config.getOuterFrameRight(f)), 39, 1);
	}

	protected void renderSCREENFrame(@Nonnull Graphics2D g2d) {
		renderOuterFrame(g2d, UIFrame.SCREEN);
	}

	protected void renderGAMEFrame(@Nonnull Graphics2D g2d) {
		renderOuterFrame(g2d, UIFrame.GAME);
		renderHorizontalFrameBorder(g2d, parseFrameIndexes(config.getInnerFrameHorizontal(UIFrame.GAME)), 1, 16);
		renderVerticalFrameBorder(g2d, parseFrameIndexes(config.getInnerFrameVertical(UIFrame.GAME)), 16, 1);

		if (isPortraitShown())
			renderPortrait(g2d);
	}

	protected void renderBIGPICFrame(@Nonnull Graphics2D g2d) {
		renderOuterFrame(g2d, UIFrame.BIGPIC);
		renderHorizontalFrameBorder(g2d, parseFrameIndexes(config.getInnerFrameHorizontal(UIFrame.BIGPIC)), 1, 16);
	}

	protected void renderSPACEFrame(@Nonnull Graphics2D g2d) {
		renderOuterFrame(g2d, UIFrame.SPACE);
		renderHorizontalFrameBorder(g2d, parseFrameIndexes(config.getInnerFrameHorizontal(UIFrame.SPACE)), 1, 16);
		renderVerticalFrameBorder(g2d, parseFrameIndexes(config.getInnerFrameVertical(UIFrame.SPACE)), 16, 1);

		if (isPortraitShown())
			renderPortrait(g2d);
	}

	protected void renderPortrait(@Nonnull Graphics2D g2d) {
		renderHorizontalFrameBorder(g2d, parseFrameIndexes(config.getPortraitTop()), 2, 2);
		renderHorizontalFrameBorder(g2d, parseFrameIndexes(config.getPortraitBottom()), 2, 14);
		renderVerticalFrameBorder(g2d, parseFrameIndexes(config.getPortraitLeft()), 2, 3);
		renderVerticalFrameBorder(g2d, parseFrameIndexes(config.getPortraitRight()), 14, 3);
	}

	protected void renderHorizontalFrameBorder(Graphics2D g2d, int[] indexes, int startX, int y) {
		List<BufferedImage> frames = resman.getFrames();
		for (int x = 0; x < indexes.length; x++) {
			if (indexes[x] != -1) {
				g2d.drawImage(frames.get(indexes[x]), settings.zoom8(startX + x), settings.zoom8(y), null);
			}
		}
	}

	protected void renderVerticalFrameBorder(Graphics2D g2d, int[] indexes, int x, int startY) {
		List<BufferedImage> frames = resman.getFrames();
		for (int y = 0; y < indexes.length; y++) {
			if (indexes[y] != -1) {
				g2d.drawImage(frames.get(indexes[y]), settings.zoom8(x), settings.zoom8(startY + y), null);
			}
		}
	}
}
