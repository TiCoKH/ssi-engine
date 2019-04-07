package ui.classic;

import static ui.FontType.NORMAL;
import static ui.UIFrame.GAME;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.annotation.Nonnull;

import data.content.DungeonMap.VisibleWalls;
import data.content.WallDef;
import data.content.WallDef.WallDistance;
import data.content.WallDef.WallPlacement;
import types.GoldboxString;
import ui.UIResources;
import ui.UISettings;

public class DungeonRenderer extends StoryRenderer {
	// Order is FORWARD(FAR,MEDIUM,CLOSE), LEFT(FAR,MEDIUM,CLOSE), RIGHT(FAR,MEDIUM,CLOSE)
	private static final int[] WALL_START_X = { 2, 1, -2, 3, 2, 3, 9, 10, 12 };
	private static final int[] WALL_START_Y = { 7, 6, 4, 6, 4, 3, 6, 4, 3 };
	private static final int[] WALL_WIDTH = { 2, 3, 7, 2, 3, 0, 2, 3, 0 };

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

			r.getWalls().ifPresent(walls -> {
				renderVisibleWalls(g2d, r.getVisibleWalls(), walls, r.getWallSymbols());
			});

			r.getSprite().ifPresent(sprite -> {
				renderImage(g2d, sprite, 3, 3);
			});
		});
	}

	private void renderVisibleWalls(Graphics2D g2d, VisibleWalls vwalls, WallDef walls, List<BufferedImage> wallSymbols) {
		for (WallDistance dis : WallDistance.values()) {
			for (WallPlacement plc : WallPlacement.values()) {
				int[] vwallindexes = vwalls.getVisibleWall(dis, plc);
				int xStart = WALL_START_X[3 * plc.ordinal() + dis.ordinal()];
				int yStart = WALL_START_Y[3 * plc.ordinal() + dis.ordinal()];
				for (int i = 0; i < vwallindexes.length; i++) {
					if (vwallindexes[i] > 0) {
						int[][] wall = walls.getWallDisplay(vwallindexes[i] - 1, dis, plc);
						renderWall(g2d, wall, xStart, yStart, wallSymbols);
						if (dis == WallDistance.FAR && plc == WallPlacement.FOWARD && i + 1 < vwallindexes.length && vwallindexes[i + 1] > 0) {
							int[][] farFiller = walls.getWallDisplayFarFiller(vwallindexes[i] - 1);
							renderWall(g2d, farFiller, xStart + 1, yStart, wallSymbols);
						}
					}
					xStart += WALL_WIDTH[3 * plc.ordinal() + dis.ordinal()];
				}
			}
		}
	}

	private void renderWall(Graphics2D g2d, int[][] wallDisplay, int xStart, int yStart, List<BufferedImage> wallSymbols) {
		for (int y = 0; y < wallDisplay.length; y++) {
			int[] row = wallDisplay[y];
			int xBegin = xStart < 3 ? 3 - xStart : 0;
			int xEnd = xStart + row.length > 13 ? 14 - xStart : row.length;
			for (int x = xBegin; x < xEnd; x++) {
				renderImage(g2d, wallSymbols.get(row[x]), xStart + x, yStart + y);
			}
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
