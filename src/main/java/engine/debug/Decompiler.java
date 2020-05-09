package engine.debug;

import static engine.EngineAddress.SAVED_TEMP_START;
import static engine.EngineAddress.SEL_PC_START;
import static engine.EngineAddress.TEMP_START;
import static engine.debug.CodeBlockConnectionType.CONTINUE;
import static engine.debug.CodeBlockConnectionType.ELSE;
import static engine.debug.CodeBlockConnectionType.ELSE_CONTINUE;
import static engine.debug.CodeBlockConnectionType.G0TO;
import static engine.debug.CodeBlockConnectionType.SWITCH;
import static engine.debug.CodeBlockConnectionType.THEN;
import static engine.script.EclOpCode.AND;
import static engine.script.EclOpCode.COMPARE;
import static engine.script.EclOpCode.COMPARE_AND;
import static engine.script.EclOpCode.IF_EQUALS;
import static engine.script.EclOpCode.IF_GREATER;
import static engine.script.EclOpCode.IF_GREATER_EQUALS;
import static engine.script.EclOpCode.IF_LESS;
import static engine.script.EclOpCode.IF_LESS_EQUALS;
import static engine.script.EclOpCode.IF_NOT_EQUALS;
import static engine.script.EclOpCode.INPUT_YES_NO_22;
import static engine.script.EclOpCode.INPUT_YES_NO_2C;
import static engine.script.EclOpCode.OR;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;

import engine.EngineConfiguration;
import engine.VirtualMemory;
import engine.debug.Disassembler.JumpAddresses;
import engine.script.EclArgument;
import engine.script.EclInstruction;
import engine.script.EclOpCode;

public class Decompiler {
	private static final List<EclOpCode> OP_CODE_COMP = ImmutableList.of(COMPARE, COMPARE_AND, AND, OR, INPUT_YES_NO_22, INPUT_YES_NO_2C);

	private static final Map<Integer, String> KNOWN_ADRESSES = new HashMap<>();
	static {
		KNOWN_ADRESSES.put(0x4BAB, "ENGINE_CONF_4BAB"); // probably boolean flag game state changed
		KNOWN_ADRESSES.put(VirtualMemory.MEMLOC_SPACE_X, "SPACE_X");
		KNOWN_ADRESSES.put(VirtualMemory.MEMLOC_SPACE_Y, "SPACE_Y");
		KNOWN_ADRESSES.put(0x4BC5, "GEO_ID");
		KNOWN_ADRESSES.put(0x4BC7, "TIME_MIN_ONE");
		KNOWN_ADRESSES.put(0x4BC8, "TIME_MIN_TEN");
		KNOWN_ADRESSES.put(0x4BC9, "TIME_HOUR");
		KNOWN_ADRESSES.put(0x4BCA, "TIME_DAY");
		KNOWN_ADRESSES.put(0x4BCB, "TIME_YEAR");
		KNOWN_ADRESSES.put(0x4BE6, "DUNGEON_VALUE");
		KNOWN_ADRESSES.put(0x4BE7, "ENGINE_CONF_4BE7"); // configures OpCode LOAD_AREA_DECO
		KNOWN_ADRESSES.put(0x4BE8, "ENGINE_CONF_4BE8"); // configures OpCode LOAD_AREA_DECO
		KNOWN_ADRESSES.put(0x4BE9, "ENGINE_CONF_4BE9");
		KNOWN_ADRESSES.put(0x4BFB, "ENGINE_CONF_NO_AREA_MAP");
		KNOWN_ADRESSES.put(VirtualMemory.MEMLOC_ENGINE_CONF_GAME_SPEED, "ENGINE_CONF_GAME_SPEED");
		KNOWN_ADRESSES.put(0x4BFF, "PICS_ARE_DRAWN");
		KNOWN_ADRESSES.put(0x4C2F, "CURRENT_PIC");
		KNOWN_ADRESSES.put(VirtualMemory.MEMLOC_MED_SUPPLIES, "MED_SUPPLIES");
		KNOWN_ADRESSES.put(0x4C1A, "REPAIR_COST");
		KNOWN_ADRESSES.put(0x4CE6, "MONEY_NEO_ACCT");
		KNOWN_ADRESSES.put(VirtualMemory.MEMLOC_EXTENDED_DUNGEON_X, "EXTENDED_DUNGEON_X");
		KNOWN_ADRESSES.put(VirtualMemory.MEMLOC_EXTENDED_DUNGEON_Y, "EXTENDED_DUNGEON_Y");
		KNOWN_ADRESSES.put(VirtualMemory.MEMLOC_HULL, "HULL");
		KNOWN_ADRESSES.put(VirtualMemory.MEMLOC_SENSORS, "SENSORS");
		KNOWN_ADRESSES.put(VirtualMemory.MEMLOC_CONTROL, "CONTROL");
		KNOWN_ADRESSES.put(VirtualMemory.MEMLOC_LIFE, "LIFE");
		KNOWN_ADRESSES.put(VirtualMemory.MEMLOC_FUEL, "FUEL");
		KNOWN_ADRESSES.put(VirtualMemory.MEMLOC_ENGINE, "ENGINE");
		KNOWN_ADRESSES.put(VirtualMemory.MEMLOC_KCANNON_WEAPONS, "KCANNON_WEAPONS");
		KNOWN_ADRESSES.put(VirtualMemory.MEMLOC_KCANNON_AMMO, "KCANNON_AMMO");
		KNOWN_ADRESSES.put(VirtualMemory.MEMLOC_KCANNON_RELOAD, "KCANNON_RELOAD");
		KNOWN_ADRESSES.put(VirtualMemory.MEMLOC_MISSILE_WEAPONS, "MISSILE_WEAPONS");
		KNOWN_ADRESSES.put(VirtualMemory.MEMLOC_MISSILE_AMMO, "MISSILE_AMMO");
		KNOWN_ADRESSES.put(VirtualMemory.MEMLOC_MISSILE_RELOAD, "MISSILE_RELOAD");
		KNOWN_ADRESSES.put(VirtualMemory.MEMLOC_LASER_WEAPONS, "LASER_WEAPONS");
		KNOWN_ADRESSES.put(0x4D6C, "ENEMY_HULL");
		KNOWN_ADRESSES.put(0x4D6E, "ENEMY_SENSORS");
		KNOWN_ADRESSES.put(0x4D70, "ENEMY_CONTROL");
		KNOWN_ADRESSES.put(0x4D72, "ENEMY_LIFE");
		KNOWN_ADRESSES.put(0x4D76, "ENEMY_ENGINE");
		KNOWN_ADRESSES.put(0x4D7C, "ENEMY_WAS_ENTERED");
		KNOWN_ADRESSES.put(0x4D81, "ENEMY_WEAPONS");
		KNOWN_ADRESSES.put(0x7B90, "STRING1");
		KNOWN_ADRESSES.put(0x7C00, "SEL_PC_NAME");
		KNOWN_ADRESSES.put(0x7D00, "SEL_PC_STATUS");
		KNOWN_ADRESSES.put(0x7EC6, "COMBAT_MORALE_BASE");
		KNOWN_ADRESSES.put(0x7ECB, "COMBAT_IS_AMBUSH");
		List<String> celestials = ImmutableList.of("MERKUR", "VENUS", "EARTH", "MARS", "CERES", "VESTA", "FORTUNA", "PALLAS", "PSYCHE", "JUNO",
			"HYGEIA", "AURORA", "THULE");
		for (int i = 0; i < 13; i++) {
			KNOWN_ADRESSES.put(VirtualMemory.MEMLOC_CELESTIAL_POS_START + (2 * i), celestials.get(i) + "_X");
			KNOWN_ADRESSES.put(VirtualMemory.MEMLOC_CELESTIAL_POS_START + (2 * i) + 1, celestials.get(i) + "_Y");
		}
	}

	private int codeBase;

	public Decompiler(@Nonnull EngineConfiguration cfg) {
		this.codeBase = cfg.getCodeBase();

		EclInstruction.configOpCodes(cfg.getOpCodes());

		KNOWN_ADRESSES.putAll(cfg.getEngineAdresses());

		int savedTempStart = cfg.getEngineAddress(SAVED_TEMP_START);
		for (int i = savedTempStart; i < savedTempStart + 0x20; i++) {
			KNOWN_ADRESSES.put(i, "SAVED_TEMP_" + hex(i));
		}
		int selPCStart = cfg.getEngineAddress(SEL_PC_START);
		for (int i = selPCStart; i < selPCStart + 0x1FF; i++) {
			KNOWN_ADRESSES.put(i, "SEL_PC_" + hex(i));
		}
		int tempStart = cfg.getEngineAddress(TEMP_START);
		for (int i = 0; i < 0xA; i++) {
			KNOWN_ADRESSES.put(tempStart + i, "TEMP" + String.format("%01X", i + 1));
		}
	}

	public void updateKnownAddresses(@Nonnull Map<Integer, String> addressMap) {
		addressMap.putAll(KNOWN_ADRESSES);
	}

	public void updateKnownAddresses(@Nonnull Map<Integer, String> addressMap, @Nonnull Set<CodeBlock> blocks, int eclId) {
		blocks.stream().map(b -> findKnownAddresses(b, eclId)).forEach(m -> m.forEach((k, v) -> addressMap.putIfAbsent(k, v)));
	}

	public Set<CodeBlock> decompile(@Nonnull CodeSection section, @Nonnull Set<CodeBlock> blocks, @Nonnull Map<Integer, String> addressNames,
		@Nonnull JumpAddresses addresses) {

		Set<CodeBlock> result = new TreeSet<CodeBlock>();
		blocks.stream().forEach(b -> result.addAll(splitIfBlocks(b)));
		boolean aBlockWasCompacted;
		do {
			aBlockWasCompacted = false;
			for (CodeBlock b : result) {
				aBlockWasCompacted = compactIfBlocks(b, result, addresses);
				if (aBlockWasCompacted) {
					break;
				}
			}
		} while (aBlockWasCompacted);

		Map<Integer, CodeBlock> cbMap = new HashMap<>();
		result.stream().forEach(b -> cbMap.put(b.getStartAddress(), b));
		result.stream().forEach(b -> buildConnections(b, cbMap, addresses));

		result.stream().forEach(b -> b.getCode().stream().forEach(i -> updateInstruction(i, addressNames)));

		result.stream().forEach(b -> compactInstructions(b));

		return result;
	}

	private Set<CodeBlock> splitIfBlocks(@Nonnull CodeBlock block) {
		Set<CodeBlock> result = new HashSet<>();

		CodeBlock b1 = new CodeBlock(block.getCode(), block.getStartAddress(), block.getEndAddress());
		result.add(b1);

		int index = 0;
		do {
			EclinstructionWrapper inst = (EclinstructionWrapper) b1.getCode().get(index++);
			if (inst.isIf()) {
				CodeBlock b2 = b1.splitAfter(inst, codeBase);
				result.add(b2);
				CodeBlockConnection.register(b1, b2, THEN);

				CodeBlock b3 = b2.splitAfter(b2.getCode().get(0), codeBase);
				if (b3 != null) {
					CodeBlockConnection.register(b1, b3, ELSE_CONTINUE);
					result.add(b3);
				}
				b1 = b3;
				index = 0;
			}
		} while (b1 != null && index < b1.getCode().size());

		return result;
	}

	private boolean compactIfBlocks(CodeBlock b, Set<CodeBlock> allBlocks, JumpAddresses addresses) {
		EclinstructionWrapper inst = (EclinstructionWrapper) b.getLastInst();
		EclOpCode currentOp = inst.getOpCode();
		if (inst.isIf()) {
			CodeBlock then = b.getOutgoing(THEN).orElseThrow(() -> illegalState(inst)).getTo();
			CodeBlock to = b.getOutgoing(ELSE_CONTINUE).map(c -> c.getTo()).orElse(null);

			if (to != null && !addresses.isJumpAddress(to.getStartAddress())) {
				EclOpCode nextOp = ((EclinstructionWrapper) to.getCode().get(0)).getOpCode();
				if (currentOp == nextOp || isOppositeIfs(currentOp, nextOp)) {
					CodeBlock nextThen = to.getOutgoing(THEN).orElseThrow(() -> illegalState(inst)).getTo();

					if (currentOp == nextOp) {
						then.merge(nextThen);
						allBlocks.remove(nextThen);
					} else if (b.getOutgoing(ELSE).isPresent()) {
						b.getOutgoing(ELSE).get().getTo().merge(nextThen);
						allBlocks.remove(nextThen);
					} else {
						nextThen.getIncoming(THEN).ifPresent(c -> c.unregister());
						CodeBlockConnection.register(b, nextThen, ELSE);
					}
					b.replace(to);
					allBlocks.remove(to);
					return true;
				}
			}
		}
		return false;
	}

	private void buildConnections(CodeBlock b, Map<Integer, CodeBlock> cbMap, JumpAddresses addresses) {
		EclinstructionWrapper inst = (EclinstructionWrapper) b.getLastInst();
		switch (inst.getOpCode()) {
			case GOTO:
				CodeBlockConnection.register(b, cbMap.get(inst.getArgument(0).valueAsInt()), G0TO);
				break;
			case ON_GOTO:
				inst.getDynArgs().stream().forEach(a -> {
					CodeBlockConnection.register(b, cbMap.get(a.valueAsInt()), SWITCH);
				});
				// TODO fall through for implicit jump/continue for unknown values
				break;
			default:
				if (blockCanContinueAfter(inst)) {
					CodeBlock to;
					if (b.getIncoming(THEN).isPresent() || b.getIncoming(ELSE).isPresent()) {
						CodeBlock parent = b.getIncoming(THEN).isPresent() ? b.getIncoming(THEN).get().getFrom()
							: b.getIncoming(ELSE).get().getFrom();
						to = parent.getOutgoing(ELSE_CONTINUE).map(CodeBlockConnection::getTo).orElse(null);
						if (to == null)
							to = parent.getOutgoing(CONTINUE).map(CodeBlockConnection::getTo).orElseThrow(() -> illegalState(inst));
					} else {
						to = cbMap.get(b.getEndAddress());
					}
					CodeBlockConnection.register(b, to, CONTINUE);
				} else if (inst.isIf() && !b.getOutgoing(ELSE_CONTINUE).isPresent()) {
					int nextJump = addresses.getAllAddresses().tailSet(b.getStartAddress()).stream().filter(i -> i != b.getStartAddress()).findFirst()
						.orElseThrow(() -> illegalState(inst));
					CodeBlockConnection.register(b, cbMap.get(nextJump), CONTINUE);
				}
		}
	}

	private void updateInstruction(@Nonnull EclInstructionData data, @Nonnull Map<Integer, String> names) {
		if (!(data instanceof EclinstructionWrapper))
			return;
		EclinstructionWrapper inst = (EclinstructionWrapper) data;
		EclOpCode opCode = inst.getOpCode();
		if (opCode.hasDestAddress()) {
			EclArgument lArg = inst.getArgument(opCode.getArgIndexDestAddress());
			inst.setlArgBase(lArg);
			inst.setlArgBaseName(names.get(lArg.valueAsInt()));
		}
		for (int i = 0; i < inst.getArguments().length; i++) {
			if (!opCode.hasDestAddress() || i != opCode.getArgIndexDestAddress()) {
				inst.setrArgName(i, argName(inst, i, names));
			}
		}
		switch (opCode) {
			case ADD:
				inst.setrArgExpression(argName(inst, 0, names) + " + " + argName(inst, 1, names));
				break;
			case SUBTRACT:
				inst.setrArgExpression(argName(inst, 1, names) + " - " + argName(inst, 0, names));
				break;
			case DIVIDE:
				inst.setrArgExpression(argName(inst, 0, names) + " / " + argName(inst, 1, names));
				break;
			case MULTIPLY:
				inst.setrArgExpression(argName(inst, 0, names) + " * " + argName(inst, 1, names));
				break;
			case RANDOM:
				inst.setrArgExpression("RANDOM(" + argName(inst, 0, names) + ")");
				break;
			case WRITE_MEM:
				inst.setrArgExpression(argName(inst, 0, names));
				break;
			case INPUT_NUMBER:
				inst.setrArgExpression("INPUT_NUMBER(" + argName(inst, 0, names) + ")");
				break;
			case INPUT_STRING:
				inst.setrArgExpression("INPUT_STRING(" + argName(inst, 0, names) + ")");
				break;
			case MENU_VERTICAL:
				String vMenu = String.join(", ", inst.getDynArgs().stream().map(a -> argName(a, names)).collect(Collectors.toList()));
				inst.setrArgExpression("MENU_VERTICAL(" + vMenu + ")");
				break;
			case COPY_MEM:
				inst.setrArgExpression("[" + argName(inst, 0, names) + " + " + argName(inst, 1, names) + "]");
				break;
			case MENU_HORIZONTAL:
				String hMenu = String.join(", ", inst.getDynArgs().stream().map(a -> argName(a, names)).collect(Collectors.toList()));
				inst.setrArgExpression("MENU_HORIZONTAL(" + hMenu + ")");
				break;
			case AND:
				inst.setrArgExpression(argName(inst, 0, names) + " & " + argName(inst, 1, names));
				break;
			case OR:
				inst.setrArgExpression(argName(inst, 0, names) + " | " + argName(inst, 1, names));
				break;
			case SELECT_ACTION:
				String selMenu = String.join(", ", inst.getDynArgs().stream().map(a -> argName(a, names)).collect(Collectors.toList()));
				inst.setrArgExpression("SELECT_ACTION(" + selMenu + ")");
				break;
			case WRITE_MEM_BASE_OFF:
				inst.setlArgOffset(inst.getArgument(2));
				inst.setlArgOffsetName(argName(inst, 2, names));
				inst.setrArgExpression(argName(inst, 0, names));
				break;
			case RANDOM0:
				inst.setrArgExpression("RANDOM0(" + argName(inst, 1, names) + ")");
				break;
			default:
				break;
		}
	}

	private void compactInstructions(@Nonnull CodeBlock block) {
		EclinstructionWrapper compare = null;
		List<EclInstructionData> code = block.getCode();
		for (EclInstructionData data : block.getCode()) {
			if (!(data instanceof EclinstructionWrapper))
				continue;
			EclinstructionWrapper inst = (EclinstructionWrapper) data;
			if (OP_CODE_COMP.contains(inst.getOpCode())) {
				compare = inst;
			}
			if (inst.isIf() && compare != null) {
				EclInstructionCompareIf cmpIf = new EclInstructionCompareIf(compare, inst);
				code.set(code.indexOf(inst), cmpIf);
				if (!compare.getOpCode().hasDestAddress()) {
					code.remove(compare);
				}
			}
		}
		block.setCode(code);
	}

	@Nonnull
	private Map<Integer, String> findKnownAddresses(@Nonnull CodeBlock block, int eclId) {
		Map<Integer, String> result = new HashMap<>();

		block.getCode().stream() //
			.filter(i -> i instanceof EclinstructionWrapper) //
			.map(i -> (EclinstructionWrapper) i) //
			.filter(i -> i.getOpCode().hasDestAddress()) //
			.map(i -> i.getArgument(i.getOpCode().getArgIndexDestAddress())) //
			.forEach(a -> result.putIfAbsent(a.valueAsInt(), argName(a, eclId)));

		return result;
	}

	private boolean blockCanContinueAfter(EclinstructionWrapper inst) {
		return !inst.isBlockFinisher() && !inst.isIf();
	}

	private boolean isOppositeIfs(EclOpCode ifOpCode, EclOpCode opCode) {
		switch (ifOpCode) {
			case IF_EQUALS:
				return opCode == IF_NOT_EQUALS;
			case IF_NOT_EQUALS:
				return opCode == IF_EQUALS;
			case IF_LESS:
				return opCode == IF_GREATER_EQUALS;
			case IF_GREATER:
				return opCode == IF_LESS_EQUALS;
			case IF_LESS_EQUALS:
				return opCode == IF_GREATER;
			case IF_GREATER_EQUALS:
				return opCode == IF_LESS;
			default:
				return false;
		}
	}

	private String argName(EclinstructionWrapper inst, int argIndex, @Nonnull Map<Integer, String> addressNames) {
		return argName(inst.getArgument(argIndex), addressNames);
	}

	private String argName(EclArgument a, @Nonnull Map<Integer, String> addressNames) {
		if (a.isMemAddress()) {
			return addressNames.getOrDefault(a.valueAsInt(), "[" + a.toString() + "]");
		}
		if (a.isNumberValue()) {
			return Integer.toString(a.valueAsInt());
		}
		return '"' + a.valueAsString().toString() + '"';
	}

	private String argName(EclArgument a, int eclId) {
		if (a.isStringValue()) {
			return "string_" + eclId + "_" + hex(a.valueAsInt());
		} else if (a.isShortValue()) {
			return "short_" + eclId + "_" + hex(a.valueAsInt());
		} else {
			return "byte_" + eclId + "_" + hex(a.valueAsInt());
		}
	}

	private static String hex(int value) {
		return String.format("%04X", value);
	}

	private static IllegalStateException illegalState(EclinstructionWrapper inst) {
		return new IllegalStateException(String.format("%s: %s", hex(inst.getPosition()), inst.toString()));
	}
}
