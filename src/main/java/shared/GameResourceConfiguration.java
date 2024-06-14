package shared;

import java.io.File;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

import io.vavr.collection.Array;
import io.vavr.collection.Seq;

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
		String[] propFiles = {
			"Pool of Radiance.properties",
			"Curse of the Azure Bonds.properties",
			"Treasures of the Savage Frontier.properties",
			"Pools of Darkness.properties",
			"Neverwinter Nights.properties",
			"Gateway to the Savage Frontier.properties",
			"Treasures of the Savage Frontier.properties",
			"Champions of Krynn.properties",
			"Death Knights of Krynn.properties",
			"The Dark Queen of Krynn.properties",
			"Buck Rogers - Countdown to Doomsday.properties",
			"Buck Rogers - Matrix Cubed.properties",
			"Forgotten Realm Unlimited Adventures.properties"
			// Add other properties files here
		};

		for (String filename : propFiles) {
			String game = filename.replace(".properties", "");

			Properties props = new Properties();
			try (InputStream in = ClassLoader.getSystemResourceAsStream(filename)) {
				if (in == null) {
					continue;  // Skip if the file is not found
				}
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

	public Seq<String> findProperties(String propStart) {
		return Array.ofAll(gameProperties.keySet())
			.filter(Objects::nonNull)
			.map(String.class::cast)
			.filter(key -> key.startsWith(propStart));
	}
}
