package engine;

import static engine.rulesystem.Flavors.STANDARD;
import static io.vavr.API.Seq;

import io.vavr.collection.Array;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;

import common.FileMap;
import data.character.CharacterValues;
import engine.character.PlayerDataType;
import engine.rulesystem.Flavor;
import engine.rulesystem.Flavors;
import engine.script.EclOpCode;
import engine.text.SpecialCharType;
import shared.GameResourceConfiguration;

public class EngineConfiguration extends GameResourceConfiguration {

	private static final String CONFIG_MODE_MENU_PREFIX = "mode.";
	private static final String CONFIG_MODE_MENU_NAME = CONFIG_MODE_MENU_PREFIX + "name";
	private static final String CONFIG_CODE_BASE = "code.base";
	private static final String CONFIG_ADDRESS_PREFIX = "address.";
	private static final String CONFIG_OVERLAND_MAP_IDS = "overland.map";
	private static final String CONFIG_SPECIAL_CHAR_PREFIX = "ecl.char.";

	private static final String CONFIG_CHARACTER_FORMAT = "character.format";
	private static final String CONFIG_CHARACTER_VALUES = "character.values";
	private static final String CONFIG_RULE_FLAVOR = "rule.flavor";
	private static final String CONFIG_STARTING_EXP = "rule.starting_experience";
	private static final String CONFIG_STARTING_LEVEL = "rule.starting_level";

	public EngineConfiguration(FileMap filemap) throws Exception {
		super(filemap);
	}

	public String getModeMenuName() {
		return getProperty(CONFIG_MODE_MENU_NAME);
	}

	public String getModeMenuEntry(String entry) {
		return getProperty(CONFIG_MODE_MENU_PREFIX + entry, "");
	}

	public int getCodeBase() {
		return Integer.parseInt(getProperty(CONFIG_CODE_BASE), 16);
	}

	public Map<Integer, EclOpCode> getOpCodes() {
		return Seq(EclOpCode.values()).filter(this::select).toMap(EclOpCode::getId, op -> op);
	}

	private boolean select(EclOpCode op) {
		final String opCodeId = String.format("%02X", op.getId());
		final String opCodeName = getProperty("opcode." + opCodeId);
		if (opCodeName == null) {
			if (EclOpCode.byId(op.getId()).size() > 1) {
				throw new IllegalArgumentException(
					"OpCode 0x" + opCodeId + " has more than one version, but none is configured!");
			}
			return true;
		}
		return op.name().equals(opCodeName);
	}

	public int getEngineAddress(EngineAddress address) {
		return Integer.parseInt(getProperty(CONFIG_ADDRESS_PREFIX + address.name(), "0"), 16);
	}

	public Map<Integer, String> getEngineAdresses() {
		return findProperties(CONFIG_ADDRESS_PREFIX).toMap(k -> Integer.parseInt(getProperty(k), 16),
			k -> k.substring(CONFIG_ADDRESS_PREFIX.length()));
	}

	public Seq<Integer> getOverlandMapIds() {
		return Array.of(getProperty(CONFIG_OVERLAND_MAP_IDS, "0").split(",")).map(Integer::parseInt);
	}

	public char getSpecialChar(SpecialCharType type) {
		return getProperty(CONFIG_SPECIAL_CHAR_PREFIX + type.name(), "\0").charAt(0);
	}

	public PlayerDataType getCharacterFormat() {
		return PlayerDataType.valueOf(getProperty(CONFIG_CHARACTER_FORMAT));
	}

	public CharacterValues getCharacterValues() {
		return CharacterValues.valueOf(getProperty(CONFIG_CHARACTER_VALUES));
	}

	public Flavor getFlavor() {
		return Flavors.valueOf(getProperty(CONFIG_RULE_FLAVOR, STANDARD.name())).getFlavor();
	}

	public int getStartingExperience() {
		return Integer.parseInt(getProperty(CONFIG_STARTING_EXP, "-1"));
	}

	public int getStartingLevel() {
		return Integer.parseInt(getProperty(CONFIG_STARTING_LEVEL, "-1"));
	}
}
