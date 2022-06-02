package ui.classic;

import static data.image.ImageContentProperties.X_OFFSET;
import static data.image.ImageContentProperties.Y_OFFSET;
import static data.palette.Palette.COLOR_GAME_STATIC;
import static shared.FontColor.NORMAL;
import static ui.shared.UIFrame.GAME;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Optional;

import javax.annotation.Nonnull;

import io.vavr.collection.Seq;

import data.dungeon.DungeonMap.VisibleWalls;
import data.dungeon.WallDef.WallDistance;
import data.dungeon.WallDef.WallPlacement;
import ui.UISettings;
import ui.classic.RendererState.DungeonResources;
import ui.shared.dungeon.DungeonWall;
import ui.shared.resource.UIResourceManager;

public class DungeonRenderer extends StoryRenderer {
	// Order is FORWARD(FAR,MEDIUM,CLOSE), LEFT(FAR,MEDIUM,CLOSE), RIGHT(FAR,MEDIUM,CLOSE)
	private static final int[] WALL_START_X = { 2, 1, -2, 3, 2, 3, 9, 10, 12 };
	private static final int[] WALL_START_Y = { 7, 6, 4, 6, 4, 3, 6, 4, 3 };
	private static final int[] WALL_SPACING = { 2, 3, 7, 2, 3, 0, 2, 3, 0 };
	private static final int[] WALL_MAX_HEIGHT = { 16, 32, 64, 32, 64, 88, 32, 64, 88 };

	public DungeonRenderer(@Nonnull RendererState resources, @Nonnull UISettings settings,
		@Nonnull UIResourceManager resman, @Nonnull AbstractFrameRenderer frameRenderer) {

		super(resources, settings, resman, frameRenderer);
	}

	@Override
	public void render(@Nonnull Graphics2D g2d) {
		renderFrame(g2d, GAME);
		renderParty(g2d, state.getGlobalData(), 17);
		renderMenuOrTextStatus(g2d);
		Optional<DungeonResources> r = state.getDungeonResources();
		if (r.isPresent() && r.get().isShowRunicText()) {
			renderRunicText(g2d, r.get());
		} else if (state.getPic().isPresent() && !state.preferSprite()) {
			renderPicture(g2d, 3);
		} else {
			renderDungeon(g2d);
		}
		renderPosition(g2d);
	}

	private void renderDungeon(@Nonnull Graphics2D g2d) {
		state.getDungeonResources().ifPresent(r -> {

			if (r.isShowAreaMap()) {
				r.getMap().ifPresent(map -> renderMap(g2d, r, map));
			} else {
				renderBackdrop(g2d, r);
				r.getVisibleWalls().ifPresent(vw -> renderVisibleWalls(g2d, vw, r.getWalls()));

				r.getSprite().ifPresent(sprite -> {
					int xOffset = Math.abs((int) sprite.getProperty(X_OFFSET.name()));
					int yOffset = Math.abs((int) sprite.getProperty(Y_OFFSET.name()));
					int x = settings.zoom8(3) + settings.zoom(xOffset);
					int y = settings.zoom8(3) + settings.zoom(yOffset);
					g2d.drawImage(sprite, x, y, null);
				});
			}
		});
	}

	private void renderBackdrop(@Nonnull Graphics2D g2d, @Nonnull DungeonResources res) {
		switch (res.getBackdropMode()) {
			case COLOR:
				g2d.setBackground(
					COLOR_GAME_STATIC[res.isOutside() ? res.getSkyColorOutdoors() : res.getSkyColorIndoors()]);
				g2d.clearRect(settings.zoom8(3), settings.zoom8(3), settings.zoom8(11), settings.zoom(43));
				g2d.setBackground(COLOR_GAME_STATIC[15]);
				g2d.clearRect(settings.zoom8(3), settings.zoom(67), settings.zoom8(11), settings.zoom(1));
				g2d.setBackground(COLOR_GAME_STATIC[7]);
				g2d.clearRect(settings.zoom8(3), settings.zoom(68), settings.zoom8(11), settings.zoom(2));
				g2d.setBackground(COLOR_GAME_STATIC[6]);
				g2d.clearRect(settings.zoom8(3), settings.zoom(70), settings.zoom8(11), settings.zoom(42));
				break;
			case SKY:
				renderImage(g2d, res.getBackdrop(2), 3, 8);
				g2d.setBackground(
					COLOR_GAME_STATIC[res.isOutside() ? res.getSkyColorOutdoors() : res.getSkyColorIndoors()]);
				g2d.clearRect(settings.zoom8(3), settings.zoom8(3), settings.zoom8(11), settings.zoom(44));
				g2d.setBackground(COLOR_GAME_STATIC[7]);
				g2d.clearRect(settings.zoom8(3), settings.zoom(69), settings.zoom8(11), settings.zoom(2));
				break;
			case SKYGRND:
				break;
			case SPACE:
				renderImage(g2d, res.getBackdrop(res.isOutside() ? 0 : 1), 3, 3);
				break;
			case GEO2:
				break;
		}
	}

	private void renderVisibleWalls(Graphics2D g2d, VisibleWalls vwalls, Seq<DungeonWall> walls) {
		g2d.clipRect(settings.zoom8(3), settings.zoom8(3), settings.zoom8(11), settings.zoom8(11));
		for (WallDistance dis : WallDistance.values()) {
			for (WallPlacement plc : WallPlacement.values()) {
				int[] vwallindexes = vwalls.getVisibleWall(dis, plc);
				int xStart = WALL_START_X[3 * plc.ordinal() + dis.ordinal()];
				for (int i = 0; i < vwallindexes.length; i++) {
					boolean renderFiller = dis == WallDistance.FAR && plc == WallPlacement.FOWARD
						&& i + 1 < vwallindexes.length && vwallindexes[i + 1] > 0;
					if (vwallindexes[i] > 0) {
						renderWall(g2d, walls, vwallindexes[i], xStart, plc, dis, renderFiller);
					}
					xStart += WALL_SPACING[3 * plc.ordinal() + dis.ordinal()];
				}
			}
		}
		g2d.setClip(null);
	}

	private void renderWall(@Nonnull Graphics2D g2d, @Nonnull Seq<DungeonWall> walls, int index, int xStart,
		WallPlacement plc, WallDistance dis, boolean renderFarfillerNext) {

		int yStart = WALL_START_Y[3 * plc.ordinal() + dis.ordinal()];
		int maxHeight = WALL_MAX_HEIGHT[3 * plc.ordinal() + dis.ordinal()];

		DungeonWall wall = walls.get(index - 1);
		BufferedImage wallView = wall.getWallViewFor(dis, plc);
		g2d.drawImage(wallView, settings.zoom8(xStart),
			settings.zoom8(yStart) + settings.zoom(maxHeight) - wallView.getHeight(), null);

		if (renderFarfillerNext) {
			wallView = wall.getFarFiller();
			g2d.drawImage(wallView, settings.zoom8(xStart + 1),
				settings.zoom8(yStart) + settings.zoom(maxHeight) - wallView.getHeight(), null);
		}
	}

	private void renderMap(Graphics2D g2d, @Nonnull DungeonResources res, BufferedImage map) {
		g2d.clipRect(settings.zoom8(3), settings.zoom8(3), settings.zoom8(11), settings.zoom8(11));

		int posX = res.getPositionX();
		int posY = res.getPositionY();

		int startX = posX > 4 ? posX >= (res.getMapWidth() - 5) ? 14 - res.getMapWidth() : 8 - posX : 3;
		int startY = posY > 4 ? posY >= (res.getMapHeight() - 5) ? 14 - res.getMapHeight() : 8 - posY : 3;
		renderImage(g2d, map, startX, startY);

		int arrowX = posX > 4 ? posX >= (res.getMapWidth() - 5) ? 8 + posX - (res.getMapWidth() - 6) : 8 : 3 + posX;
		int arrowY = posY > 4 ? posY >= (res.getMapHeight() - 5) ? 8 + posY - (res.getMapHeight() - 6) : 8 : 3 + posY;
		renderImage(g2d, res.getMapArrow(), arrowX, arrowY);

		g2d.setClip(null);
	}

	private void renderRunicText(@Nonnull Graphics2D g2d, @Nonnull DungeonResources res) {
		renderText(g2d, res.getRunicText(), 1, 5, 11, -1, Optional.empty(), 1);
	}

	private void renderPosition(@Nonnull Graphics2D g2d) {
		state.getDungeonResources().ifPresent(r -> {
			renderString(g2d, r.getPositionText(), 17, 15, NORMAL);
		});
	}
}
