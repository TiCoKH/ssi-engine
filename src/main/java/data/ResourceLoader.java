package data;

import static io.vavr.API.Map;
import static java.util.function.Function.identity;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nonnull;

import io.vavr.API;
import io.vavr.collection.Array;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import io.vavr.collection.SortedSet;
import io.vavr.control.Try;

import common.FileMap;

public class ResourceLoader {
	private final FileMap fileMap;
	private final Map<ContentType, Set<String>> contentMap;

	private AtomicReference<Map<String, Try<ContentFile>>> files = new AtomicReference<>(Map());

	public ResourceLoader(@Nonnull FileMap fileMap) {
		this.fileMap = fileMap;

		contentMap = Array.of(ContentType.values())
			.toMap(identity(), content -> fileMap.findMatching(content.getFilePattern()));
	}

	@Nonnull
	public SortedSet<Integer> idsFor(@Nonnull ContentType type) {
		return filesFor(type).map(this::load)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.filter(Try::isSuccess)
			.map(Try::get)
			.flatMap(ContentFile::getIds)
			.toSortedSet();
	}

	@Nonnull
	public <T extends Content> Optional<Try<T>> find(int id, @Nonnull Class<T> clazz, @Nonnull ContentType type) {
		return filesFor(type).map(filename -> load(filename, id, clazz, type))
			.filter(Optional::isPresent)
			.getOrElse(Optional::empty);
	}

	@Nonnull
	protected Set<String> filesFor(@Nonnull ContentType type) {
		return contentMap.get(type).getOrElse(API::Set);
	}

	@Nonnull
	protected <T extends Content> Optional<Try<T>> load(@Nonnull String name, int blockId, @Nonnull Class<T> clazz,
		@Nonnull ContentType type) {
		return load(name).flatMap(t -> {
			return t.map(cf -> cf.getById(blockId, clazz, type)).getOrElse(Optional::empty);
		});
	}

	@Nonnull
	protected Optional<Try<ContentFile>> load(@Nonnull String name) {
		final Map<String, Try<ContentFile>> cache = files.get();
		if (cache.containsKey(name)) {
			return cache.get(name).toJavaOptional();
		}
		final Try<ContentFile> file = fileMap.toFile(name)
			.map(ContentFile::create)
			.orElseGet(() -> Try.failure(new FileNotFoundException(name)));
		files.updateAndGet(f -> f.containsKey(name) ? f : f.put(name, file));
		return Optional.of(file);
	}

	public Optional<File> toFile(String nameUC) {
		return fileMap.toFile(nameUC);
	}
}
