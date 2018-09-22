package engine;

import static data.content.DAXContentType.BACK;
import static data.content.DAXContentType.BIGPIC;
import static data.content.DAXContentType.ECL;
import static data.content.DAXContentType.GEO;
import static data.content.DAXContentType.PIC;
import static data.content.DAXContentType.WALLDEF;
import static data.content.DAXContentType._8X8D;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import data.DAXFile;
import data.content.DAXContent;
import data.content.DAXContentType;
import data.content.DAXImageContent;
import data.content.MonocromeSymbols;
import data.content.VGADependentImages;
import data.content.VGAImage;

public class EngineResources {
	private static final Map<DAXContentType, String[]> fileMap = new EnumMap<>(DAXContentType.class);
	static {
		fileMap.put(_8X8D, new String[] { "8X8D0.DAX", "8X8D1.DAX", "8X8D2.DAX", "8X8D3.DAX", "8X8D4.DAX", "8X8D5.DAX", "8X8D6.DAX" });
		fileMap.put(BACK, new String[] { "BACK1.DAX", "BACK2.DAX", "BACK3.DAX", "BACK4.DAX", "BACK5.DAX", "BACK6.DAX" });
		fileMap.put(BIGPIC, new String[] { "BIGPIC1.DAX", "BIGPIC2.DAX", "BIGPIC3.DAX", "BIGPIC4.DAX", "BIGPIC5.DAX", "BIGPIC6.DAX" });
		fileMap.put(ECL, new String[] { "ECL1.DAX", "ECL2.DAX", "ECL3.DAX", "ECL4.DAX", "ECL5.DAX", "ECL6.DAX" });
		fileMap.put(GEO, new String[] { "GEO1.DAX", "GEO2.DAX", "GEO3.DAX", "GEO4.DAX", "GEO5.DAX", "GEO6.DAX" });
		fileMap.put(PIC, new String[] { "PIC1.DAX", "PIC2.DAX", "PIC3.DAX", "PIC4.DAX", "PIC5.DAX", "PIC6.DAX", "PIC7.DAX", "PIC8.DAX", "PIC9.DAX" });
		fileMap.put(WALLDEF, new String[] { "WALLDEF1.DAX", "WALLDEF2.DAX", "WALLDEF3.DAX", "WALLDEF4.DAX", "WALLDEF5.DAX", "WALLDEF6.DAX" });
	}
	private static final Map<DAXContentType, Class<? extends DAXImageContent>> imageTypes = new EnumMap<>(DAXContentType.class);
	static {
		imageTypes.put(_8X8D, VGAImage.class);
		imageTypes.put(BACK, VGAImage.class);
		imageTypes.put(BIGPIC, VGAImage.class);
		imageTypes.put(PIC, VGADependentImages.class);
	}

	private String gameDir;
	private Map<String, DAXFile> files;

	public EngineResources(String gameDir) {
		this.gameDir = gameDir;
		this.files = new HashMap<>();
	}

	public MonocromeSymbols getFont() throws IOException {
		return load("8X8D1.DAX", 201, MonocromeSymbols.class);
	}

	public DAXImageContent getBorders() throws IOException {
		return load("BORDERS.DAX", 0, VGAImage.class);
	}

	public DAXImageContent getTitles(int id) throws IOException {
		return load("TITLE.DAX", id, VGAImage.class);
	}

	public DAXImageContent findImage(int id, DAXContentType type) throws IOException {
		return find(id, imageTypes.get(type), type);
	}

	public Set<Integer> idsFor(DAXContentType type) throws IOException {
		Set<Integer> result = new HashSet<>();
		String[] filenames = fileMap.get(type);
		for (int i = 0; i < filenames.length; i++) {
			DAXFile f = load(filenames[i]);
			result.addAll(f.getIds());
		}
		return result;
	}

	public <T extends DAXContent> T find(int id, Class<T> clazz, DAXContentType type) throws IOException {
		String[] filenames = fileMap.get(type);
		for (int i = 0; i < filenames.length; i++) {
			T dic = load(filenames[i], id, clazz);
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
		DAXFile f = files.get(name);
		if (f == null) {
			try (FileChannel c = FileChannel.open(new File(gameDir, name).toPath(), StandardOpenOption.READ)) {
				f = DAXFile.createFrom(c);
				files.put(name, f);
			}
		}
		return f;
	}
}
