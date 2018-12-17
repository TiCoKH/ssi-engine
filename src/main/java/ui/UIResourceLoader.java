package ui;

import static data.content.DAXContentType.BACK;
import static data.content.DAXContentType.BIGPIC;
import static data.content.DAXContentType.PIC;
import static data.content.DAXContentType.SPRIT;
import static data.content.DAXContentType.TITLE;
import static data.content.DAXContentType._8X8D;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import common.FileMap;
import data.ResourceLoader;
import data.content.DAXContentType;
import data.content.DAXImageContent;
import data.content.MonocromeSymbols;
import data.content.VGADependentImages;
import data.content.VGAImage;

public class UIResourceLoader extends ResourceLoader {
	private static final Map<DAXContentType, Class<? extends DAXImageContent>> imageTypes = new EnumMap<>(DAXContentType.class);
	static {
		imageTypes.put(_8X8D, VGAImage.class);
		imageTypes.put(BACK, VGAImage.class);
		imageTypes.put(BIGPIC, VGAImage.class);
		imageTypes.put(PIC, VGADependentImages.class);
		imageTypes.put(SPRIT, VGADependentImages.class);
		imageTypes.put(TITLE, VGAImage.class);
	}

	public UIResourceLoader(@Nonnull FileMap fileMap) {
		super(fileMap);
	}

	@Nonnull
	public MonocromeSymbols getFont() throws IOException {
		return load("8X8D1.DAX", 201, MonocromeSymbols.class, _8X8D);
	}

	@Nonnull
	public DAXImageContent getMisc() throws IOException {
		return load("8X8D1.DAX", 202, VGAImage.class, _8X8D);
	}

	@Nonnull
	public DAXImageContent getBorders() throws IOException {
		return load("BORDERS.DAX", 0, VGAImage.class, _8X8D);
	}

	@Nonnull
	public DAXImageContent getOverlandCursor() throws IOException {
		return load("CURSOR.DAX", 1, VGAImage.class, _8X8D);
	}

	@Nonnull
	public DAXImageContent getSpaceSymbols() throws IOException {
		return load("8X8D0.DAX", 1, VGAImage.class, _8X8D);
	}

	@Nonnull
	public DAXImageContent getSpaceBackground() throws IOException {
		return load("SHIPS.DAX", 128, VGAImage.class, BIGPIC);
	}

	@Nullable
	public DAXImageContent findImage(int id) throws IOException {
		if (idsFor(PIC).contains(id)) {
			return find(id, imageTypes.get(PIC), PIC);
		} else if (idsFor(BIGPIC).contains(id)) {
			return find(id, imageTypes.get(BIGPIC), BIGPIC);
		}
		return null;
	}

	@Nullable
	public DAXImageContent findImage(int id, @Nullable DAXContentType type) throws IOException {
		if (type == null) {
			return findImage(id);
		}
		if (!imageTypes.containsKey(type)) {
			return null;
		}
		return find(id, imageTypes.get(type), type);
	}
}
