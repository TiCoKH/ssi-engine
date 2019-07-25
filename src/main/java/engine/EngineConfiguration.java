package engine;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import common.FileMap;
import engine.opcodes.EclOpCode;
import shared.GameResourceConfiguration;

public class EngineConfiguration extends GameResourceConfiguration {

	private static final String CONFIG_MAIN_PREFIX = "main.";
	private static final String CONFIG_MAIN_NAME = CONFIG_MAIN_PREFIX + "name";
	private static final String CONFIG_CODE_BASE = "code.base";
	private static final String CONFIG_ADDRESS_PREFIX = "address.";
	private static final String CONFIG_OVERLAND_MAP_IDS = "overland.map";

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

	public Map<Integer, EclOpCode> getOpCodes() {
		return Arrays.asList(EclOpCode.values()).stream().collect(Collectors.toMap(op -> op.getId(), op -> op, (op1, op2) -> select(op1, op2)));
	}

	private EclOpCode select(EclOpCode op1, EclOpCode op2) {
		String opCodeId = String.format("%02X", op1.getId());
		String opCodeName = getProperty("opcode." + opCodeId);
		if (opCodeName == null) {
			throw new IllegalArgumentException("OpCode 0x" + opCodeId + " has more than one version, but none is configured!");
		}
		try {
			EclOpCode.valueOf(opCodeName);
		} catch (Exception e) {
			throw new IllegalArgumentException("OpCode 0x" + opCodeId + " has an illegal name configured!");
		}
		if (op1.name().equals(opCodeName)) {
			return op1;
		}
		if (op2.name().equals(opCodeName)) {
			return op2;
		}
		return op1;
	}

	public int getEngineAddress(EngineAddress address) {
		return Integer.parseInt(getProperty(CONFIG_ADDRESS_PREFIX + address.name(), "0"), 16);
	}

	public Map<Integer, String> getEngineAdresses() {
		return findProperties(CONFIG_ADDRESS_PREFIX).stream()
			.collect(Collectors.toMap(k -> Integer.parseInt(getProperty(k), 16), k -> k.substring(CONFIG_ADDRESS_PREFIX.length())));
	}

	public List<Integer> getOverlandMapIds() {
		return Stream.of(getProperty(CONFIG_OVERLAND_MAP_IDS, "0").split(",")).map(Integer::parseInt).collect(Collectors.toList());
	}
}
