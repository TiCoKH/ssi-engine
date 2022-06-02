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
import java.util.Optional;

import javax.annotation.Nonnull;

import io.vavr.collection.Array;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.control.Try;

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
	public Optional<Try<Seq<DungeonWall>>> build() {
		if (loader.idsFor(WALLDEF).isEmpty()) {
			return buildWithoutWallDefs();
		}
		return buildUsingWallDefs();
	}

	@Nonnull
	public Optional<Try<Seq<DungeonWall>>> buildWithoutWallDefs() {
		Optional<Try<Seq<DungeonWall>>> result = Optional.of(Try.of(Array::empty));
		if (id1 != 127 && id1 != 255)
			result = result.flatMap(t -> buildWithoutWallDefs(id1).map(t2 -> append(t, t2)));
		if (id2 != 127 && id2 != 255)
			result = result.flatMap(t -> buildWithoutWallDefs(id2).map(t2 -> append(t, t2)));
		if (id3 != 127 && id3 != 255)
			result = result.flatMap(t -> buildWithoutWallDefs(id3).map(t2 -> append(t, t2)));

		return result;
	}

	private Try<Seq<DungeonWall>> append(Try<Seq<DungeonWall>> t1, Try<Seq<DungeonWall>> t2) {
		return t2.flatMap(seq2 -> {
			return t1.map(seq1 -> seq1.appendAll(seq2));
		});
	}

	@Nonnull
	public Optional<Try<Seq<DungeonWall>>> buildWithoutWallDefs(int id) {
		return loader.findImage(id, _8X8D).map(t -> t.map(ic -> {
			final Seq<BufferedImage> wallParts = ic.toSeq();

			final Seq<BufferedImage> wall1 = wallParts.subSequence(0, 10);
			final Seq<BufferedImage> wall2 = wallParts.subSequence(10, 20);

			final Seq<BufferedImage> decal1 = wallParts.subSequence(20, 29);
			final Seq<BufferedImage> decal2 = wallParts.subSequence(29, 38);
			final Seq<BufferedImage> decal3 = wallParts.subSequence(38, 47);

			return Array.of(buildWall(wall1, null), buildWall(wall2, null), buildWall(wall1, decal1),
				buildWall(wall1, decal2), buildWall(wall1, decal3));
		}));
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
	public Optional<Try<Seq<DungeonWall>>> buildUsingWallDefs() {
		final int wallDefId1 = replaceId1() ? 0 : id1;
		final int wallDefId2 = replaceId2() ? wallDefId1 : id2;
		final int wallDefId3 = replaceId3() ? (id2 == 255 ? wallDefId1 : wallDefId2) : id3;
		Optional<Try<Seq<DungeonWall>>> result = appendMoreWalls(wallDefId1, Array.empty(), 0);
		if (id2 != 255) {
			result = result.flatMap(t -> {
				if (t.isFailure()) {
					return Optional.of(t);
				}
				return appendMoreWalls(wallDefId2, t.get(), replaceId2() ? 5 : 0);
			});
		}
		if (id3 != 255) {
			result = result.flatMap(t -> {
				if (t.isFailure()) {
					return Optional.of(t);
				}
				return appendMoreWalls(wallDefId3, t.get(), replaceId3() ? (id2 == 127 ? 10 : 5) : 0);
			});
		}
		return result;
	}

	private Optional<Try<Seq<DungeonWall>>> appendMoreWalls(int id, Seq<DungeonWall> walls, int wallStart) {
		return loader.find(id, WallDef.class, WALLDEF).flatMap(t -> {
			if (t.isFailure()) {
				return Optional.of(Try.failure(t.getCause()));
			}
			final WallDef wallDef = t.get();
			return buildWallSymbolListFor(id, wallDef.getWallCount() > 5).map(t2 -> t2.map(wallSymbols -> {
				return walls.appendAll(buildDungeonWallSetPart(wallDef, wallSymbols, wallStart));
			}));
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

	private Optional<Try<Seq<BufferedImage>>> buildWallSymbolListFor(int id, boolean additionalSymbols) {
		final Optional<Try<ImageContent>> symbols = loader.find8x8d(id, id);
		if (symbols.isPresent()) {
			final Try<ImageContent> t = symbols.get();
			if (t.isFailure()) {
				return Optional.of(Try.failure(t.getCause()));
			}
			final ImageContent ic = t.get();
			if (ic.size() == 255) {
				return Optional.of(Try.success(Array.of(createTransparentSymbol()).appendAll(ic.toSeq())));
			}
			if (ic.size() > 255) {
				return Optional.of(Try.success(ic.toSeq()));
			}
			return buildWallSymbolListFor(id, ic.toSeq(), additionalSymbols);
		} else {
			return buildWallSymbolListFor(id, Array.empty(), additionalSymbols);
		}
	}

	private Optional<Try<Seq<BufferedImage>>> buildWallSymbolListFor(int id, Seq<BufferedImage> symbols,
		boolean additionalSymbols) {

		return loader.findImage(203, _8X8D).map(t -> {
			return t.<Seq<BufferedImage>>map(sharedSymbols -> Array.of(createTransparentSymbol())
				.appendAll(sharedSymbols.toSeq())
				.appendAll(symbols));
		}).flatMap(t -> {
			if (additionalSymbols && t.isSuccess()) {
				return appendAdditionalWallSymbolList(id, t.get());
			}
			return Optional.of(t);
		});
	}

	private Optional<Try<Seq<BufferedImage>>> appendAdditionalWallSymbolList(int id, Seq<BufferedImage> symbols) {

		final int addSymbolsIdBase = 10 * (id == 0 ? 10 : id);

		Optional<Try<Seq<BufferedImage>>> result = Optional.of(Try.success(symbols));
		result = appendAdditional(addSymbolsIdBase + 1, id, result);
		result = appendAdditional(addSymbolsIdBase + 2, id, result);
		result = appendAdditional(addSymbolsIdBase + 3, id, result);
		return result;
	}

	private Optional<Try<Seq<BufferedImage>>> appendAdditional(int id, int wallDefId,
		Optional<Try<Seq<BufferedImage>>> symbols) {

		if (loader.idsFor(_8X8D).contains(id)) {
			return symbols.flatMap(t -> {
				if (t.isFailure()) {
					return symbols;
				}
				return loader.find8x8d(id, wallDefId).map(t2 -> t2.map(ic -> {
					return t.get().appendAll(ic.toSeq());
				}));
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
}
