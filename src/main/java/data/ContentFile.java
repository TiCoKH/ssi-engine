package data;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;

import common.ByteBufferWrapper;
import data.content.DAXContent;

public abstract class ContentFile {

	public abstract <T extends DAXContent> T getById(int id, Class<T> clazz);

	public abstract List<ByteBufferWrapper> getById(int id);

	public abstract Set<Integer> getIds();

	@Nonnull
	public static Optional<ContentFile> create(@Nonnull File f) throws IOException {
		Optional<ContentFileType> type = ContentFileType.getType(f);
		if (type.isPresent()) {
			try (FileChannel c = FileChannel.open(f.toPath(), StandardOpenOption.READ)) {
				// TODO endianness
				ByteBufferWrapper file = ByteBufferWrapper.allocateLE((int) c.size()).readFrom(c);
				switch (type.get()) {
					case DAX:
						return Optional.of(DAXFile.createFrom(file));
				}
			}
		}
		return Optional.empty();
	}

	public static boolean isKnown(File f) {
		return ContentFileType.getType(f).isPresent();
	}

	private static enum ContentFileType {
		DAX(".+?\\.DAX$");

		private String namePattern;

		private ContentFileType(@Nonnull String namePattern) {
			this.namePattern = namePattern;
		}

		@Nonnull
		public static Optional<ContentFileType> getType(File f) {
			for (ContentFileType type : values()) {
				if (f.getName().toUpperCase().matches(type.namePattern)) {
					return Optional.of(type);
				}
			}
			return Optional.empty();
		}
	}
}
