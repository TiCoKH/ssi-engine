package ui.classic;

import static ui.FontType.NORMAL;
import static ui.UIFrame.GAME;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.annotation.Nonnull;

import data.content.DungeonMap.VisibleWalls;
import data.content.WallDef.WallDistance;
import data.content.WallDef.WallPlacement;
import types.GoldboxString;
import ui.DungeonWall;
import ui.UIResources;
import ui.UISettings;

public class DungeonRenderer extends StoryRenderer {
	// Order is FORWARD(FAR,MEDIUM,CLOSE), LEFT(FAR,MEDIUM,CLOSE), RIGHT(FAR,MEDIUM,CLOSE)
	private static final int[] WALL_START_X = { 2, 1, -2, 3, 2, 3, 9, 10, 12 };
	private static final int[] WALL_START_Y = { 7, 6, 4, 6, 4, 3, 6, 4, 3 };
	private static final int[] WALL_SPACING = { 2, 3, 7, 2, 3, 0, 2, 3, 0 };
	private static final int[] WALL_MAX_HEIGHT = { 16, 32, 64, 32, 64, 88, 32, 64, 88 };

	public DungeonRenderer(@Nonnull UIResources resources, @Nonnull UISettings settings, @Nonnull FrameRenderer frameRenderer) {
		super(resources, settings, frameRenderer);
	}

	@Override
	public void render(@Nonnull Graphics2D g2d) {
		renderFrame(g2d, GAME);
		renderMenuOrTextStatus(g2d);
		if (resources.getPic().isPresent() && !resources.preferSprite()) {
			renderPicture(g2d, 3);
		} else {
			renderDungeon(g2d);
		}
		renderPosition(g2d);
	}

	private void renderDungeon(@Nonnull Graphics2D g2d) {
		resources.getDungeonResources().ifPresent(r -> {
			renderImage(g2d, r.getBackdrop(), 3, 3);

			renderVisibleWalls(g2d, r.getVisibleWalls(), r.getWalls());

			r.getSprite().ifPresent(sprite -> {
				renderImage(g2d, sprite, 3, 3);
			});
		});
	}

	private void renderVisibleWalls(Graphics2D g2d, VisibleWalls vwalls, List<DungeonWall> walls) {
		g2d.clipRect(settings.zoom8(3), settings.zoom8(3), settings.zoom8(11), settings.zoom8(11));
		for (WallDistance dis : WallDistance.values()) {
			for (WallPlacement plc : WallPlacement.values()) {
				int[] vwallindexes = vwalls.getVisibleWall(dis, plc);
				int xStart = WALL_START_X[3 * plc.ordinal() + dis.ordinal()];
				for (int i = 0; i < vwallindexes.length; i++) {
					boolean renderFiller = dis == WallDistance.FAR && plc == WallPlacement.FOWARD && i + 1 < vwallindexes.length
						&& vwallindexes[i + 1] > 0;
					if (vwallindexes[i] > 0) {
						renderWall(g2d, walls, vwallindexes[i], xStart, plc, dis, renderFiller);
					}
					xStart += WALL_SPACING[3 * plc.ordinal() + dis.ordinal()];
				}
			}
		}
		g2d.setClip(null);
	}

	private void renderWall(@Nonnull Graphics2D g2d, @Nonnull List<DungeonWall> walls, int index, int xStart, WallPlacement plc, WallDistance dis,
		boolean renderFarfillerNext) {

		int yStart = WALL_START_Y[3 * plc.ordinal() + dis.ordinal()];
		int maxHeight = WALL_MAX_HEIGHT[3 * plc.ordinal() + dis.ordinal()];

		DungeonWall wall = walls.get(index - 1);
		BufferedImage wallView = wall.getWallViewFor(dis, plc);
		g2d.drawImage(wallView, settings.zoom8(xStart), settings.zoom8(yStart) + settings.zoom(maxHeight) - wallView.getHeight(), null);

		if (renderFarfillerNext) {
			wallView = wall.getFarFiller();
			g2d.drawImage(wallView, settings.zoom8(xStart + 1), settings.zoom8(yStart) + settings.zoom(maxHeight) - wallView.getHeight(), null);
		}
	}

	private void renderPosition(@Nonnull Graphics2D g2d) {
		resources.getDungeonResources().ifPresent(r -> {
			GoldboxString position = r.getPositionText();
			for (int pos = 0; pos < position.getLength(); pos++) {
				renderChar(g2d, 17 + pos, 15, position.getChar(pos), NORMAL);
			}
		});
	}
}
