package data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import common.FileMap;

public class ResourceLoader {
	private static final Map<ContentType, List<String>> contentMap = new EnumMap<>(ContentType.class);
	private Map<String, ContentFile> files = new HashMap<>();

	private FileMap fileMap;

	public ResourceLoader(@Nonnull FileMap fileMap) {
		this.fileMap = fileMap;

		for (ContentType content : ContentType.values()) {
			contentMap.put(content, fileMap.findMatching(content.getFilePattern()));
		}
	}

	@Nonnull
	public Set<Integer> idsFor(@Nonnull ContentType type) throws IOException {
		Set<Integer> result = new HashSet<>();
		List<String> filenames = contentMap.get(type);
		for (int i = 0; i < filenames.size(); i++) {
			ContentFile f = load(filenames.get(i));
			result.addAll(f.getIds());
		}
		return result;
	}

	@Nonnull
	protected Set<String> filesFor(@Nonnull ContentType type) throws IOException {
		return new HashSet<>(contentMap.get(type));
	}

	@Nullable
	public <T extends Content> T find(int id, @Nonnull Class<T> clazz, @Nonnull ContentType type) throws IOException {
		List<String> filenames = contentMap.get(type);
		for (int i = 0; i < filenames.size(); i++) {
			T dic = load(filenames.get(i), id, clazz, type);
			if (dic != null) {
				return dic;
			}
		}
		return null;
	}

	@Nonnull
	protected <T extends Content> T load(@Nonnull String name, int blockId, @Nonnull Class<T> clazz, @Nonnull ContentType type) throws IOException {

		return load(name).getById(blockId, clazz, type);
	}

	@Nonnull
	protected ContentFile load(@Nonnull String name) throws IOException {
		if (!files.containsKey(name)) {
			Optional<File> f = fileMap.toFile(name);
			if (f.isPresent()) {
				ContentFile.create(f.get()).ifPresent(c -> files.put(name, c));
			}
			if (!files.containsKey(name)) {
				throw new FileNotFoundException(name + " wasnt found in the game dir.");
			}
		}
		return files.get(name);
	}

	public Optional<File> toFile(String nameUC) {
		return fileMap.toFile(nameUC);
	}
}
