package ui.shared.resource;

import static data.ContentType.BIGPIC;
import static data.ContentType.PIC;
import static data.ContentType.WALLDEF;
import static data.ContentType._8X8D;
import static java.nio.channels.FileChannel.open;
import static java.nio.file.StandardOpenOption.READ;

import java.util.StringTokenizer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.vavr.control.Try;

import common.ByteBufferWrapper;
import common.FileMap;
import data.ContentType;
import data.Resource;
import data.ResourceLoader;
import data.dungeon.WallDef;
import data.image.ImageContent;
import data.image.MonocromeLargeSymbols;
import data.image.MonocromeSymbols;

public class UIResourceLoader extends ResourceLoader {
	private static final String FRAMES_IN_MISC = "MISC";
	private static final String CURSOR_FILENAME = "CURSOR.DAX";

	private final UIResourceConfiguration config;

	public UIResourceLoader(@Nonnull FileMap fileMap, @Nonnull UIResourceConfiguration config) {
		super(fileMap);
		this.config = config;
	}

	@Nonnull
	public Resource<? extends ImageContent> getFont() {
		final String font = config.getFont();
		if (font.contains(",")) {
			final StringTokenizer st = new StringTokenizer(font, ",");
			final String archive = st.nextToken();
			final int blockId = Integer.parseUnsignedInt(st.nextToken());
			if (blockId == 201) {
				return load(archive, blockId, MonocromeSymbols.class, _8X8D);
			} else {
				return load(archive, blockId, _8X8D);
			}
		}
		// TODO a ContentFile for the font
		return Resource.ofOptional(toFile(font)).flatMap(fontFile -> {
			return Resource.of(Try.withResources(() -> open(fontFile.toPath(), READ)).<ImageContent>of(fc -> {
				final ByteBufferWrapper buf = ByteBufferWrapper.allocateLE((int) fc.size()).readFrom(fc);
				return new MonocromeLargeSymbols(buf.rewind(), _8X8D);
			}));
		});
	}

	@Nonnull
	public Resource<? extends ImageContent> getMisc() {
		final StringTokenizer st = new StringTokenizer(config.getMisc(), ",");
		final String archive = st.nextToken();
		final int blockId = Integer.parseUnsignedInt(st.nextToken());
		return load(archive, blockId, _8X8D);
	}

	@Nonnull
	protected Resource<? extends ImageContent> getFrames() {
		if (FRAMES_IN_MISC.equals(config.getFrameLocation())) {
			return getMisc();
		}
		final StringTokenizer st = new StringTokenizer(config.getFrameLocation(), ",");
		final String archive = st.nextToken();
		final int blockId = Integer.parseUnsignedInt(st.nextToken());
		return load(archive, blockId, _8X8D);
	}

	@Nonnull
	protected Resource<? extends ImageContent> getOverlandCursor() {
		if (toFile(CURSOR_FILENAME).isPresent())
			return load(CURSOR_FILENAME, 1, _8X8D);
		else if (idsFor(_8X8D).contains(204)) {
			return findImage(204, _8X8D);
		}
		return Resource.empty();
	}

	@Nonnull
	protected Resource<? extends ImageContent> findImage(int id) {
		if (idsFor(PIC).contains(id)) {
			return find(id, config.getImageTypeClass(PIC), PIC);
		} else if (idsFor(BIGPIC).contains(id)) {
			return find(id, config.getImageTypeClass(BIGPIC), BIGPIC);
		}
		return Resource.empty();
	}

	@Nonnull
	public Resource<? extends ImageContent> findImage(int id, @Nullable ContentType type) {
		if (type == null) {
			return findImage(id);
		}
		return find(id, config.getImageTypeClass(type), type);
	}

	@Nonnull
	public Resource<? extends ImageContent> find8x8d(int id, int wallDefId) {
		for (String filename : filesFor(WALLDEF)) {
			final Resource<WallDef> wallDef = load(filename, wallDefId, WallDef.class, WALLDEF);
			if (wallDef.isPresent()) {
				// 8X8D ids arent unique in some games
				// always load the 8X8D from the 8X8D?.DAX that corresponds to the WALLDEF?.DAX,
				// where the walls with id are found
				final Resource<? extends ImageContent> result = load(filename.replace("WALLDEF", "8X8D"), id, _8X8D);
				if (result.isPresentAndSuccess()) {
					return result;
				}
			}
		}
		return findImage(id, _8X8D);
	}

	@Nonnull
	protected Resource<? extends ImageContent> load(@Nonnull String name, int blockId, @Nonnull ContentType type) {
		return load(name).flatMap(cf -> cf.getById(blockId, config.getImageTypeClass(type), type));
	}
}
