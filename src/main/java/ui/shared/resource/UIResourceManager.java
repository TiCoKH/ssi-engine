package ui.shared.resource;

import static io.vavr.API.Map;
import static io.vavr.API.Seq;
import static io.vavr.collection.Array.range;
import static shared.FontColor.INTENSE;
import static ui.shared.FrameType.FRAME;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import io.vavr.API;
import io.vavr.Tuple2;
import io.vavr.collection.Array;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.control.Try;

import data.dungeon.WallDef.WallDistance;
import data.dungeon.WallDef.WallPlacement;
import data.image.ImageContent;
import data.palette.Palette;
import shared.FontColor;
import shared.GameFeature;
import ui.ExceptionHandler;
import ui.UISettings;
import ui.shared.dungeon.DungeonMapBuilder;
import ui.shared.dungeon.DungeonWall;
import ui.shared.dungeon.DungeonWallSetBuilder;

public class UIResourceManager {
	private static final ImageResource INTERNAL_ID_MISC = new ImageResource(1000, null);
	private static final ImageResource INTERNAL_ID_FRAMES = new ImageResource(2000, null);
	private static final ImageResource INTERNAL_ID_OVERLAND_CURSOR = new ImageResource(3000, null);

	private final UIResourceConfiguration config;
	private final UIResourceLoader loader;
	private final ExceptionHandler excHandler;
	private final UIScaler scaler;

	private final ImageContent originalFont;
	private final ImageContent originalMisc;
	private final ImageContent originalFrames;
	private final AtomicReference<Map<DungeonResource, Seq<DungeonWall>>> originalWalls = new AtomicReference<>(Map());

	private final AtomicReference<Map<FontColor, Seq<BufferedImage>>> fonts = new AtomicReference<>(Map());
	private final AtomicReference<Map<ImageResource, Seq<BufferedImage>>> images = new AtomicReference<>(Map());
	private final AtomicReference<Map<DungeonResource, Seq<DungeonWall>>> walls = new AtomicReference<>(Map());
	private final AtomicReference<Map<DungeonMapResource, BufferedImage>> maps = new AtomicReference<>(Map());

	public UIResourceManager(@Nonnull UIResourceConfiguration config, @Nonnull UIResourceLoader loader,
		@Nonnull UISettings settings, @Nonnull ExceptionHandler excHandler) {

		settings.addPropertyChangeListener(e -> {
			fonts.set(Map());
			images.set(Map());
			walls.set(Map());
			maps.set(Map());
		});

		this.config = config;
		this.loader = loader;
		this.excHandler = excHandler;
		this.scaler = new UIScaler(settings);

		this.originalFont = loader.getFont()
			.orElseThrow()
			.onFailure(t -> excHandler.handleException("Loading font", t))
			.get();
		this.originalMisc = loader.getMisc()
			.orElseThrow()
			.onFailure(t -> excHandler.handleException("Loading misc", t))
			.get();
		this.originalFrames = loader.getFrames()
			.orElseThrow()
			.onFailure(t -> excHandler.handleException("Loading frames", t))
			.get();
	}

	@Nonnull
	public Seq<BufferedImage> getFont(@Nonnull FontColor type) {
		final Map<FontColor, Seq<BufferedImage>> cache = fonts.get();
		if (cache.containsKey(type)) {
			return cache.get(type).getOrElseThrow(IllegalStateException::new);
		}
		final Seq<BufferedImage> font = createFont(type);
		fonts.updateAndGet(f -> f.containsKey(type) ? f : f.put(type, font));
		return font;
	}

	private Seq<BufferedImage> createFont(FontColor type) {
		final IndexColorModel cm = (IndexColorModel) originalFont.get(0).getColorModel();

		final Color textColor = FRAME.equals(config.getFrameType()) ? //
			type.getFrameFontColor() : type.getFontColor();
		final IndexColorModel newCM = type == INTENSE ? //
			Palette.toInvertedPalette(cm) : Palette.toPaletteWithFG(cm, textColor);

		Seq<BufferedImage> scaledFont = originalFont
			.map(c -> scaler.scale(new BufferedImage(newCM, c.getRaster(), false, null)));
		if (config.isUsingFeature(GameFeature.SPECIAL_CHARS_NOT_FROM_FONT)) {
			scaledFont = scaledFont.append(createScaledGlyphFrom(config.getFontUmlautAe(), newCM))
				.append(createScaledGlyphFrom(config.getFontUmlautOe(), newCM))
				.append(createScaledGlyphFrom(config.getFontUmlautUe(), newCM))
				.append(createScaledGlyphFrom(config.getFontSharpSz(), newCM));
		}
		return scaledFont;
	}

	private BufferedImage createScaledGlyphFrom(String umlaut, IndexColorModel cm) {
		final byte[] data = new byte[8];
		for (int i = 0; i < data.length; i++) {
			data[i] = (byte) Integer.parseInt(umlaut.substring(2 * i, 2 * i + 2), 16);
		}
		final DataBufferByte db = new DataBufferByte(data, 8);
		final WritableRaster r = Raster.createPackedRaster(db, 8, 8, 1, null);
		final BufferedImage unscaledGlyph = new BufferedImage(cm, r, false, null);
		return scaler.scale(unscaledGlyph);
	}

	@Nonnull
	public Seq<BufferedImage> getMisc() {
		return getImageResource(INTERNAL_ID_MISC, originalMisc);
	}

	@Nonnull
	public Seq<BufferedImage> getFrames() {
		return getImageResource(INTERNAL_ID_FRAMES, originalFrames);
	}

	private Seq<BufferedImage> getImageResource(ImageResource r, ImageContent content) {
		return getImageResource(r, () -> scale(content));
	}

	@Nonnull
	public BufferedImage getOverlandCursor() {
		return getImageResource(INTERNAL_ID_OVERLAND_CURSOR, this::createOverlandCursor).get(0);
	}

	private Seq<BufferedImage> createOverlandCursor() {
		return loader.getOverlandCursor().map(t -> t.onFailure(throwable -> {
			excHandler.handleException("Error reading the overland cursor", throwable);
		}).map(this::scale).getOrElse(this::createDefaultOverlandCursor)).orElseGet(this::createDefaultOverlandCursor);
	}

	private Seq<BufferedImage> createDefaultOverlandCursor() {
		final int[] cursorData = new int[8 * 8];
		Arrays.fill(cursorData, 0xFFFFFF);
		final BufferedImage cursorImage = new BufferedImage(8, 8, BufferedImage.TYPE_BYTE_BINARY);
		cursorImage.setRGB(0, 0, 8, 8, cursorData, 0, 8);
		return Seq(scaler.scale(cursorImage));
	}

	@Nonnull
	public Seq<BufferedImage> getImageResource(@Nonnull ImageResource r) {
		return getImageResource(r, () -> createImageResource(r));
	}

	private Seq<BufferedImage> createImageResource(ImageResource r) {
		if (r instanceof ImageCompositeResource) {
			ImageCompositeResource cr = (ImageCompositeResource) r;

				final Seq<Tuple2<Point, Seq<BufferedImage>>> images = range(0, cr.getLength()).map(i -> {
				final Point offset = cr.getOffset(i);
				final Seq<BufferedImage> img = loadImageResource(cr.get(i)) //
					.map(t -> toSeq(r, t))
					.orElseGet(this::getBrokenList);
				return new Tuple2<>(offset, img);
			});
			return Array.of(scaler.scaleComposite(cr.getType(), images.flatMap(Tuple2::_2), images.map(Tuple2::_1)));
		}
		return loadImageResource(r).map(t -> toSeq(r, t).map(scaler::scale)).orElseGet(this::getBrokenList);
	}

	private Seq<BufferedImage> toSeq(ImageResource r, Try<ImageContent> t) {
		return t.onFailure(throwable -> excHandler.handleException("could not load " + r, throwable)) //
			.map(ImageContent::toSeq)
			.getOrElse(this::getBrokenList);
	}

	private Optional<Try<ImageContent>> loadImageResource(ImageResource r) {
		final Optional<String> fn = r.getFilename();
		if (fn.isPresent()) {
			return loader.load(fn.get(), r.getId(), r.getType());
		}
		return loader.findImage(r.getId(), r.getType());
	}

	@Nonnull
	private Seq<BufferedImage> getImageResource(@Nonnull ImageResource r,
		@Nonnull Supplier<Seq<BufferedImage>> resourceSupplier) {

		final Map<ImageResource, Seq<BufferedImage>> cache = images.get();
		if (cache.containsKey(r)) {
			return cache.get(r).getOrElseThrow(IllegalStateException::new);
		}
		final Seq<BufferedImage> res = resourceSupplier.get();
		images.updateAndGet(i -> i.containsKey(r) ? i : i.put(r, res));
		return res;
	}

	@Nonnull
	public BufferedImage getMapResource(@Nonnull DungeonMapResource r) {
		final Map<DungeonMapResource, BufferedImage> cache = maps.get();
		if (cache.containsKey(r)) {
			return cache.get(r).getOrElseThrow(IllegalStateException::new);
		}
		final BufferedImage map = createMap(r);
		maps.updateAndGet(m -> m.containsKey(r) ? m : m.put(r, map));
		return map;
	}

	private BufferedImage createMap(DungeonMapResource r) {
		final DungeonMapBuilder builder = new DungeonMapBuilder(config, loader);
		builder.withMap(r.getMap());
		r.getRes()
			.ifPresentOrElse(res -> builder.withWMapDecoIds(res.getIds()[0], res.getIds()[1], res.getIds()[2]),
				builder::withoutMapDecoIds);
		return builder.build()
			.map(t -> t.onFailure(throwable -> excHandler.handleException("could not create map image", throwable))
				.map(scaler::scale)
				.getOrElse(this::getBroken))
			.orElseGet(this::getBroken);
	}

	@Nonnull
	public Seq<DungeonWall> getWallResource(@Nonnull DungeonResource r) {
		final Map<DungeonResource, Seq<DungeonWall>> cache = walls.get();
		if (cache.containsKey(r)) {
			return cache.get(r).getOrElseThrow(IllegalStateException::new);
		}
		final Seq<DungeonWall> dungeonwalls = createWalls(r);
		walls.updateAndGet(w -> w.containsKey(r) ? w : w.put(r, dungeonwalls));
		return dungeonwalls;
	}

	private Seq<DungeonWall> createWalls(DungeonResource r) {
		return getOriginalWallResource(r).map(this::scale);
	}

	private Seq<DungeonWall> getOriginalWallResource(DungeonResource r) {
		final Map<DungeonResource, Seq<DungeonWall>> cache = originalWalls.get();
		if (cache.containsKey(r)) {
			return cache.get(r).getOrElseThrow(IllegalStateException::new);
		}
		final Seq<DungeonWall> dungeonwalls = createOriginalWalls(r);
		originalWalls.updateAndGet(w -> w.containsKey(r) ? w : w.put(r, dungeonwalls));
		return dungeonwalls;
	}

	private Seq<DungeonWall> createOriginalWalls(DungeonResource r) {
		final DungeonWallSetBuilder builder = new DungeonWallSetBuilder(loader);
		return builder.withWallDecoIds(r.getIds()[0], r.getIds()[1], r.getIds()[2])
			.build()
			.map(t -> t.onFailure(
				throwable -> excHandler.handleException("could not create original walls for " + r.getIds(), throwable))
				.getOrElse(API::Seq))
			.orElseGet(API::Seq);
	}

	private DungeonWall scale(DungeonWall originalWall) {
		final Map<WallDistance, Map<WallPlacement, BufferedImage>> wallViewsMap = Seq(WallDistance.values())
			.toMap(Function.identity(), dis -> {
				return Seq(WallPlacement.values()).toMap(Function.identity(), plc -> {
					return scaler.scale(originalWall.getWallViewFor(dis, plc));
				});
			});
		final BufferedImage farFiller = scaler.scale(originalWall.getFarFiller());
		return new DungeonWall(wallViewsMap, farFiller);
	}

	private Seq<BufferedImage> scale(ImageContent content) {
		return content.map(scaler::scale);
	}

	private static final BufferedImage BROKEN = new BufferedImage(8, 8, BufferedImage.TYPE_BYTE_BINARY);
	private static final Seq<BufferedImage> BROKEN_List = Seq(BROKEN);

	private BufferedImage getBroken() {
		return BROKEN;
	}

	private Seq<BufferedImage> getBrokenList() {
		return BROKEN_List;
	}
}
