package ui.classic;

import java.awt.Graphics2D;

import javax.annotation.Nonnull;

import ui.UISettings;
import ui.shared.resource.UIResourceManager;

public abstract class AbstractDialogRenderer extends AbstractRenderer {

	protected AbstractDialogRenderer(@Nonnull UISettings settings, @Nonnull UIResourceManager resman, @Nonnull AbstractFrameRenderer frameRenderer) {
		super(settings, resman, frameRenderer);
	}

	public abstract void render(@Nonnull Graphics2D g2d, @Nonnull AbstractDialogState state);
}
