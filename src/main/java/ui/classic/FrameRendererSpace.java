package ui.classic;

import java.awt.Graphics2D;

import ui.UISettings;
import ui.shared.UIFrame;
import ui.shared.resource.UIResourceConfiguration;
import ui.shared.resource.UIResourceManager;

public class FrameRendererSpace extends FrameRendererSymbols {

	public FrameRendererSpace(UIResourceConfiguration config, UIResourceManager resman, UISettings settings) {
		super(config, resman, settings);
	}

	@Override
	protected void renderSHEETFrame(Graphics2D g2d) {
		renderHorizontalFrameBorder(g2d, parseFrameIndexes(config.getOuterFrameBottom(UIFrame.SHEET)), 0, 13);
		renderOuterFrame(g2d, UIFrame.SHEET);
		renderHorizontalFrameBorder(g2d, parseFrameIndexes(config.getInnerFrameHorizontal(UIFrame.SHEET)), 1, 8);
		renderVerticalFrameBorder(g2d, parseFrameIndexes(config.getInnerFrameVertical(UIFrame.SHEET)), 11, 13);
	}
}
