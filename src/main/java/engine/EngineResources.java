package engine;

import static data.content.DAXContentType.BACK;
import static data.content.DAXContentType.BIGPIC;
import static data.content.DAXContentType.PIC;
import static data.content.DAXContentType.SPRIT;
import static data.content.DAXContentType._8X8D;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import common.FileMap;
import data.DAXFile;
import data.content.DAXContent;
import data.content.DAXContentType;
import data.content.DAXImageContent;
import data.content.MonocromeSymbols;
import data.content.VGADependentImages;
import data.content.VGAImage;

public class EngineResources {
	private static final Map<DAXContentType, Class<? extends DAXImageContent>> imageTypes = new EnumMap<>(DAXContentType.class);
	static {
		imageTypes.put(_8X8D, VGAImage.class);
		imageTypes.put(BACK, VGAImage.class);
		imageTypes.put(BIGPIC, VGAImage.class);
		imageTypes.put(PIC, VGADependentImages.class);
		imageTypes.put(SPRIT, VGADependentImages.class);
	}

	private static final Map<DAXContentType, List<String>> contentMap = new EnumMap<>(DAXContentType.class);
	private Map<String, DAXFile> files = new HashMap<>();
	private FileMap fileMap;

	public EngineResources(FileMap fileMap) {
		this.fileMap = fileMap;

		for (DAXContentType content : DAXContentType.values()) {
			contentMap.put(content, fileMap.findMatching(content.getFilePattern()));
		}
	}

	public MonocromeSymbols getFont() throws IOException {
		return load("8X8D1.DAX", 201, MonocromeSymbols.class);
	}

	public DAXImageContent getBorders() throws IOException {
		return load("BORDERS.DAX", 0, VGAImage.class);
	}

	public DAXImageContent getOverlandCursor() throws IOException {
		return load("CURSOR.DAX", 1, VGAImage.class);
	}

	public DAXImageContent getSpaceSymbols() throws IOException {
		return load("8X8D0.DAX", 1, VGAImage.class);
	}

	public DAXImageContent getSpaceBackground() throws IOException {
		return load("SHIPS.DAX", 128, VGAImage.class);
	}

	public DAXImageContent getTitles(int id) throws IOException {
		return load("TITLE.DAX", id, VGAImage.class);
	}

	public DAXImageContent findImage(int id, DAXContentType type) throws IOException {
		return find(id, imageTypes.get(type), type);
	}

	public Set<Integer> idsFor(DAXContentType type) throws IOException {
		Set<Integer> result = new HashSet<>();
		List<String> filenames = contentMap.get(type);
		for (int i = 0; i < filenames.size(); i++) {
			DAXFile f = load(filenames.get(i));
			result.addAll(f.getIds());
		}
		return result;
	}

	public <T extends DAXContent> T find(int id, Class<T> clazz, DAXContentType type) throws IOException {
		List<String> filenames = contentMap.get(type);
		for (int i = 0; i < filenames.size(); i++) {
			T dic = load(filenames.get(i), id, clazz);
			if (dic != null) {
				return dic;
			}
		}
		return null;
	}

	public <T extends DAXContent> T load(String name, int blockId, Class<T> clazz) throws IOException {
		return load(name).getById(blockId, clazz);
	}

	private DAXFile load(String name) throws IOException {
		if (!files.containsKey(name)) {
			Optional<File> f = fileMap.toFile(name);
			if (f.isPresent()) {
				try (FileChannel c = FileChannel.open(f.get().toPath(), StandardOpenOption.READ)) {
					files.put(name, DAXFile.createFrom(c));
				}
			} else {
				throw new FileNotFoundException(name + " wasnt found in the game dir.");
			}
		}
		return files.get(name);
	}

	public File getSavesPath() {
		File parent = null;
		if (System.getenv("XDG_DATA_DIR") != null) {
			parent = new File(System.getenv("XDG_DATA_DIR"));
		} else if (System.getProperty("user.home") != null) {
			parent = new File(System.getProperty("user.home"), ".local" + File.separator + "share");
		} else {
			parent = new File(System.getProperty("user.dir"));
		}
		return new File(parent, "ssi-engine");
	}
}
