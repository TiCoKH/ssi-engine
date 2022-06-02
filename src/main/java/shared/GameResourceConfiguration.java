package shared;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import common.FileMap;
import common.MD5Util;

public class GameResourceConfiguration {

	private FileMap filemap;

	private String gameName;
	private Properties gameProperties;

	public GameResourceConfiguration(FileMap filemap) throws Exception {
		this.filemap = filemap;

		findConfig();

		if (gameProperties == null) {
			throw new IllegalArgumentException("No matching game config found!");
		}
	}

	private void findConfig() throws Exception {
		File cp = new File(ClassLoader.getSystemResource(".").toURI());
		String[] propFiles = cp.list((dir, name) -> name.endsWith(".properties"));

		for (String filename : propFiles) {
			String game = filename.replace(".properties", "");

			Properties props = new Properties();
			try (InputStream in = ClassLoader.getSystemResourceAsStream(filename)) {
				props.load(in);
			}

			String detectionFilename = props.getProperty("detection.name");
			String detectionMD5 = props.getProperty("detection.md5");

			Optional<File> detectionFile = filemap.toFile(detectionFilename);
			if (detectionFile.isEmpty()) {
				continue;
			}

			String md5Sum = MD5Util.getMd5For(detectionFile.get());
			if (md5Sum.equals(detectionMD5)) {
				System.out.println("Reading game properties: " + game);
				gameName = game;
				gameProperties = props;
				break;
			}
		}
	}

	public String getGameName() {
		return gameName;
	}

	public boolean isUsingFeature(GameFeature feature) {
		return Boolean.parseBoolean(getProperty("uses." + feature.name(), "false"));
	}

	public String getProperty(String key) {
		return gameProperties.getProperty(key);
	}

	public String getProperty(String key, String defaultValue) {
		return gameProperties.getProperty(key, defaultValue);
	}

	public List<String> findProperties(String propStart) {
		return gameProperties.keySet()
			.stream()
			.map(k -> (String) k)
			.filter(k -> k != null && k.startsWith(propStart))
			.collect(Collectors.toList());
	}
}
