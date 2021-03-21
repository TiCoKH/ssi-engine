package ui.classic;

import static shared.FontColor.NORMAL;

import java.awt.Graphics2D;

import javax.annotation.Nonnull;

import ui.UISettings;
import ui.shared.resource.UIResourceManager;
import ui.shared.text.GoldboxStringInput;

public class TextInputRenderer extends AbstractRenderer {

	public TextInputRenderer(UISettings settings, UIResourceManager resman, AbstractFrameRenderer frameRenderer) {
		super(settings, resman, frameRenderer);
	}

	public void render(@Nonnull Graphics2D g2d, @Nonnull GoldboxStringInput input) {
		frameRenderer.clearStatusLine(g2d);
		renderString(g2d, input, 0, 24, NORMAL);
	}
}
