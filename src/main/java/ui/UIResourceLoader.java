package ui;

import static data.content.DAXContentType.BIGPIC;
import static data.content.DAXContentType.PIC;
import static data.content.DAXContentType._8X8D;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import common.FileMap;
import data.ResourceLoader;
import data.content.DAXContentType;
import data.content.DAXImageContent;
import data.content.MonocromeSymbols;

public class UIResourceLoader extends ResourceLoader {
	private UIResourceConfiguration config;

	public UIResourceLoader(@Nonnull FileMap fileMap, @Nonnull UIResourceConfiguration config) throws IOException {
		super(fileMap);
		this.config = config;
	}

	@Nonnull
	public MonocromeSymbols getFont() throws IOException {
		return load("8X8D1.DAX", 201, MonocromeSymbols.class, _8X8D);
	}

	@Nonnull
	public DAXImageContent getMisc() throws IOException {
		return load("8X8D1.DAX", 202, _8X8D);
	}

	@Nonnull
	public DAXImageContent getBorders() throws IOException {
		return load("BORDERS.DAX", 0, _8X8D);
	}

	@Nonnull
	public DAXImageContent getOverlandCursor() throws IOException {
		return load("CURSOR.DAX", 1, _8X8D);
	}

	@Nullable
	public DAXImageContent findImage(int id) throws IOException {
		if (idsFor(PIC).contains(id)) {
			return find(id, config.getImageTypeClass(PIC), PIC);
		} else if (idsFor(BIGPIC).contains(id)) {
			return find(id, config.getImageTypeClass(BIGPIC), BIGPIC);
		}
		return null;
	}

	@Nullable
	public DAXImageContent findImage(int id, @Nullable DAXContentType type) throws IOException {
		if (type == null) {
			return findImage(id);
		}
		return find(id, config.getImageTypeClass(type), type);
	}

	@Nonnull
	protected DAXImageContent load(@Nonnull String name, int blockId, @Nonnull DAXContentType type) throws IOException {
		return load(name).getById(blockId, config.getImageTypeClass(type), type);
	}
}
