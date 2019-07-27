package ui;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
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

import data.content.DAXImageContent;
import data.content.DAXPalette;
import data.content.WallDef;
import data.content.WallDef.WallDistance;
import data.content.WallDef.WallPlacement;

public class UIResourceManager {
	private static final ImageResource INTERNAL_ID_MISC = new ImageResource(1000, null);
	private static final ImageResource INTERNAL_ID_FRAMES = new ImageResource(2000, null);
	private static final ImageResource INTERNAL_ID_OVERLAND_CURSOR = new ImageResource(3000, null);

	private static final BufferedImage BROKEN = new BufferedImage(8, 8, BufferedImage.TYPE_BYTE_BINARY);

	private UIResourceConfiguration config;
	private UIResourceLoader loader;
	private ExceptionHandler excHandler;
	private UIScaler scaler;

	private DAXImageContent originalFont;
	private DAXImageContent originalMisc;
	private DAXImageContent originalFrames;
	private Map<DungeonResource, List<DungeonWall>> originalWalls = new HashMap<>();

	private Map<FontType, List<BufferedImage>> fonts = new EnumMap<>(FontType.class);
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
	public List<BufferedImage> getFont(@Nonnull FontType type) {
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
		return imageResources.computeIfAbsent(r, this::createResource);
	}

	@Nonnull
	private List<BufferedImage> getOrCreateResource(@Nonnull ImageResource r, @Nonnull DAXImageContent content) {
		if (!imageResources.containsKey(r)) {
			imageResources.put(r, scale(content));
		}
		return imageResources.get(r);
	}

	@Nonnull
	private List<DungeonWall> getOrCreateResource(@Nonnull DungeonResource r) {
		return walls.computeIfAbsent(r, this::createWalls);
	}

	@Nonnull
	private BufferedImage getOrCreateResource(@Nonnull DungeonMapResource r) {
		return maps.computeIfAbsent(r, this::createMap);
	}

	@Nonnull
	private List<BufferedImage> createFont(@Nonnull FontType type) {
		IndexColorModel cm = (IndexColorModel) originalFont.get(0).getColorModel();

		IndexColorModel newCM;
		switch (type) {
			case NORMAL:
			case PC_HEADING:
			case SEL_PC:
			case PC:
				newCM = DAXPalette.toPaletteWithGreenFG(cm);
				break;
			case GAME_NAME:
				newCM = DAXPalette.toPaletteWithMagentaFG(cm);
				break;
			case INTENSE:
				newCM = DAXPalette.toInvertedPalette(cm);
				break;
			default:
				newCM = cm;
				break;
		}
		return originalFont.stream() //
			.map(c -> scaler.scale(new BufferedImage(newCM, c.getRaster(), false, null))) //
			.collect(Collectors.toList());
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

		List<BufferedImage> brokenResult = new ArrayList<>();
		brokenResult.add(BROKEN);
		return brokenResult;
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
