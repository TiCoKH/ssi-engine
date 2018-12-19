package ui;

import java.awt.image.BufferedImage;
import java.util.Map;

import javax.annotation.Nonnull;

import data.content.WallDef.WallDistance;
import data.content.WallDef.WallPlacement;

public class DungeonWall {
	private Map<WallDistance, Map<WallPlacement, BufferedImage>> wallViewsMap;

	private BufferedImage farFiller;

	public DungeonWall(@Nonnull Map<WallDistance, Map<WallPlacement, BufferedImage>> wallViewsMap, @Nonnull BufferedImage farFiller) {
		this.wallViewsMap = wallViewsMap;
		this.farFiller = farFiller;
	}

	public BufferedImage getWallViewFor(WallDistance dis, WallPlacement plc) {
		return this.wallViewsMap.get(dis).get(plc);
	}

	public BufferedImage getFarFiller() {
		return farFiller;
	}
}
