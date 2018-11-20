package common;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

public class FileMap {
	private Map<String, String> namesMap = new HashMap<>();

	private String gameDir;

	public FileMap(@Nonnull String gameDir) {
		this.gameDir = gameDir;

		String[] filenames = new File(gameDir).list((dir, name) -> new File(dir, name).isFile());
		namesMap.putAll(Arrays.asList(filenames).stream().collect(Collectors.toMap(name -> name.toUpperCase(), name -> name)));
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
