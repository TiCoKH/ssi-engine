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
import static io.vavr.API.Map;
import static io.vavr.API.Seq;
import static java.util.function.Function.identity;

import java.util.Optional;

import javax.annotation.Nonnull;

import io.vavr.API;
import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.collection.SortedSet;

import engine.EngineConfiguration;
import engine.VirtualMemory;
import engine.script.EclArgument;
import engine.script.EclInstruction;
import engine.script.EclOpCode;

public class Decompiler {
	private static final Seq<EclOpCode> OP_CODE_COMP = Seq(COMPARE, COMPARE_AND, AND, OR, INPUT_YES_NO_22,
		INPUT_YES_NO_2C);

	private Map<Integer, String> KNOWN_ADRESSES = HashMap.ofEntries( //
		Map.entry(0x4BAB, "ENGINE_CONF_4BAB"), // probably boolean flag game state changed
		Map.entry(VirtualMemory.MEMLOC_SPACE_X, "SPACE_X"), //
		Map.entry(VirtualMemory.MEMLOC_SPACE_Y, "SPACE_Y"), //
		Map.entry(0x4BC5, "GEO_ID"), //
		Map.entry(0x4BC7, "TIME_MIN_ONE"), //
		Map.entry(0x4BC8, "TIME_MIN_TEN"), //
		Map.entry(0x4BC9, "TIME_HOUR"), //
		Map.entry(0x4BCA, "TIME_DAY"), //
		Map.entry(0x4BCB, "TIME_YEAR"), //
		Map.entry(0x4BE6, "DUNGEON_VALUE"), //
		Map.entry(0x4BE7, "ENGINE_CONF_4BE7"), // configures OpCode LOAD_AREA_DECO
		Map.entry(0x4BE8, "ENGINE_CONF_4BE8"), // configures OpCode LOAD_AREA_DECO
		Map.entry(0x4BE9, "ENGINE_CONF_4BE9"), //
		Map.entry(0x4BFB, "ENGINE_CONF_NO_AREA_MAP"), //
		Map.entry(VirtualMemory.MEMLOC_ENGINE_CONF_GAME_SPEED, "ENGINE_CONF_GAME_SPEED"), //
		Map.entry(0x4BFF, "PICS_ARE_DRAWN"), //
		Map.entry(0x4C2F, "CURRENT_PIC"), //
		Map.entry(VirtualMemory.MEMLOC_MED_SUPPLIES, "MED_SUPPLIES"), //
		Map.entry(0x4C1A, "REPAIR_COST"), //
		Map.entry(0x4CE6, "MONEY_NEO_ACCT"), //
		Map.entry(VirtualMemory.MEMLOC_EXTENDED_DUNGEON_X, "EXTENDED_DUNGEON_X"), //
		Map.entry(VirtualMemory.MEMLOC_EXTENDED_DUNGEON_Y, "EXTENDED_DUNGEON_Y"), //
		Map.entry(VirtualMemory.MEMLOC_HULL, "HULL"), //
		Map.entry(VirtualMemory.MEMLOC_SENSORS, "SENSORS"), //
		Map.entry(VirtualMemory.MEMLOC_CONTROL, "CONTROL"), //
		Map.entry(VirtualMemory.MEMLOC_LIFE, "LIFE"), //
		Map.entry(VirtualMemory.MEMLOC_FUEL, "FUEL"), //
		Map.entry(VirtualMemory.MEMLOC_ENGINE, "ENGINE"), //
		Map.entry(VirtualMemory.MEMLOC_KCANNON_WEAPONS, "KCANNON_WEAPONS"), //
		Map.entry(VirtualMemory.MEMLOC_KCANNON_AMMO, "KCANNON_AMMO"), //
		Map.entry(VirtualMemory.MEMLOC_KCANNON_RELOAD, "KCANNON_RELOAD"), //
		Map.entry(VirtualMemory.MEMLOC_MISSILE_WEAPONS, "MISSILE_WEAPONS"), //
		Map.entry(VirtualMemory.MEMLOC_MISSILE_AMMO, "MISSILE_AMMO"), //
		Map.entry(VirtualMemory.MEMLOC_MISSILE_RELOAD, "MISSILE_RELOAD"), //
		Map.entry(VirtualMemory.MEMLOC_LASER_WEAPONS, "LASER_WEAPONS"), //
		Map.entry(0x4D6C, "ENEMY_HULL"), //
		Map.entry(0x4D6E, "ENEMY_SENSORS"), //
		Map.entry(0x4D70, "ENEMY_CONTROL"), //
		Map.entry(0x4D72, "ENEMY_LIFE"), //
		Map.entry(0x4D76, "ENEMY_ENGINE"), //
		Map.entry(0x4D7C, "ENEMY_WAS_ENTERED"), //
		Map.entry(0x4D81, "ENEMY_WEAPONS"), //
		Map.entry(0x7B90, "STRING1"), //
		Map.entry(0x7C00, "SEL_PC_NAME"), //
		Map.entry(0x7D00, "SEL_PC_STATUS"), //
		Map.entry(0x7EC6, "COMBAT_MORALE_BASE"), //
		Map.entry(0x7ECB, "COMBAT_IS_AMBUSH") //
	)
		.merge( //
			Seq("MERKUR", "VENUS", "EARTH", "MARS", "CERES", "VESTA", "FORTUNA", "PALLAS", "PSYCHE", "JUNO", "HYGEIA",
				"AURORA", "THULE").zipWithIndex().flatMap(t2 -> {
					final String celestial = t2._1;
					final int index = t2._2;
					return Map(VirtualMemory.MEMLOC_CELESTIAL_POS_START + (2 * index), celestial + "_X",
						VirtualMemory.MEMLOC_CELESTIAL_POS_START + (2 * index) + 1, celestial + "_Y");
				}).toMap(Tuple2::_1, Tuple2::_2) //
		);

	public Decompiler(@Nonnull EngineConfiguration cfg) {
		EclInstruction.configOpCodes(cfg.getOpCodes());

		KNOWN_ADRESSES = KNOWN_ADRESSES.merge(cfg.getEngineAdresses());

		int savedTempStart = cfg.getEngineAddress(SAVED_TEMP_START);
		for (int i = savedTempStart; i < savedTempStart + 0x20; i++) {
			KNOWN_ADRESSES = KNOWN_ADRESSES.put(i, "SAVED_TEMP_" + hex(i));
		}
		int selPCStart = cfg.getEngineAddress(SEL_PC_START);
		for (int i = selPCStart; i < selPCStart + 0x1FF; i++) {
			KNOWN_ADRESSES = KNOWN_ADRESSES.put(i, "SEL_PC_" + hex(i));
		}
		int tempStart = cfg.getEngineAddress(TEMP_START);
		for (int i = 0; i < 0xA; i++) {
			KNOWN_ADRESSES = KNOWN_ADRESSES.put(tempStart + i, "TEMP" + String.format("%01X", i + 1));
		}
	}

	public Map<Integer, String> updateKnownAddresses(@Nonnull Map<Integer, String> addressMap) {
		return addressMap.merge(KNOWN_ADRESSES);
	}

	public Map<Integer, String> updateKnownAddresses(@Nonnull Map<Integer, String> addressMap,
		@Nonnull SortedSet<CodeBlock> blocks, int eclId) {
		return addressMap.merge(blocks.toArray().map(b -> findDestinationAddresses(b, eclId)).reduce(Map::merge));
	}

	public SortedSet<CodeBlock> decompile(@Nonnull SortedSet<CodeBlock> blocks,
		@Nonnull Map<Integer, String> addressNames) {

		SortedSet<CodeBlock> result = blocks;
		result = splitOnIf(result);
		result = splitThenBlocks(result);
		buildConnections(result);
		updateInstruction(result, addressNames);
		result = compactSameIfBlocks(result);
		result = compactOppositeIfBlocks(result);
		result = compactInstructions(result);

		return result;
	}

	private SortedSet<CodeBlock> splitOnIf(SortedSet<CodeBlock> blocks) {
		SortedSet<CodeBlock> result = blocks;
		Optional<Tuple2<CodeBlock, EclinstructionWrapper>> blockToSplit = result.find(this::containsIf)
			.toJavaOptional()
			.flatMap(this::findIf);
		while (blockToSplit.isPresent()) {
			final Tuple2<CodeBlock, EclinstructionWrapper> t2 = blockToSplit.get();
			final CodeBlock block = t2._1;
			final EclinstructionWrapper eclIf = t2._2;
			final SortedSet<CodeBlock> splitBlock = block.splitAfter(eclIf, THEN);
			if (splitBlock.size() == 1) {
				final CodeBlock next = result.dropUntil(block::equals).tail().head();
				CodeBlockConnection.register(block, next, THEN);
			} else {
				result = result.remove(block).addAll(splitBlock);
			}
			blockToSplit = result.find(this::containsIf).toJavaOptional().flatMap(this::findIf);
		}
		return result;
	}

	private boolean containsIf(CodeBlock block) {
		return findIf(block).isPresent();
	}

	private Optional<Tuple2<CodeBlock, EclinstructionWrapper>> findIf(CodeBlock block) {
		return block.getCode()
			.filter(EclinstructionWrapper.class::isInstance)
			.map(EclinstructionWrapper.class::cast)
			.find(inst -> isUnhandledIf(block, inst))
			.map(inst -> API.Tuple(block, inst))
			.toJavaOptional();
	}

	private boolean isUnhandledIf(CodeBlock block, EclinstructionWrapper inst) {
		return inst.isIf() && (!block.getLastInst().equals(inst) || !block.getOutgoing(THEN).isPresent());
	}

	private SortedSet<CodeBlock> splitThenBlocks(SortedSet<CodeBlock> blocks) {
		SortedSet<CodeBlock> result = blocks;
		Optional<CodeBlock> blockToSplit = result.find(this::containsUnhandledThen).toJavaOptional();
		while (blockToSplit.isPresent()) {
			final CodeBlock block = blockToSplit.get();
			if (block.getCode().size() > 1) {
				final SortedSet<CodeBlock> splitBlock = block.splitAfter(block.getFirstInst(), CONTINUE);
				CodeBlockConnection.register(splitBlock.head().getIncoming(THEN).get().getFrom(), splitBlock.last(),
					ELSE_CONTINUE);
				result = result.remove(block).addAll(splitBlock);
			} else {
				final CodeBlock next = result.dropUntil(block::equals).tail().head();
				CodeBlockConnection.register(block, next, CONTINUE);
				CodeBlockConnection.register(block.getIncoming(THEN).get().getFrom(), next, ELSE_CONTINUE);
			}
			blockToSplit = result.find(this::containsUnhandledThen).toJavaOptional();
		}
		return result;
	}

	private boolean containsUnhandledThen(CodeBlock block) {
		return block.getIncoming(THEN).isPresent() && block.getOutgoing(CONTINUE).isEmpty();
	}

	private SortedSet<CodeBlock> compactSameIfBlocks(SortedSet<CodeBlock> blocks) {
		SortedSet<CodeBlock> result = blocks;
		Optional<CodeBlockConnection> connToMerge = findSameIf(result);
		while (connToMerge.isPresent()) {
			final CodeBlockConnection conn = connToMerge.get();
			final CodeBlock ifBlock = conn.getFrom();
			final CodeBlock thenBlock = ifBlock.getOutgoing(THEN).get().getTo();
			final CodeBlock mergableIf = conn.getTo();
			final CodeBlock mergableThen = mergableIf.getOutgoing(THEN).get().getTo();
			final CodeBlock thenAfterMerge = thenBlock.merge(mergableThen);
			CodeBlockConnection.register(ifBlock, mergableIf.getOutgoing(ELSE_CONTINUE).get().getTo(), ELSE_CONTINUE);
			CodeBlockConnection.register(thenAfterMerge, mergableThen.getOutgoing(CONTINUE).get().getTo(), CONTINUE);
			mergableIf.unregister();
			mergableThen.unregister();
			result = result.remove(thenBlock).remove(mergableIf).remove(mergableThen).add(thenAfterMerge);
			connToMerge = findSameIf(result);
		}
		return result;
	}

	private SortedSet<CodeBlock> compactOppositeIfBlocks(SortedSet<CodeBlock> blocks) {
		SortedSet<CodeBlock> result = blocks;
		Optional<CodeBlockConnection> connToMerge = findOppositeIf(result);
		while (connToMerge.isPresent()) {
			final CodeBlockConnection conn = connToMerge.get();
			final CodeBlock ifBlock = conn.getFrom();
			final CodeBlock thenBlock = ifBlock.getOutgoing(THEN).get().getTo();
			final CodeBlock mergableIf = conn.getTo();
			final CodeBlock elseBlock = mergableIf.getOutgoing(THEN).get().getTo();
			ifBlock.getOutgoing(ELSE_CONTINUE).get().unregister();
			CodeBlockConnection.register(ifBlock, elseBlock, ELSE);
			thenBlock.getOutgoing(CONTINUE).get().unregister();
			CodeBlockConnection.register(thenBlock, elseBlock.getOutgoing(CONTINUE).get().getTo(), CONTINUE);
			result = result.remove(mergableIf);
			connToMerge = findOppositeIf(result);
		}
		return result;
	}

	private Optional<CodeBlockConnection> findSameIf(SortedSet<CodeBlock> blocks) {
		return blocks.filter(this::endsWithIf)
			.find(this::nextBlockHasSameIf)
			.toJavaOptional()
			.flatMap(block -> block.getOutgoing(ELSE_CONTINUE));
	}

	private Optional<CodeBlockConnection> findOppositeIf(SortedSet<CodeBlock> blocks) {
		return blocks.filter(this::endsWithIf)
			.find(this::nextBlockHasOppositeIf)
			.toJavaOptional()
			.flatMap(block -> block.getOutgoing(ELSE_CONTINUE));
	}

	private boolean endsWithIf(CodeBlock block) {
		return isIf(block.getLastInst());
	}

	private boolean isIf(EclInstructionData inst) {
		return inst instanceof EclinstructionWrapper && ((EclinstructionWrapper) inst).isIf();
	}

	private boolean nextBlockHasSameIf(CodeBlock block) {
		final EclinstructionWrapper eclIf = (EclinstructionWrapper) block.getLastInst();
		return block.getOutgoing(ELSE_CONTINUE).map(conn -> {
			// ELSE_CONTINUE and CONTINE of previous if-else are ok
			if (conn.getTo().getIncomingCount() == 2 && isIf(conn.getTo().getFirstInst())) {
				final EclinstructionWrapper inst = (EclinstructionWrapper) conn.getTo().getFirstInst();
				return eclIf.getOpCode().equals(inst.getOpCode());
			}
			return false;
		}).orElse(false);
	}

	private boolean nextBlockHasOppositeIf(CodeBlock block) {
		return block.getOutgoing(ELSE_CONTINUE).map(conn -> {
			// ELSE_CONTINUE and CONTINE of previous if-else are ok
			if (conn.getTo().getIncomingCount() == 2 && isIf(conn.getTo().getFirstInst())) {
				return isOppositeIfs(block.getLastInst(), conn.getTo().getFirstInst());
			}
			return false;
		}).orElse(false);
	}

	private void buildConnections(SortedSet<CodeBlock> blocks) {
		final Map<Integer, CodeBlock> cbMap = blocks.toMap(CodeBlock::getStartAddress, identity());
		blocks.forEach(block -> {
			final EclinstructionWrapper inst = (EclinstructionWrapper) block.getLastInst();
			switch (inst.getOpCode()) {
				case GOTO:
					CodeBlockConnection.register(block,
						cbMap.get(inst.getArgument(0).valueAsInt()).getOrElseThrow(() -> illegalState(inst)), G0TO);
					break;
				case ON_GOTO:
					inst.getDynArgs().forEach(a -> {
						CodeBlockConnection.register(block,
							cbMap.get(a.valueAsInt()).getOrElseThrow(() -> illegalState(inst)), SWITCH);
					});
					// TODO fall through for implicit jump/continue for unknown values
					break;
				default:
					if ((blockCanContinueAfter(inst) && !block.getOutgoing(CONTINUE).isPresent())) {
						final CodeBlock next = blocks.dropUntil(block::equals).tail().head();
						CodeBlockConnection.register(block, next, CONTINUE);
					}
			}
		});
	}

	private static final Map<EclOpCode, String> OPS = HashMap.of(EclOpCode.ADD, " + ", EclOpCode.DIVIDE, " / ",
		EclOpCode.MULTIPLY, " * ", EclOpCode.AND, " & ", EclOpCode.OR, " | ");

	private void updateInstruction(SortedSet<CodeBlock> blocks, Map<Integer, String> argNames) {
		blocks.flatMap(CodeBlock::getCode).forEach(data -> {
			if (!(data instanceof EclinstructionWrapper))
				return;
			final EclinstructionWrapper inst = (EclinstructionWrapper) data;
			final EclOpCode opCode = inst.getOpCode();
			if (opCode.hasDestAddress()) {
				EclArgument lArg = inst.getArgument(opCode.getArgIndexDestAddress());
				inst.setlArgBase(lArg);
				inst.setlArgBaseName(argNames.get(lArg.valueAsInt()).getOrElseThrow(() -> illegalState(inst)));
			}
			for (int i = 0; i < inst.getArguments().length; i++) {
				if (!opCode.hasDestAddress() || i != opCode.getArgIndexDestAddress()) {
					inst.setrArgName(i, argName(inst, i, argNames));
				}
			}
			switch (opCode) {
				case ADD:
				case DIVIDE:
				case MULTIPLY:
				case AND:
				case OR:
					final String op = OPS.get(opCode).get();
					inst.setrArgExpression(argName(inst, 0, argNames) + op + argName(inst, 1, argNames));
					break;
				case SUBTRACT:
					inst.setrArgExpression(argName(inst, 1, argNames) + " - " + argName(inst, 0, argNames));
					break;
				case RANDOM:
				case RANDOM0:
				case INPUT_NUMBER:
				case INPUT_STRING:
					inst.setrArgExpression(opCode.name() + "(" + argName(inst, 0, argNames) + ")");
					break;
				case WRITE_MEM:
					inst.setrArgExpression(argName(inst, 0, argNames));
					break;
				case MENU_VERTICAL:
				case MENU_HORIZONTAL:
				case SELECT_ACTION:
					inst.setrArgExpression(
						inst.getDynArgs().map(a -> argName(a, argNames)).mkString(opCode.name() + "(", ", ", ")"));
					break;
				case COPY_MEM:
					inst.setrArgExpression("[" + argName(inst, 0, argNames) + " + " + argName(inst, 1, argNames) + "]");
					break;
				case WRITE_MEM_BASE_OFF:
					inst.setlArgOffset(inst.getArgument(2));
					inst.setlArgOffsetName(argName(inst, 2, argNames));
					inst.setrArgExpression(argName(inst, 0, argNames));
					break;
				default:
					break;
			}
		});
	}

	private SortedSet<CodeBlock> compactInstructions(SortedSet<CodeBlock> blocks) {
		EclinstructionWrapper compare = null;
		SortedSet<CodeBlock> result = blocks;
		for (CodeBlock block : blocks) {
			CodeBlock resultBlock = block;
			for (EclInstructionData data : block.getCode()) {
				if (!(data instanceof EclinstructionWrapper))
					continue;
				final EclinstructionWrapper inst = (EclinstructionWrapper) data;
				if (OP_CODE_COMP.contains(inst.getOpCode())) {
					compare = inst;
				} else if (inst.isIf() && compare != null) {
					final EclInstructionCompareIf cmpIf = new EclInstructionCompareIf(compare, inst);
					resultBlock = resultBlock.replace(inst, cmpIf);
					if (!compare.getOpCode().hasDestAddress()) {
						resultBlock = resultBlock.remove(compare);
					}
				}
			}
			result = result.remove(block).add(resultBlock);
		}
		return result;
	}

	private Map<Integer, String> findDestinationAddresses(CodeBlock block, int eclId) {
		return block.getCode()
			.toArray()
			.filter(EclinstructionWrapper.class::isInstance)
			.map(EclinstructionWrapper.class::cast)
			.filter(i -> i.getOpCode().hasDestAddress())
			.map(i -> i.getArgument(i.getOpCode().getArgIndexDestAddress()))
			.toMap(EclArgument::valueAsInt, arg -> argName(arg, eclId));
	}

	private boolean blockCanContinueAfter(EclinstructionWrapper inst) {
		return !inst.isBlockFinisher() && !inst.isIf();
	}

	private boolean isOppositeIfs(EclInstructionData ecl1, EclInstructionData ecl2) {
		return isOppositeIfs(((EclinstructionWrapper) ecl1).getOpCode(), ((EclinstructionWrapper) ecl2).getOpCode());
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

	private String argName(EclinstructionWrapper inst, int argIndex, Map<Integer, String> addressNames) {
		return argName(inst.getArgument(argIndex), addressNames);
	}

	private String argName(EclArgument a, Map<Integer, String> addressNames) {
		if (a.isMemAddress()) {
			return addressNames.get(a.valueAsInt()).getOrElse(() -> "[" + a.toString() + "]");
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
