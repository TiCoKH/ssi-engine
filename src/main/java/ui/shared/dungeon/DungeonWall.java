package ui.shared.dungeon;

import java.awt.image.BufferedImage;

import javax.annotation.Nonnull;

import io.vavr.collection.Map;

import data.dungeon.WallDef.WallDistance;
import data.dungeon.WallDef.WallPlacement;

public class DungeonWall {
	private final Map<WallDistance, Map<WallPlacement, BufferedImage>> wallViewsMap;

	private final BufferedImage farFiller;

	public DungeonWall(@Nonnull Map<WallDistance, Map<WallPlacement, BufferedImage>> wallViewsMap,
		@Nonnull BufferedImage farFiller) {
		this.wallViewsMap = wallViewsMap;
		this.farFiller = farFiller;
	}

	public BufferedImage getWallViewFor(@Nonnull WallDistance dis, @Nonnull WallPlacement plc) {
		return this.wallViewsMap.get(dis)
			.getOrElseThrow(IllegalStateException::new)
			.get(plc)
			.getOrElseThrow(IllegalStateException::new);
	}

	public BufferedImage getFarFiller() {
		return farFiller;
	}
}
