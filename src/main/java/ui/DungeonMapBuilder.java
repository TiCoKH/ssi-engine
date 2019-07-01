package ui;

import static data.content.DAXContentType._8X8D;

import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DirectColorModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

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
	public BufferedImage build() throws IOException {
		int height = map.length << 3;
		int width = map[0].length << 3;

		List<BufferedImage> mapTiles = buildMapTiles();

		ColorModel cm = new DirectColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), 32, //
			0xff0000, 0x00ff00, 0x0000ff, 0xff000000, true, DataBuffer.TYPE_INT);
		WritableRaster r = cm.createCompatibleWritableRaster(width, height);
		BufferedImage result = new BufferedImage(cm, r, true, null);

		Graphics2D g2d = result.createGraphics();
		for (int y = 0; y < map.length; y++) {
			int[] row = map[y];
			for (int x = 0; x < row.length; x++) {
				try {
					BufferedImage s = mapTiles.get(row[x]);
					g2d.drawImage(s, x << 3, y << 3, null);
				} catch (Exception e) {
				}
			}
		}
		return result;
	}

	private List<BufferedImage> buildMapTiles() throws IOException {
		if (isWithoutMapDecoIds()) {
			return loader.getMisc().subList(config.getMiscAreaMapIndex(), config.getMiscAreaMapIndex() + 16);
		}
		List<BufferedImage> result = new ArrayList<>();

		result.addAll(loader.findImage(id1, _8X8D).toList());
		result.addAll(loader.findImage(id2, _8X8D).toList());
		result.addAll(loader.findImage(id3, _8X8D).toList());

		return result;
	}

	private boolean isWithoutMapDecoIds() {
		return id1 == -1 && id2 == -1 && id3 == -1;
	}
}
