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
import data.content.DAXContent;
import data.content.DAXContentType;

public class ResourceLoader {
	private static final Map<DAXContentType, List<String>> contentMap = new EnumMap<>(DAXContentType.class);
	private Map<String, ContentFile> files = new HashMap<>();

	private FileMap fileMap;

	public ResourceLoader(@Nonnull FileMap fileMap) {
		this.fileMap = fileMap;

		for (DAXContentType content : DAXContentType.values()) {
			contentMap.put(content, fileMap.findMatching(content.getFilePattern()));
		}
	}

	@Nonnull
	public Set<Integer> idsFor(@Nonnull DAXContentType type) throws IOException {
		Set<Integer> result = new HashSet<>();
		List<String> filenames = contentMap.get(type);
		for (int i = 0; i < filenames.size(); i++) {
			ContentFile f = load(filenames.get(i));
			result.addAll(f.getIds());
		}
		return result;
	}

	@Nullable
	public <T extends DAXContent> T find(int id, @Nonnull Class<T> clazz, @Nonnull DAXContentType type) throws IOException {
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
	protected <T extends DAXContent> T load(@Nonnull String name, int blockId, @Nonnull Class<T> clazz, @Nonnull DAXContentType type)
		throws IOException {

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
}
