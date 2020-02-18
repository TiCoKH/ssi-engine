package ui.classic;

import java.awt.Graphics2D;

import javax.annotation.Nonnull;

import ui.UIFrame;
import ui.UIResourceConfiguration;
import ui.UIResourceManager;
import ui.UISettings;

public class FrameRendererPortraitFrame extends FrameRendererSymbols {

	public FrameRendererPortraitFrame(UIResourceConfiguration config, UIResourceManager resman, UISettings settings) {
		super(config, resman, settings);
	}

	@Override
	protected void renderGAMEFrame(Graphics2D g2d) {
		renderHorizontalFrameBorder(g2d, parseFrameIndexes(config.getOuterFrameTop(UIFrame.GAME)), 17, 0);
		renderHorizontalFrameBorder(g2d, parseFrameIndexes(config.getOuterFrameBottom(UIFrame.GAME)), 0, 23);
		renderVerticalFrameBorder(g2d, parseFrameIndexes(config.getOuterFrameLeft(UIFrame.GAME)), 0, 17);
		renderVerticalFrameBorder(g2d, parseFrameIndexes(config.getOuterFrameRight(UIFrame.GAME)), 39, 1);

		renderHorizontalFrameBorder(g2d, parseFrameIndexes(config.getInnerFrameHorizontal(UIFrame.GAME)), 17, 16);

		if (isPortraitShown())
			renderPortrait(g2d);
	}

	@Override
	protected void renderPortrait(@Nonnull Graphics2D g2d) {
		renderImage(g2d, config.getPortraitTop(), 0, 0);
		renderImage(g2d, config.getPortraitBottom(), 0, 14);
		renderImage(g2d, config.getPortraitLeft(), 0, 3);
		renderImage(g2d, config.getPortraitRight(), 14, 3);
	}
}
