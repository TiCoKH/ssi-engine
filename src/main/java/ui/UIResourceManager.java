package ui;

import static shared.FontColor.INTENSE;
import static ui.FrameType.FRAME;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import data.content.DAXImageContent;
import data.content.DAXPalette;
import data.content.WallDef;
import data.content.WallDef.WallDistance;
import data.content.WallDef.WallPlacement;
import shared.FontColor;
import shared.GameFeature;

public class UIResourceManager {
	private static final ImageResource INTERNAL_ID_MISC = new ImageResource(1000, null);
	private static final ImageResource INTERNAL_ID_FRAMES = new ImageResource(2000, null);
	private static final ImageResource INTERNAL_ID_OVERLAND_CURSOR = new ImageResource(3000, null);

	private static final BufferedImage BROKEN = new BufferedImage(8, 8, BufferedImage.TYPE_BYTE_BINARY);
	private static final List<BufferedImage> BROKEN_List = ImmutableList.of(BROKEN);

	private UIResourceConfiguration config;
	private UIResourceLoader loader;
	private ExceptionHandler excHandler;
	private UIScaler scaler;

	private DAXImageContent originalFont;
	private DAXImageContent originalMisc;
	private DAXImageContent originalFrames;
	private Map<DungeonResource, List<DungeonWall>> originalWalls = new HashMap<>();

	private Map<FontColor, List<BufferedImage>> fonts = new EnumMap<>(FontColor.class);
	private Map<ImageResource, List<BufferedImage>> imageResources = new HashMap<>();
	private Map<DungeonResource, List<DungeonWall>> walls = new HashMap<>();
	private Map<DungeonMapResource, BufferedImage> maps = new HashMap<>();

	public UIResourceManager(@Nonnull UIResourceConfiguration config, @Nonnull UIResourceLoader loader, @Nonnull UISettings settings,
		@Nonnull ExceptionHandler excHandler) throws IOException {

		settings.addPropertyChangeListener(e -> {
			fonts.clear();
			imageResources.clear();
			walls.clear();
			maps.clear();
		});

		this.config = config;
		this.loader = loader;
		this.excHandler = excHandler;
		this.scaler = new UIScaler(settings);

		this.originalFont = loader.getFont();
		this.originalMisc = loader.getMisc();
		this.originalFrames = loader.getFrames();
	}

	@Nonnull
	public List<BufferedImage> getFont(@Nonnull FontColor type) {
		return fonts.computeIfAbsent(type, this::createFont);
	}

	@Nonnull
	public List<BufferedImage> getMisc() {
		return getOrCreateResource(INTERNAL_ID_MISC, originalMisc);
	}

	@Nonnull
	public List<BufferedImage> getFrames() {
		return getOrCreateResource(INTERNAL_ID_FRAMES, originalFrames);
	}

	@Nonnull
	public BufferedImage getOverlandCursor() {
		if (!imageResources.containsKey(INTERNAL_ID_OVERLAND_CURSOR)) {
			List<BufferedImage> cursor = null;
			try {
				DAXImageContent content = loader.getOverlandCursor();
				if (content != null) {
					cursor = scale(content);
				}
			} catch (IOException e) {
				excHandler.handleException("Error reading the overland cursor", e);
			}
			if (cursor == null) {
				BufferedImage cursorImage = new BufferedImage(8, 8, BufferedImage.TYPE_BYTE_BINARY);
				cursorImage.setRGB(0, 0, 8, 8, new int[] { //
					0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, //
					0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, //
					0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, //
					0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, //
					0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, //
					0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, //
					0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, //
					0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, //
				}, 0, 8);
				cursorImage = scaler.scale(cursorImage);
				cursor = new ArrayList<>();
				cursor.add(cursorImage);
			}
			imageResources.put(INTERNAL_ID_OVERLAND_CURSOR, cursor);
		}
		return imageResources.get(INTERNAL_ID_OVERLAND_CURSOR).get(0);
	}

	@Nonnull
	public BufferedImage getMapResource(@Nonnull DungeonMapResource r) {
		return getOrCreateResource(r);
	}

	@Nonnull
	public List<BufferedImage> getImageResource(@Nonnull ImageResource r) {
		return getOrCreateResource(r);
	}

	@Nullable
	public List<DungeonWall> getWallResource(@Nonnull DungeonResource r) {
		return getOrCreateResource(r);
	}

	@Nonnull
	private List<BufferedImage> getOrCreateResource(@Nonnull ImageResource r) {
		if (!imageResources.containsKey(r)) {
			synchronized (imageResources) {
				if (!imageResources.containsKey(r)) {
					imageResources.put(r, createResource(r));
				}
			}
		}
		return imageResources.get(r);
	}

	@Nonnull
	private List<BufferedImage> getOrCreateResource(@Nonnull ImageResource r, @Nonnull DAXImageContent content) {
		if (!imageResources.containsKey(r)) {
			synchronized (imageResources) {
				if (!imageResources.containsKey(r)) {
					imageResources.put(r, scale(content));
				}
			}
		}
		return imageResources.get(r);
	}

	@Nonnull
	private List<DungeonWall> getOrCreateResource(@Nonnull DungeonResource r) {
		if (!walls.containsKey(r)) {
			synchronized (walls) {
				if (!walls.containsKey(r)) {
					walls.put(r, createWalls(r));
				}
			}
		}
		return walls.get(r);
	}

	@Nonnull
	private BufferedImage getOrCreateResource(@Nonnull DungeonMapResource r) {
		if (!maps.containsKey(r)) {
			synchronized (maps) {
				if (!maps.containsKey(r)) {
					maps.put(r, createMap(r));
				}
			}
		}
		return maps.get(r);
	}

	@Nonnull
	private List<BufferedImage> createFont(@Nonnull FontColor type) {
		IndexColorModel cm = (IndexColorModel) originalFont.get(0).getColorModel();

		Color textColor = FRAME.equals(config.getFrameType()) ? //
			type.getFrameFontColor() : type.getFontColor();
		IndexColorModel newCM = type == INTENSE ? //
			DAXPalette.toInvertedPalette(cm) : DAXPalette.toPaletteWithFG(cm, textColor);

		List<BufferedImage> scaledFont = originalFont.stream() //
			.map(c -> scaler.scale(new BufferedImage(newCM, c.getRaster(), false, null))) //
			.collect(Collectors.toList());
		if (config.isUsingFeature(GameFeature.SPECIAL_CHARS_NOT_FROM_FONT)) {
			scaledFont.add(createScaledGlyphFrom(config.getFontUmlautAe(), newCM));
			scaledFont.add(createScaledGlyphFrom(config.getFontUmlautOe(), newCM));
			scaledFont.add(createScaledGlyphFrom(config.getFontUmlautUe(), newCM));
			scaledFont.add(createScaledGlyphFrom(config.getFontSharpSz(), newCM));
		}
		return scaledFont;
	}

	private BufferedImage createScaledGlyphFrom(String umlaut, IndexColorModel cm) {
		byte[] data = new byte[8];
		for (int i = 0; i < data.length; i++) {
			data[i] = (byte) Integer.parseInt(umlaut.substring(2 * i, 2 * i + 2), 16);
		}
		DataBufferByte db = new DataBufferByte(data, 8);
		WritableRaster r = Raster.createPackedRaster(db, 8, 8, 1, null);
		BufferedImage unscaledGlyph = new BufferedImage(cm, r, false, null);
		return scaler.scale(unscaledGlyph);
	}

	@Nonnull
	private List<BufferedImage> createResource(@Nonnull ImageResource r) {
		try {
			if (r instanceof ImageCompositeResource) {
				ImageCompositeResource cr = (ImageCompositeResource) r;

				List<BufferedImage> allContent = new ArrayList<>();
				List<Point> allOffsets = new ArrayList<>();
				for (int i = 0; i < cr.getLength(); i++) {
					DAXImageContent content = loadResource(cr.get(i));
					if (content != null) {
						allContent.addAll(content.toList());
						allOffsets.add(cr.getOffset(i));
					}
				}
				List<BufferedImage> result = new ArrayList<BufferedImage>();
				result.add(scaler.scaleComposite(cr.getType(), allContent, allOffsets));
				return result;
			}

			DAXImageContent content = loadResource(r);
			if (content != null) {
				return scale(content);
			}
		} catch (IOException e) {
			excHandler.handleException("Error reading " + r, e);
		}

		return BROKEN_List;
	}

	private DAXImageContent loadResource(@Nonnull ImageResource r) throws IOException {
		Optional<String> fn = r.getFilename();
		if (fn.isPresent()) {
			return loader.load(fn.get(), r.getId(), r.getType());
		}
		return loader.findImage(r.getId(), r.getType());
	}

	@Nonnull
	private List<DungeonWall> createWalls(@Nonnull DungeonResource r) {
		List<DungeonWall> originalWallRes = originalWalls.computeIfAbsent(r, res -> {
			try {
				DungeonWallSetBuilder builder = new DungeonWallSetBuilder(loader);
				return builder.withWallDecoIds(res.getIds()[0], res.getIds()[1], res.getIds()[2]).build();
			} catch (IOException e) {
				excHandler.handleException("Error reading WallDefs " + res.getIds(), e);
			}
			return new ArrayList<>();
		});
		return originalWallRes.stream().map(this::scale).collect(Collectors.toList());
	}

	private BufferedImage createMap(@Nonnull DungeonMapResource r) {
		DungeonMapBuilder builder = new DungeonMapBuilder(config, loader);
		builder.withMap(r.getMap());

		Optional<DungeonResource> res = r.getRes();
		if (res.isPresent()) {
			DungeonResource dres = res.get();
			builder.withWMapDecoIds(dres.getIds()[0], dres.getIds()[1], dres.getIds()[2]);
		} else {
			builder.withoutMapDecoIds();
		}

		try {
			return scaler.scale(builder.build());
		} catch (IOException e) {
			excHandler.handleException("Error creating map image", e);
			return BROKEN;
		}
	}

	@Nonnull
	private DungeonWall scale(@Nonnull DungeonWall originalWall) {
		Map<WallDistance, Map<WallPlacement, BufferedImage>> wallViewsMap = new HashMap<WallDef.WallDistance, Map<WallPlacement, BufferedImage>>();
		wallViewsMap.put(WallDistance.CLOSE, new HashMap<WallDef.WallPlacement, BufferedImage>());
		wallViewsMap.put(WallDistance.MEDIUM, new HashMap<WallDef.WallPlacement, BufferedImage>());
		wallViewsMap.put(WallDistance.FAR, new HashMap<WallDef.WallPlacement, BufferedImage>());

		for (WallDistance dis : WallDistance.values()) {
			for (WallPlacement plc : WallPlacement.values()) {
				wallViewsMap.get(dis).put(plc, scaler.scale(originalWall.getWallViewFor(dis, plc)));
			}
		}
		BufferedImage farFiller = scaler.scale(originalWall.getFarFiller());
		return new DungeonWall(wallViewsMap, farFiller);
	}

	@Nonnull
	private List<BufferedImage> scale(@Nonnull DAXImageContent content) {
		return content.stream().map(scaler::scale).collect(Collectors.toList());
	}
}
