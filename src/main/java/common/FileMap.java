package common;

import static java.util.function.Function.identity;

import java.io.File;
import java.util.Optional;

import javax.annotation.Nonnull;

import io.vavr.collection.Array;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.collection.Set;

public class FileMap {
	private static final Seq<String> SUBDIRS = Array.of("DISK1", "DISK2", "DISK3");

	private final Map<String, File> namesMap;

	public FileMap(@Nonnull String gameDir) {
		namesMap = Array.of(new File(gameDir).listFiles())
			.flatMap(FileMap::toFilesSeq)
			.toMap(file -> file.getName().toUpperCase(), identity());
	}

	private static Seq<File> toFilesSeq(File file) {
		if (file.isFile()) {
			return Array.of(file);
		}
		if (file.isDirectory() && SUBDIRS.contains(file.getName().toUpperCase())) {
			return Array.of(file.listFiles((dir, name) -> new File(dir, name).isFile()));
		}
		return Array.empty();
	}

	@Nonnull
	public Set<String> findMatching(String pattern) {
		return namesMap.keySet().filter(nameUC -> nameUC.matches(pattern));
	}

	@Nonnull
	public Optional<File> toFile(String nameUC) {
		return namesMap.get(nameUC).toJavaOptional();
	}
}
