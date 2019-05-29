package common;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

public class FileMap {
	private static final List<String> SUBDIRS = Arrays.asList("DISK1", "DISK2", "DISK3");

	private Map<String, String> namesMap = new HashMap<>();

	private String gameDir;

	public FileMap(@Nonnull String gameDir) {
		this.gameDir = gameDir;

		File gameDirFile = new File(gameDir);

		Stream.of(gameDirFile.listFiles()).forEach(f -> {
			if (f.isFile()) {
				namesMap.put(f.getName().toUpperCase(), f.getName());
			}
			if (f.isDirectory() && SUBDIRS.contains(f.getName().toUpperCase())) {
				String[] subnames = f.list((dir, name) -> new File(dir, name).isFile());
				namesMap.putAll(Stream.of(subnames).collect(Collectors.toMap(String::toUpperCase, name -> f.getName() + File.separator + name)));
			}
		});
	}

	@Nonnull
	public List<String> findMatching(String pattern) {
		return namesMap.keySet().stream().filter(nameUC -> nameUC.matches(pattern)).collect(Collectors.toList());
	}

	@Nonnull
	public Optional<File> toFile(String nameUC) {
		if (namesMap.containsKey(nameUC)) {
			return Optional.of(new File(gameDir, namesMap.get(nameUC)));
		}
		return Optional.empty();
	}
}
