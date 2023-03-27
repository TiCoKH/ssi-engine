package ui.shared.dungeon;

import static data.ContentType._8X8D;

import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DirectColorModel;
import java.awt.image.WritableRaster;

import javax.annotation.Nonnull;

import io.vavr.collection.Seq;

import data.Resource;
import data.image.ImageContent;
import ui.shared.resource.UIResourceConfiguration;
import ui.shared.resource.UIResourceLoader;

public class DungeonMapBuilder {
	private UIResourceConfiguration config;
	private UIResourceLoader loader;

	private int id1;
	private int id2;
	private int id3;

	private int[][] map;

	public DungeonMapBuilder(@Nonnull UIResourceConfiguration config, @Nonnull UIResourceLoader loader) {
		this.config = config;
		this.loader = loader;
	}

	public DungeonMapBuilder withoutMapDecoIds() {
		this.id1 = -1;
		this.id2 = -1;
		this.id3 = -1;
		return this;
	}

	public DungeonMapBuilder withWMapDecoIds(int id1, int id2, int id3) {
		this.id1 = id1;
		this.id2 = id2;
		this.id3 = id3;
		return this;
	}

	public DungeonMapBuilder withMap(int[][] map) {
		this.map = map;
		return this;
	}

	@Nonnull
	public Resource<BufferedImage> build() {
		return buildMapTiles().map(this::buildMap);
	}

	private Resource<Seq<BufferedImage>> buildMapTiles() {
		if (isWithoutMapDecoIds()) {
			return loader.getMisc()
				.map(misc -> misc.subList(config.getMiscAreaMapIndex(), config.getMiscAreaMapIndex() + 16));
		}

		return loader.findImage(id1, _8X8D).map(ImageContent::toSeq).flatMap(seq -> {
			return loader.findImage(id2, _8X8D).map(ic -> seq.appendAll(ic.toSeq())).flatMap(seq2 -> {
				return loader.findImage(id3, _8X8D).map(ic -> seq2.appendAll(ic.toSeq()));
			});
		});
	}

	private BufferedImage buildMap(Seq<BufferedImage> mapTiles) {
		final int height = map.length << 3;
		final int width = map[0].length << 3;

		final ColorModel cm = new DirectColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), 32, //
			0xff0000, 0x00ff00, 0x0000ff, 0xff000000, true, DataBuffer.TYPE_INT);
		final WritableRaster r = cm.createCompatibleWritableRaster(width, height);
		final BufferedImage result = new BufferedImage(cm, r, true, null);

		final Graphics2D g2d = result.createGraphics();
		for (int y = 0; y < map.length; y++) {
			int[] row = map[y];
			for (int x = 0; x < row.length; x++) {
				final BufferedImage s = mapTiles.get(row[x]);
				g2d.drawImage(s, x << 3, y << 3, null);
			}
		}
		return result;
	}

	private boolean isWithoutMapDecoIds() {
		return id1 == -1 && id2 == -1 && id3 == -1;
	}
}
