package ui;

import static data.content.DAXContentType.WALLDEF;

import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import data.content.DAXContentType;
import data.content.DAXImageContent;
import data.content.DAXPalette;
import data.content.MonocromeSymbols;
import data.content.WallDef;

public class UIResourceManager {
	private static final ImageResource INTERNAL_ID_MISC = new ImageResource(1000, null);
	private static final ImageResource INTERNAL_ID_BORDERS = new ImageResource(2000, null);
	private static final ImageResource INTERNAL_ID_OVERLAND_CURSORD = new ImageResource(3000, null);
	private static final ImageResource INTERNAL_ID_SPACE_BACKGROUND = new ImageResource(4000, null);
	private static final ImageResource INTERNAL_ID_SPACE_SYMBOLS = new ImageResource(5000, null);

	private static final BufferedImage BROKEN = new BufferedImage(8, 8, BufferedImage.TYPE_BYTE_BINARY);

	private UIResourceLoader loader;
	private ExceptionHandler excHandler;
	private UIScaler scaler;

	private MonocromeSymbols originalFont;
	private DAXImageContent originalMisc;
	private DAXImageContent originalBorders;

	private Map<FontType, List<BufferedImage>> fonts = new EnumMap<>(FontType.class);
	private Map<ImageResource, List<BufferedImage>> imageResources = new HashMap<>();
	private Map<Integer, WallDef> walldefs = new HashMap<>();

	public UIResourceManager(@Nonnull UIResourceLoader loader, @Nonnull UISettings settings, @Nonnull ExceptionHandler excHandler)
		throws IOException {

		this.loader = loader;
		this.excHandler = excHandler;
		this.scaler = new UIScaler(settings);

		this.originalFont = loader.getFont();
		this.originalMisc = loader.getMisc();
		this.originalBorders = loader.getBorders();
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
	public List<BufferedImage> getBorders() {
		return getOrCreateResource(INTERNAL_ID_BORDERS, originalBorders);
	}

	@Nonnull
	public BufferedImage getOverlandCursor() {
		try {
			return getOrCreateResource(INTERNAL_ID_OVERLAND_CURSORD, loader.getOverlandCursor()).get(0);
		} catch (IOException e) {
			excHandler.handleException("Error reading the overland cursor", e);
		}
		return BROKEN;
	}

	@Nonnull
	public List<BufferedImage> getSpaceSymbols() {
		try {
			return getOrCreateResource(INTERNAL_ID_SPACE_SYMBOLS, loader.getSpaceSymbols());
		} catch (IOException e) {
			excHandler.handleException("Error reading the space symbols", e);
		}

		List<BufferedImage> brokenResult = new ArrayList<>();
		for (int i = 0; i < 95; i++) {
			brokenResult.add(BROKEN);
		}
		return brokenResult;
	}

	@Nonnull
	public BufferedImage getSpaceBackground() {
		try {
			return getOrCreateResource(INTERNAL_ID_SPACE_BACKGROUND, loader.getSpaceBackground()).get(0);
		} catch (IOException e) {
			excHandler.handleException("Error reading the Space background", e);
		}
		return BROKEN;
	}

	@Nonnull
	public List<BufferedImage> getImageResource(int id, @Nullable DAXContentType type) {
		return getOrCreateResource(new ImageResource(id, type));
	}

	@Nullable
	public WallDef getWalldef(int id) {
		return walldefs.computeIfAbsent(id, x -> {
			try {
				return loader.find(id, WallDef.class, WALLDEF);
			} catch (IOException e) {
				excHandler.handleException("Error reading WallDef " + id, e);
			}
			return null;
		});
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
	private List<BufferedImage> createFont(@Nonnull FontType type) {
		IndexColorModel newCM;
		switch (type) {
			case NORMAL:
			case PC_HEADING:
			case SEL_PC:
			case PC:
				newCM = DAXPalette.binaryPaletteWithGreenFG();
				break;
			case GAME_NAME:
				newCM = DAXPalette.binaryPaletteWithMagentaFG();
				break;
			case INTENSE:
				newCM = DAXPalette.binaryInvertedPalette();
				break;
			default:
				newCM = (IndexColorModel) originalFont.get(0).getColorModel();
				break;
		}
		return originalFont.stream() //
			.map(c -> scaler.scale(new BufferedImage(newCM, c.getRaster(), false, null))) //
			.collect(Collectors.toList());
	}

	@Nonnull
	private List<BufferedImage> createResource(@Nonnull ImageResource r) {
		try {
			DAXImageContent content = loader.findImage(r.getId(), r.getType());
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

	@Nonnull
	private List<BufferedImage> scale(@Nonnull DAXImageContent content) {
		return content.stream().map(scaler::scale).collect(Collectors.toList());
	}
}
