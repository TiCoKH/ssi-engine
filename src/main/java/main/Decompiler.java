package main;

import static data.content.DAXContentType.ECL;
import static engine.EngineAddress.SAVED_TEMP_START;
import static engine.EngineAddress.SEL_PC_START;
import static engine.EngineAddress.TEMP_START;
import static engine.opcodes.EclOpCode.ADD;
import static engine.opcodes.EclOpCode.AND;
import static engine.opcodes.EclOpCode.CALL;
import static engine.opcodes.EclOpCode.COMPARE;
import static engine.opcodes.EclOpCode.COMPARE_AND;
import static engine.opcodes.EclOpCode.COPY_MEM;
import static engine.opcodes.EclOpCode.DIVIDE;
import static engine.opcodes.EclOpCode.ENCOUNTER_MENU;
import static engine.opcodes.EclOpCode.EXIT;
import static engine.opcodes.EclOpCode.GOSUB;
import static engine.opcodes.EclOpCode.GOTO;
import static engine.opcodes.EclOpCode.IF_EQUALS;
import static engine.opcodes.EclOpCode.IF_GREATER;
import static engine.opcodes.EclOpCode.IF_GREATER_EQUALS;
import static engine.opcodes.EclOpCode.IF_LESS;
import static engine.opcodes.EclOpCode.IF_LESS_EQUALS;
import static engine.opcodes.EclOpCode.IF_NOT_EQUALS;
import static engine.opcodes.EclOpCode.INPUT_YES_NO_22;
import static engine.opcodes.EclOpCode.INPUT_YES_NO_2C;
import static engine.opcodes.EclOpCode.MENU_HORIZONTAL;
import static engine.opcodes.EclOpCode.MULTIPLY;
import static engine.opcodes.EclOpCode.NEW_ECL;
import static engine.opcodes.EclOpCode.ON_GOSUB;
import static engine.opcodes.EclOpCode.ON_GOTO;
import static engine.opcodes.EclOpCode.OR;
import static engine.opcodes.EclOpCode.PARLAY;
import static engine.opcodes.EclOpCode.RANDOM;
import static engine.opcodes.EclOpCode.RANDOM0;
import static engine.opcodes.EclOpCode.RETURN;
import static engine.opcodes.EclOpCode.SELECT_ACTION;
import static engine.opcodes.EclOpCode.STOP_MOVE_23;
import static engine.opcodes.EclOpCode.STOP_MOVE_42;
import static engine.opcodes.EclOpCode.SUBTRACT;
import static engine.opcodes.EclOpCode.WRITE_MEM;
import static engine.opcodes.EclOpCode.WRITE_MEM_BASE_OFF;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

import common.ByteBufferWrapper;
import common.FileMap;
import data.ResourceLoader;
import data.content.EclProgram;
import engine.EngineAddress;
import engine.EngineConfiguration;
import engine.VirtualMemory;
import engine.opcodes.EclArgument;
import engine.opcodes.EclInstruction;
import engine.opcodes.EclOpCode;

public class Decompiler {
	private static final List<EclOpCode> OP_CODE_STOP = ImmutableList.of(EXIT, STOP_MOVE_23, STOP_MOVE_42, GOTO, ON_GOTO, RETURN, NEW_ECL);
	private static final List<EclOpCode> OP_CODE_COMP = ImmutableList.of(COMPARE, COMPARE_AND, AND, OR, INPUT_YES_NO_22, INPUT_YES_NO_2C);
	private static final List<EclOpCode> OP_CODE_IF = ImmutableList.of(IF_EQUALS, IF_GREATER, IF_GREATER_EQUALS, IF_LESS, IF_LESS_EQUALS,
		IF_NOT_EQUALS);
	private static final List<EclOpCode> OP_CODE_MATH = ImmutableList.of(WRITE_MEM, WRITE_MEM_BASE_OFF, COPY_MEM, ADD, SUBTRACT, MULTIPLY, DIVIDE,
		AND, OR, RANDOM, RANDOM0, ENCOUNTER_MENU, PARLAY);
	private static final List<EclOpCode> OP_CODE_HEX_ARGS = ImmutableList.of(AND, OR);

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
		KNOWN_ADRESSES.put(VirtualMemory.MEMLOC_OVERLAND_CITY, "OVERLAND_CITY");
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
	private int base;
	private int size;
	private int indention = 0;

	private SortedMap<Integer, Boolean> gotoAddressList = new TreeMap<>();

	private EclInstruction compare = null;
	private EclOpCode lastIf = null;
	private boolean wasCompare = false;

	private PrintStream out;

	private int currentId;

	public Decompiler(String gameDir) throws Exception {
		FileMap fm = new FileMap(gameDir);
		ResourceLoader res = new ResourceLoader(fm);
		EngineConfiguration cfg = new EngineConfiguration(fm);

		base = cfg.getCodeBase();
		EclInstruction.configOpCodes(cfg.getOpCodes());

		for (EngineAddress address : EngineAddress.values()) {
			KNOWN_ADRESSES.put(cfg.getEngineAddress(address), address.name());
		}
		int savedTempStart = cfg.getEngineAddress(SAVED_TEMP_START);
		for (int i = savedTempStart; i < savedTempStart + 0x20; i++) {
			KNOWN_ADRESSES.put(i, "SAVED_TEMP_" + hex(i));
		}
		int selPCStart = cfg.getEngineAddress(SEL_PC_START);
		for (int i = selPCStart; i < selPCStart + 0x103; i++) {
			KNOWN_ADRESSES.put(i, "SEL_PC_" + hex(i));
		}
		int tempStart = cfg.getEngineAddress(TEMP_START);
		for (int i = 0; i < 0xA; i++) {
			KNOWN_ADRESSES.put(tempStart + i, "TEMP" + String.format("%01X", i + 1));
		}

		Set<Integer> ids = new TreeSet<>(res.idsFor(ECL));
		for (Integer id : ids) {
			currentId = id;
			EclProgram eclCode = res.find(id, EclProgram.class, ECL);
			System.out.println(id);
			start(gameDir, eclCode);
		}
	}

	private void start(String gameDir, EclProgram ecl) throws IOException {
		ByteBufferWrapper eclCode = ecl.getCode();
		size = eclCode.limit();
		EclInstruction onMove = EclInstruction.parseNext(eclCode);
		EclInstruction onSearchLocation = EclInstruction.parseNext(eclCode);
		EclInstruction onRest = EclInstruction.parseNext(eclCode);
		EclInstruction onRestInterruption = EclInstruction.parseNext(eclCode);
		EclInstruction onInit = EclInstruction.parseNext(eclCode);

		startSection(gameDir, eclCode, onInit, "onInit");
		startSection(gameDir, eclCode, onMove, "onMove");
		startSection(gameDir, eclCode, onSearchLocation, "onSearchLocation");
		startSection(gameDir, eclCode, onRest, "onRest");
		startSection(gameDir, eclCode, onRestInterruption, "onRestInterruption");
	}

	private void startSection(String gameDir, ByteBufferWrapper eclCode, EclInstruction inst, String section) throws IOException {
		File outFile = new File(gameDir + "/ECL/ECL." + currentId + "." + section);
		outFile.getParentFile().mkdirs();
		out = new PrintStream(outFile);
		try {
			System.out.println(section + ":");
			disassemble(eclCode, inst, section);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		} finally {
			out.close();
		}
	}

	private void disassemble(ByteBufferWrapper eclCode, EclInstruction inst, String name) {
		gotoAddressList.clear();

		// To collect Goto and Gosub adresses
		disassemble(eclCode, inst.getArgument(0).valueAsInt(), false);
		int sizeBefore = 0, sizeAfter = gotoAddressList.size();
		while (sizeAfter > sizeBefore) {
			sizeBefore = gotoAddressList.size();
			Map<Integer, Boolean> copy = new HashMap<>(gotoAddressList);
			copy.keySet().stream().forEach(address -> {
				disassemble(eclCode, address, false);
			});
			sizeAfter = gotoAddressList.size();
		}

		out.println(name + "() {");
		indention += 1;

		disassemble(eclCode, inst.getArgument(0).valueAsInt(), true);
		addAdjacentGotos(eclCode);

		indention -= 1;
		out.println("}");

		while (gotoAddressList.containsValue(Boolean.FALSE)) {
			Integer a = gotoAddressList.entrySet().stream().filter(e -> Boolean.FALSE.equals(e.getValue())).findFirst().get().getKey();
			out.println();
			indention += 1;
			disassemble(eclCode, a, true);
			indention -= 1;
			gotoAddressList.put(a, Boolean.TRUE);
		}
	}

	private void addAdjacentGotos(ByteBufferWrapper eclCode) {
		int address = base + eclCode.position();
		while (gotoAddressList.containsKey(address)) {
			disassemble(eclCode, address, true);
			gotoAddressList.put(address, Boolean.TRUE);
			address = base + eclCode.position();
		}
	}

	private void disassemble(ByteBufferWrapper eclCode, int address, boolean withOutput) {
		if ((address - base) < size && (address - base) > 0) {
			eclCode.position(address - base);
			EclInstruction inst;
			do {
				inst = EclInstruction.parseNext(eclCode);
				disassembleInst(eclCode, inst, withOutput);
			} while (!OP_CODE_STOP.contains(inst.getOpCode()));
		} else {
			System.err.println("Adress " + hex(address) + " is outside the ECL code block!");
		}
	}

	private void disassembleInst(ByteBufferWrapper eclCode, EclInstruction inst, boolean withOutput) {
		EclOpCode opCode = inst.getOpCode();

		if (wasCompare && (!OP_CODE_IF.contains(opCode) || OP_CODE_MATH.contains(compare.getOpCode())) && withOutput) {
			output(compare);
		}
		wasCompare = false;

		if (opCode.hasDynArgs()) {
			List<EclArgument> dynArgs = inst.getDynArgs();

			if (opCode == ON_GOTO) {
				gotoAddressList.putAll(dynArgs.stream().filter(a -> !gotoAddressList.containsKey(a.valueAsInt())).map(EclArgument::valueAsInt)
					.sorted().collect(Collectors.toSet()).stream().collect(Collectors.toMap(Function.identity(), a -> {
						return Boolean.FALSE;
					})));
			} else if (opCode == ON_GOSUB) {
				gotoAddressList.putAll(dynArgs.stream().filter(a -> !gotoAddressList.containsKey(a.valueAsInt())).map(EclArgument::valueAsInt)
					.sorted().collect(Collectors.toSet()).stream().collect(Collectors.toMap(Function.identity(), a -> {
						return Boolean.FALSE;
					})));
			}
			if (withOutput)
				output(inst, dynArgs);
		} else if (OP_CODE_COMP.contains(opCode)) {
			compare = inst;
			lastIf = null;
			wasCompare = true;
		} else if (OP_CODE_IF.contains(opCode)) {
			if (withOutput && (lastIf == null || lastIf != opCode)) {
				outputCompare(compare, inst);
			}
			lastIf = opCode;

			indention += 1;
			EclInstruction compResultInst = EclInstruction.parseNext(eclCode);
			if (OP_CODE_COMP.contains(compResultInst.getOpCode()) && withOutput)
				output(compResultInst);
			else
				disassembleInst(eclCode, compResultInst, withOutput);
			indention -= 1;
		} else {
			if (opCode == GOTO) {
				addToMap(gotoAddressList, inst.getArgument(0));
			} else if (opCode == GOSUB) {
				addToMap(gotoAddressList, inst.getArgument(0));
			}
			if (withOutput)
				output(inst);
		}
	}

	private void addToMap(Map<Integer, Boolean> map, EclArgument a) {
		if (!map.containsKey(a.valueAsInt())) {
			map.put(a.valueAsInt(), Boolean.FALSE);
		}
	}

	private void outputLinestart(int address) {
		out.print(hex(address) + ":");
		for (int i = 0; i < indention; i++) {
			out.print('\t');
		}
	}

	private void outputGotoMarker(int address) {
		out.print("AT ");
		outputLinestart(address);
		out.println();
	}

	private void outputInstStart(int address) {
		if (gotoAddressList.containsKey(address) && Boolean.FALSE.equals(gotoAddressList.get(address))) {
			outputGotoMarker(address);
			gotoAddressList.put(address, Boolean.TRUE);
		}
		outputLinestart(address);
	}

	private void output(EclInstruction inst) {
		outputInstStart(base + inst.getPosition());
		if (OP_CODE_MATH.contains(inst.getOpCode()))
			outputMath(inst);
		else if (inst.getOpCode() != GOTO && inst.getOpCode() != GOSUB && inst.getOpCode() != CALL) {
			out.print(opCodeName(inst) + "(");
			for (int i = 0; i < inst.getArguments().length; i++) {
				if (i != 0)
					out.print(", ");
				out.print(argR(inst, i));
			}
			out.println(")");
		} else {
			out.println(inst);
		}
	}

	private void outputMath(EclInstruction inst) {
		switch (inst.getOpCode()) {
			case WRITE_MEM:
				out.println(argL(inst, 1) + " = " + argR(inst, 0));
				break;
			case WRITE_MEM_BASE_OFF:
				out.println(inst.getArgument(1) + " + " + argR(inst, 2) + " = " + argR(inst, 0));
				break;
			case COPY_MEM:
				out.println(argL(inst, 2) + " = [" + argR(inst, 0) + " + " + argR(inst, 1) + "]");
				break;
			case ADD:
				out.println(argL(inst, 2) + " = " + argR(inst, 0) + " + " + argR(inst, 1));
				break;
			case SUBTRACT:
				out.println(argL(inst, 2) + " = " + argR(inst, 1) + " - " + argR(inst, 0));
				break;
			case MULTIPLY:
				out.println(argL(inst, 2) + " = " + argR(inst, 0) + " * " + argR(inst, 1));
				break;
			case DIVIDE:
				out.println(argL(inst, 2) + " = " + argR(inst, 0) + " / " + argR(inst, 1));
				break;
			case AND:
				out.println(argL(inst, 2) + " = " + argR(inst, 0) + " & " + argR(inst, 1));
				break;
			case OR:
				out.println(argL(inst, 2) + " = " + argR(inst, 0) + " | " + argR(inst, 1));
				break;
			case RANDOM:
				out.println(argL(inst, 1) + " = RANDOM(" + argR(inst, 0) + ")");
				break;
			case RANDOM0:
				out.println(argL(inst, 0) + " = RANDOM0(" + argR(inst, 1) + ")");
				break;
			case ENCOUNTER_MENU:
				String argsEM = String.join(", ", argR(inst, 0), argR(inst, 1), argR(inst, 2), argR(inst, 4), argR(inst, 5), argR(inst, 6),
					argR(inst, 7), argR(inst, 8), argR(inst, 9), argR(inst, 10), argR(inst, 11), argR(inst, 12), argR(inst, 13));
				out.println(argL(inst, 3) + " = ENCOUNTER_MENU(" + argsEM + ")");
				break;
			case PARLAY:
				String argsP = String.join(", ", argR(inst, 0), argR(inst, 1), argR(inst, 2), argR(inst, 3), argR(inst, 4));
				out.println(argL(inst, 5) + " = PARLAY(" + argsP + ")");
				break;
			default:
				break;
		}
	}

	private void outputCompare(EclInstruction compInst, EclInstruction ifInst) {
		outputInstStart(base + compInst.getPosition());
		out.print("if (");
		switch (compInst.getOpCode()) {
			case COMPARE:
				outputCompare(compInst, 0, 1, ifInst.getOpCode());
				break;
			case COMPARE_AND:
				out.print("(");
				outputCompare(compInst, 0, 1, IF_EQUALS);
				out.print(" && ");
				outputCompare(compInst, 2, 3, IF_EQUALS);
				out.print(")");
				outputCompareOp(ifInst.getOpCode());
				out.print("TRUE");
				break;
			case AND:
				out.print(argR(compInst, 0));
				out.print(" & ");
				out.print(argR(compInst, 1));
				outputCompareOp(ifInst.getOpCode());
				out.print("0");
				break;
			case OR:
				out.print(argR(compInst, 0));
				out.print(" | ");
				out.print(argR(compInst, 1));
				outputCompareOp(ifInst.getOpCode());
				out.print("0");
				break;
			case INPUT_YES_NO_22:
			case INPUT_YES_NO_2C:
				out.print(opCodeName(compInst) + "()");
				outputCompareOp(ifInst.getOpCode());
				out.print("YES");
				break;
			default:
				throw new IllegalArgumentException("unkown compare statement " + compInst);
		}
		out.println(")");
	}

	private void outputCompare(EclInstruction compInst, int argIndex1, int argIndex2, EclOpCode ifOp) {
		if (compInst.getArgument(argIndex2).isMemAddress() && !compInst.getArgument(argIndex1).isMemAddress()) {
			out.print(argR(compInst, argIndex2));
			outputCompareOp(exchange(ifOp));
			out.print(argR(compInst, argIndex1));
		} else {
			out.print(argR(compInst, argIndex1));
			outputCompareOp(ifOp);
			out.print(argR(compInst, argIndex2));
		}
	}

	private void outputCompareOp(EclOpCode ifOp) {
		String operator;
		switch (ifOp) {
			case IF_EQUALS:
				operator = "==";
				break;
			case IF_GREATER:
				operator = ">";
				break;
			case IF_GREATER_EQUALS:
				operator = ">=";
				break;
			case IF_LESS:
				operator = "<";
				break;
			case IF_LESS_EQUALS:
				operator = "<=";
				break;
			case IF_NOT_EQUALS:
				operator = "!=";
				break;
			default:
				throw new IllegalArgumentException("unkown if statement " + ifOp);
		}
		out.print(" ");
		out.print(operator);
		out.print(" ");
	}

	private EclOpCode exchange(EclOpCode ifOp) {
		switch (ifOp) {
			case IF_EQUALS:
			case IF_NOT_EQUALS:
				return ifOp;
			case IF_GREATER:
				return IF_LESS;
			case IF_GREATER_EQUALS:
				return IF_LESS_EQUALS;
			case IF_LESS:
				return IF_GREATER;
			case IF_LESS_EQUALS:
				return IF_GREATER_EQUALS;
			default:
				throw new IllegalArgumentException("unkown if statement " + ifOp);
		}
	}

	private void output(EclInstruction inst, List<EclArgument> dynArgs) {
		outputInstStart(base + inst.getPosition());
		if (inst.getOpCode() == ON_GOTO || inst.getOpCode() == ON_GOSUB) {
			out.print("ON " + argL(inst, 0) + (inst.getOpCode() == ON_GOTO ? " GOTO " : " GOSUB "));
		} else if (inst.getOpCode() == MENU_HORIZONTAL || inst.getOpCode() == SELECT_ACTION) {
			out.print(argL(inst, 0) + " = " + opCodeName(inst));
		} else {
			out.print(opCodeName(inst) + "(");
			for (int i = 0; i < inst.getArguments().length; i++) {
				if (i != 0)
					out.print(", ");
				out.print(argR(inst, i));
			}
			out.print(")");
		}
		out.println("(" + String.join(", ", dynArgs.stream().map(EclArgument::toString).collect(Collectors.toList())) + ")");
	}

	private String argL(EclInstruction inst, int argNr) {
		EclArgument a = inst.getArgument(argNr);
		if (!a.isMemAddress()) {
			System.err.println("Value is not a memory address at " + hex(inst.getPosition() + base));
		}
		if (!KNOWN_ADRESSES.containsKey(a.valueAsInt())) {
			if (a.isStringValue()) {
				KNOWN_ADRESSES.put(a.valueAsInt(), "string_" + currentId + "_" + hex(a.valueAsInt()));
			} else if (a.isShortValue()) {
				KNOWN_ADRESSES.put(a.valueAsInt(), "short_" + currentId + "_" + hex(a.valueAsInt()));
			} else {
				KNOWN_ADRESSES.put(a.valueAsInt(), "byte_" + currentId + "_" + hex(a.valueAsInt()));
			}
		}
		return KNOWN_ADRESSES.get(a.valueAsInt());
	}

	private String argR(EclInstruction inst, int argNr) {
		EclArgument a = inst.getArgument(argNr);
		if (a.isMemAddress()) {
			if (KNOWN_ADRESSES.containsKey(a.valueAsInt())) {
				return KNOWN_ADRESSES.get(a.valueAsInt());
			} else if (a.valueAsInt() >= base && a.valueAsInt() <= (base + size)) {
				return "CODE_" + hex(a.valueAsInt());
			} else {
				return "[" + a.toString() + "]";
			}
		}
		if (a.isNumberValue()) {
			if (OP_CODE_HEX_ARGS.contains(inst.getOpCode())) {
				return "0x" + Integer.toHexString(a.valueAsInt()).toUpperCase();
			}
		}
		return a.toString();
	}

	private static String hex(int value) {
		return String.format("%04X", value);
	}

	private static String opCodeName(EclInstruction inst) {
		String opName = inst.getOpCode().name();
		if (opName.endsWith(String.format("_%02X", inst.getOpCode().getId()))) {
			opName = opName.substring(0, opName.length() - 3);
		}
		return opName;
	}

	public static void main(String[] args) throws Exception {
		new Decompiler(args[0]);
	}
}
