package ui.classic;

import static ui.classic.ClassicBorders.SPACE;

import java.awt.Graphics2D;

import javax.annotation.Nonnull;

import engine.ViewSpacePosition.Celestial;
import ui.UIResources;
import ui.UISettings;

public class SpaceTravelRenderer extends StoryRenderer {
	protected static final int TEXT_LINE_WIDTH = 15;

	public SpaceTravelRenderer(@Nonnull UIResources resources, @Nonnull UISettings setting) {
		super(resources, setting);
	}

	@Override
	public int getLineWidth() {
		return TEXT_LINE_WIDTH;
	}

	@Override
	public void render(@Nonnull Graphics2D g2d) {
		renderBorders(g2d, SPACE);
		renderSpace(g2d);
		renderPicture(g2d, 3);
		renderText(g2d);
		renderStatus(g2d);
	}

	protected void renderSpace(@Nonnull Graphics2D g2d) {
		resources.getSpaceResources().ifPresent(r -> {
			renderImage(g2d, r.getBackground(), 17, 1);
			renderImage(g2d, r.getSun(), 28, 11);
			for (Celestial c : Celestial.values()) {
				renderImage(g2d, r.getCelestial(c), r.getCelestialX(c), r.getCelestialY(c));
			}
			renderImage(g2d, r.getShip(), r.getShipX(), r.getShipY());
		});
	}
}
