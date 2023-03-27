package ui.shared.dungeon;

import static data.ContentType.WALLDEF;
import static data.ContentType._8X8D;
import static data.image.ImageContentProperties.X_OFFSET;
import static data.image.ImageContentProperties.Y_OFFSET;
import static java.util.function.Function.identity;

import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import javax.annotation.Nonnull;

import io.vavr.collection.Array;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;

import data.Resource;
import data.dungeon.WallDef;
import data.dungeon.WallDef.WallDistance;
import data.dungeon.WallDef.WallPlacement;
import data.image.ImageContent;
import data.palette.Palette;
import ui.shared.resource.UIResourceLoader;

public class DungeonWallSetBuilder {
	private UIResourceLoader loader;

	private int id1;
	private int id2;
	private int id3;

	public DungeonWallSetBuilder(@Nonnull UIResourceLoader loader) {
		this.loader = loader;
	}

	public DungeonWallSetBuilder withWallDecoIds(int id1, int id2, int id3) {
		this.id1 = id1;
		this.id2 = id2;
		this.id3 = id3;
		return this;
	}

	@Nonnull
	public Resource<Seq<DungeonWall>> build() {
		if (loader.idsFor(WALLDEF).isEmpty()) {
			return buildWithoutWallDefs();
		}
		return buildUsingWallDefs();
	}

	@Nonnull
	public Resource<Seq<DungeonWall>> buildWithoutWallDefs() {
		Resource<Seq<DungeonWall>> result = Resource.of(Array.empty());
		if (id1 != 127 && id1 != 255)
			result = result.flatMap(s -> buildWithoutWallDefs(id1).map(s::appendAll));
		if (id2 != 127 && id2 != 255)
			result = result.flatMap(s -> buildWithoutWallDefs(id2).map(s::appendAll));
		if (id3 != 127 && id3 != 255)
			result = result.flatMap(s -> buildWithoutWallDefs(id3).map(s::appendAll));

		return result;
	}

	@Nonnull
	public Resource<Seq<DungeonWall>> buildWithoutWallDefs(int id) {
		return loader.findImage(id, _8X8D).map(ic -> {
			final Seq<BufferedImage> wallParts = ic.toSeq();

			final Seq<BufferedImage> wall1 = wallParts.subSequence(0, 10);
			final Seq<BufferedImage> wall2 = wallParts.subSequence(10, 20);

			final Seq<BufferedImage> decal1 = wallParts.subSequence(20, 29);
			final Seq<BufferedImage> decal2 = wallParts.subSequence(29, 38);
			final Seq<BufferedImage> decal3 = wallParts.subSequence(38, 47);

			return Array.of(buildWall(wall1, null), buildWall(wall2, null), buildWall(wall1, decal1),
				buildWall(wall1, decal2), buildWall(wall1, decal3));
		});
	}

	@Nonnull
	private DungeonWall buildWall(Seq<BufferedImage> wall, Seq<BufferedImage> decal) {
		final BufferedImage farFiller = wall.get(0);

		final Map<WallDistance, Map<WallPlacement, BufferedImage>> wallViews = Array.of(WallDistance.values())
			.toMap(identity(), dis -> {
				return Array.of(WallPlacement.values()).toMap(identity(), plc -> {
					final int index = 1 + dis.ordinal() * 3 + plc.ordinal();
					final BufferedImage wallView = wall.get(index);

					if (decal != null) {
						return renderDecalWall(wallView, decal.get(index - 1), WALL_MAX_WIDTH[index],
							WALL_MAX_HEIGHT[index]);
					}
					return wallView;
				});
			});
		return new DungeonWall(wallViews, farFiller);
	}

	private static final int[] WALL_MAX_WIDTH = { 8, 8, 8, 8, 24, 16, 16, 56, 16, 16 };
	private static final int[] WALL_MAX_HEIGHT = { 8, 8, 24, 24, 24, 56, 56, 56, 88, 88 };

	@Nonnull
	private BufferedImage renderDecalWall(BufferedImage wallView, BufferedImage decalView, int maxWidth,
		int maxHeight) {

		final ColorModel cm = new DirectColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), 32, //
			0xff0000, 0x00ff00, 0x0000ff, 0xff000000, false, DataBuffer.TYPE_INT);
		final WritableRaster r = cm.createCompatibleWritableRaster(maxWidth, maxHeight);
		final BufferedImage result = new BufferedImage(cm, r, false, null);

		final Graphics2D g2d = result.createGraphics();
		g2d.drawImage(wallView, maxWidth - wallView.getWidth(), maxHeight - wallView.getHeight(), null);

		final int x = Math.abs((int) decalView.getProperty(X_OFFSET.name()));
		final int y = Math.abs((int) decalView.getProperty(Y_OFFSET.name()));
		g2d.drawImage(decalView, x, y, null);
		return result;
	}

	@Nonnull
	public Resource<Seq<DungeonWall>> buildUsingWallDefs() {
		final int wallDefId1 = wallDef1();
		final int wallDefId2 = wallDef2();
		final int wallDefId3 = wallDef3();
		Resource<Seq<DungeonWall>> result = appendMoreWalls(wallDefId1, Array.empty(), wallStart1());
		if (id2 != 255) {
			result = result.flatMap(seq -> appendMoreWalls(wallDefId2, seq, wallStart2()));
		}
		if (id3 != 255) {
			result = result.flatMap(seq -> appendMoreWalls(wallDefId3, seq, wallStart3()));
		}
		return result;
	}

	private Resource<Seq<DungeonWall>> appendMoreWalls(int id, Seq<DungeonWall> walls, int wallStart) {
		return loader.find(id, WallDef.class, WALLDEF).flatMap(wallDef -> {
			return buildWallSymbolListFor(id, wallDef.getWallCount() > 5).map(wallSymbols -> {
				return walls.appendAll(buildDungeonWallSetPart(wallDef, wallSymbols, wallStart));
			});
		});
	}

	private Seq<DungeonWall> buildDungeonWallSetPart(WallDef wallDef, Seq<BufferedImage> wallSymbols, int wallStart) {
		if (wallDef.getWallCount() < wallStart + 5) {
			return Array.empty();
		}
		return Array.range(wallStart, wallStart + 5).map(i -> {
			final BufferedImage farFiller = buildWallView(wallSymbols, wallDef.getWallDisplayFarFiller(i));

			final Map<WallDistance, Map<WallPlacement, BufferedImage>> wallViews = Array.of(WallDistance.values())
				.toMap(identity(), dis -> {
					return Array.of(WallPlacement.values()).toMap(identity(), plc -> {
						return buildWallView(wallSymbols, wallDef.getWallDisplay(i, dis, plc));
					});
				});

			return new DungeonWall(wallViews, farFiller);
		});
	}

	private BufferedImage buildWallView(Seq<BufferedImage> wallSymbols, int[][] indexes) {
		final int height = indexes.length << 3;
		final int width = indexes[0].length << 3;

		final BufferedImage result = new BufferedImage(width, height, wallSymbols.get(0).getType(),
			(IndexColorModel) wallSymbols.get(0).getColorModel());
		final WritableRaster r = result.getRaster();

		for (int y = 0; y < indexes.length; y++) {
			final int[] row = indexes[y];
			for (int x = 0; x < row.length; x++) {
				final BufferedImage s = row[x] >= wallSymbols.size() ? wallSymbols.get(0) : wallSymbols.get(row[x]);
				r.setRect(x << 3, y << 3, s.getData());
			}
		}
		return result;
	}

	private Resource<Seq<BufferedImage>> buildWallSymbolListFor(int id, boolean additionalSymbols) {
		final Resource<? extends ImageContent> symbols = loader.find8x8d(id, id);
		if (symbols.isFailure()) {
			return symbols.<Seq<BufferedImage>>map(Seq.class::cast);
		}
		if (symbols.isPresentAndSuccess()) {
			final ImageContent ic = symbols.get();
			if (ic.size() == 255) {
				return Resource.of(Array.of(createTransparentSymbol()).appendAll(ic.toSeq()));
			}
			if (ic.size() > 255) {
				return Resource.of(ic.toSeq());
			}
			return buildWallSymbolListFor(id, ic.toSeq(), additionalSymbols);
		} else {
			return buildWallSymbolListFor(id, Array.empty(), additionalSymbols);
		}
	}

	private Resource<Seq<BufferedImage>> buildWallSymbolListFor(int id, Seq<BufferedImage> symbols,
		boolean additionalSymbols) {

		return loader.findImage(203, _8X8D)
			.map(sharedSymbols -> Array.of(createTransparentSymbol())
				.appendAll(sharedSymbols.toSeq())
				.appendAll(symbols))
			.flatMap(seq -> {
				if (additionalSymbols) {
					return appendAdditionalWallSymbolList(id, seq);
				}
				return Resource.of(seq);
			});
	}

	private Resource<Seq<BufferedImage>> appendAdditionalWallSymbolList(int id, Seq<BufferedImage> symbols) {
		final int addSymbolsIdBase = 10 * (id == 0 ? 10 : id);

		Resource<Seq<BufferedImage>> result = Resource.of(symbols);
		result = appendAdditional(addSymbolsIdBase + 1, id, result);
		result = appendAdditional(addSymbolsIdBase + 2, id, result);
		result = appendAdditional(addSymbolsIdBase + 3, id, result);
		return result;
	}

	private Resource<Seq<BufferedImage>> appendAdditional(int id, int wallDefId, Resource<Seq<BufferedImage>> symbols) {
		if (loader.idsFor(_8X8D).contains(id)) {
			return symbols.flatMap(seq -> {
				return loader.find8x8d(id, wallDefId).map(ic -> seq.appendAll(ic.toSeq()));
			});
		}
		return symbols;
	}

	private BufferedImage createTransparentSymbol() {
		final IndexColorModel cm = Palette.createColorModel(_8X8D);
		final byte[] data = new byte[32];
		for (int i = 0; i < data.length; i++) {
			data[i] = (byte) 0xDD;
		}
		final WritableRaster r = Raster.createPackedRaster(new DataBufferByte(data, 32, 0), 8, 8, 4, null);
		return new BufferedImage(cm, r, false, null);
	}

	private boolean replaceId1() {
		return id1 == 127;
	}

	private boolean replaceId2() {
		return id2 == 127 || !loader.idsFor(WALLDEF).contains(id2);
	}

	private boolean replaceId3() {
		return id3 == 127 || !loader.idsFor(WALLDEF).contains(id3);
	}

	private int wallDef1() {
		return replaceId1() ? 0 : id1;
	}

	private int wallDef2() {
		return replaceId2() ? wallDef1() : id2;
	}

	private int wallDef3() {
		return replaceId3() ? (id2 == 255 ? wallDef1() : wallDef2()) : id3;
	}

	private int wallStart1() {
		return 0;
	}

	private int wallStart2() {
		return replaceId2() ? 5 : 0;
	}

	private int wallStart3() {
		return replaceId3() ? (id2 == 127 ? 10 : 5) : 0;
	}
}
