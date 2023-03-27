package data;

import static java.nio.channels.FileChannel.open;
import static java.nio.file.StandardOpenOption.READ;

import java.io.File;
import java.util.Optional;

import javax.annotation.Nonnull;

import io.vavr.collection.Seq;
import io.vavr.collection.Set;
import io.vavr.control.Try;

import common.ByteBufferWrapper;

public abstract class ContentFile {

	public abstract <T extends Content> Resource<T> getById(int id, Class<T> clazz, ContentType type);

	public abstract Seq<ByteBufferWrapper> getById(int id);

	public abstract Set<Integer> getIds();

	@Nonnull
	public static Try<ContentFile> create(@Nonnull File f) {
		return ContentFileType.getType(f).map(type -> {
			return Try.withResources(() -> open(f.toPath(), READ)).of(channel -> {
				final ByteBufferWrapper file = ByteBufferWrapper.allocateLE((int) channel.size()).readFrom(channel);
				switch (type) {
					case DAX:
						return DAXFile.createFrom(file.rewind());
					case TLB:
						return TLBFile.createFrom(file.rewind());
				}
				throw new IllegalArgumentException("Unhandled file type for file " + f.getAbsolutePath());
			});
		})
			.orElseGet(
				() -> Try.failure(new IllegalArgumentException("Unknown file type for file " + f.getAbsolutePath())));
	}

	public static boolean isKnown(File f) {
		return ContentFileType.getType(f).isPresent();
	}

	private enum ContentFileType {
		DAX(".+?\\.DAX$"), TLB(".+?\\.[GT]LB$");

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
