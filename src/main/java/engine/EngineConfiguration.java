package engine;

import common.FileMap;
import types.GameResourceConfiguration;

public class EngineConfiguration extends GameResourceConfiguration {

	private static final String CONFIG_MAIN_PREFIX = "main.";
	private static final String CONFIG_MAIN_NAME = CONFIG_MAIN_PREFIX + "name";
	private static final String CONFIG_CODE_BASE = "code.base";

	public EngineConfiguration(FileMap filemap) throws Exception {
		super(filemap);
	}

	public String getMainMenuName() {
		return getProperty(CONFIG_MAIN_NAME);
	}

	public String getMainMenuEntry(String entry) {
		return getProperty(CONFIG_MAIN_PREFIX + entry, "");
	}

	public int getCodeBase() {
		return Integer.parseInt(getProperty(CONFIG_CODE_BASE), 16);
	}
}
