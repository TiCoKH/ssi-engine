package ui.classic;

import static ui.shared.UIFrame.SPACE;

import java.awt.Graphics2D;

import javax.annotation.Nonnull;

import shared.ViewSpacePosition.Celestial;
import ui.UISettings;
import ui.shared.resource.UIResourceManager;

public class SpaceTravelRenderer extends StoryRenderer {
	protected static final int TEXT_LINE_WIDTH = 15;

	public SpaceTravelRenderer(@Nonnull RendererState resources, @Nonnull UISettings settings, @Nonnull UIResourceManager resman,
		@Nonnull AbstractFrameRenderer frameRenderer) {

		super(resources, settings, resman, frameRenderer);
	}

	@Override
	public int getLineWidth() {
		return TEXT_LINE_WIDTH;
	}

	@Override
	public void render(@Nonnull Graphics2D g2d) {
		renderFrame(g2d, SPACE);
		renderSpace(g2d);
		renderPicture(g2d, 3);
		renderMenuOrTextStatus(g2d);
	}

	protected void renderSpace(@Nonnull Graphics2D g2d) {
		state.getSpaceResources().ifPresent(r -> {
			renderImage(g2d, r.getBackground(), 17, 1);
			renderImage(g2d, r.getSun(), 28, 11);
			for (Celestial c : Celestial.values()) {
				renderImage(g2d, r.getCelestial(c), r.getCelestialX(c), r.getCelestialY(c));
			}
			renderImage(g2d, r.getShip(), r.getShipX(), r.getShipY());
		});
	}
}
