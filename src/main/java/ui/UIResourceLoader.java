package ui;

import static data.content.DAXContentType.BIGPIC;
import static data.content.DAXContentType.PIC;
import static data.content.DAXContentType.WALLDEF;
import static data.content.DAXContentType._8X8D;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.StringTokenizer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import common.ByteBufferWrapper;
import common.FileMap;
import data.ResourceLoader;
import data.content.DAXContentType;
import data.content.DAXImageContent;
import data.content.MonocromeLargeSymbols;
import data.content.MonocromeSymbols;
import data.content.WallDef;

public class UIResourceLoader extends ResourceLoader {
	private UIResourceConfiguration config;

	public UIResourceLoader(@Nonnull FileMap fileMap, @Nonnull UIResourceConfiguration config) throws IOException {
		super(fileMap);
		this.config = config;
	}

	@Nonnull
	public DAXImageContent getFont() throws IOException {
		String font = config.getFont();
		if (font.contains(",")) {
			StringTokenizer st = new StringTokenizer(font, ",");
			String archive = st.nextToken();
			int blockId = Integer.parseUnsignedInt(st.nextToken());
			if (blockId == 201) {
				return load(archive, blockId, MonocromeSymbols.class, _8X8D);
			} else {
				return load(archive, blockId, _8X8D);
			}
		}
		Optional<File> fontFile = toFile(font);
		if (fontFile.isPresent()) {
			try (FileChannel c = FileChannel.open(fontFile.get().toPath(), StandardOpenOption.READ)) {
				ByteBufferWrapper buf = ByteBufferWrapper.allocateLE((int) c.size()).readFrom(c);
				return new MonocromeLargeSymbols(buf.rewind(), _8X8D);
			}
		} else {
			throw new FileNotFoundException(font + " wasnt found in the game dir.");
		}
	}

	@Nonnull
	public DAXImageContent getMisc() throws IOException {
		StringTokenizer st = new StringTokenizer(config.getMisc(), ",");
		String archive = st.nextToken();
		int blockId = Integer.parseUnsignedInt(st.nextToken());
		return load(archive, blockId, _8X8D);
	}

	@Nonnull
	public DAXImageContent getFrames() throws IOException {
		if (config.getFrameLocation().equals("MISC")) {
			return getMisc();
		}
		StringTokenizer st = new StringTokenizer(config.getFrameLocation(), ",");
		String archive = st.nextToken();
		int blockId = Integer.parseUnsignedInt(st.nextToken());
		return load(archive, blockId, _8X8D);
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

	public DAXImageContent find8x8d(int id) throws IOException {
		for (String fn : filesFor(WALLDEF)) {
			WallDef wallDef = load(fn, id, WallDef.class, WALLDEF);
			if (wallDef != null) {
				// 8X8D ids arent unique in some games
				// always load the 8X8D from the 8X8D?.DAX that corresponds to the WALLDEF?.DAX,
				// where the walls with id are found
				return load(fn.replace("WALLDEF", "8X8D"), id, _8X8D);
			}
		}
		return null;
	}

	@Nonnull
	protected DAXImageContent load(@Nonnull String name, int blockId, @Nonnull DAXContentType type) throws IOException {
		return load(name).getById(blockId, config.getImageTypeClass(type), type);
	}
}
