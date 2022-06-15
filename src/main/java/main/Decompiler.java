package main;

import static data.ContentType.ECL;
import static engine.EngineAddress.SAVED_TEMP_START;
import static engine.EngineAddress.SEL_PC_START;
import static engine.EngineAddress.TEMP_START;
import static engine.script.EclOpCode.ADD;
import static engine.script.EclOpCode.AND;
import static engine.script.EclOpCode.CALL;
import static engine.script.EclOpCode.COMPARE;
import static engine.script.EclOpCode.COMPARE_AND;
import static engine.script.EclOpCode.COPY_MEM;
import static engine.script.EclOpCode.DIVIDE;
import static engine.script.EclOpCode.ENCOUNTER_MENU;
import static engine.script.EclOpCode.EXIT;
import static engine.script.EclOpCode.GOSUB;
import static engine.script.EclOpCode.GOTO;
import static engine.script.EclOpCode.IF_EQUALS;
import static engine.script.EclOpCode.IF_GREATER;
import static engine.script.EclOpCode.IF_GREATER_EQUALS;
import static engine.script.EclOpCode.IF_LESS;
import static engine.script.EclOpCode.IF_LESS_EQUALS;
import static engine.script.EclOpCode.IF_NOT_EQUALS;
import static engine.script.EclOpCode.INPUT_YES_NO_22;
import static engine.script.EclOpCode.INPUT_YES_NO_2C;
import static engine.script.EclOpCode.MENU_HORIZONTAL;
import static engine.script.EclOpCode.MULTIPLY;
import static engine.script.EclOpCode.NEW_ECL;
import static engine.script.EclOpCode.ON_GOSUB;
import static engine.script.EclOpCode.ON_GOTO;
import static engine.script.EclOpCode.OR;
import static engine.script.EclOpCode.PARLAY;
import static engine.script.EclOpCode.RANDOM;
import static engine.script.EclOpCode.RANDOM0;
import static engine.script.EclOpCode.RETURN;
import static engine.script.EclOpCode.SELECT_ACTION;
import static engine.script.EclOpCode.STOP_MOVE_23;
import static engine.script.EclOpCode.STOP_MOVE_42;
import static engine.script.EclOpCode.SUBTRACT;
import static engine.script.EclOpCode.WRITE_MEM;
import static engine.script.EclOpCode.WRITE_MEM_BASE_OFF;
import static io.vavr.API.Map;
import static io.vavr.API.Seq;
import static io.vavr.API.SortedMap;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.function.Function;

import io.vavr.collection.Array;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.collection.Set;
import io.vavr.collection.SortedMap;
import io.vavr.control.Try;

import common.ByteBufferWrapper;
import common.FileMap;
import data.ResourceLoader;
import data.script.EclProgram;
import engine.EngineConfiguration;
import engine.VirtualMemory;
import engine.script.EclArgument;
import engine.script.EclInstruction;
import engine.script.EclOpCode;

public class Decompiler {
	private static final Seq<EclOpCode> OP_CODE_STOP = Seq(EXIT, STOP_MOVE_23, STOP_MOVE_42, GOTO, ON_GOTO, RETURN,
		NEW_ECL);
	private static final Seq<EclOpCode> OP_CODE_COMP = Seq(COMPARE, COMPARE_AND, AND, OR, INPUT_YES_NO_22,
		INPUT_YES_NO_2C);
	private static final Seq<EclOpCode> OP_CODE_IF = Seq(IF_EQUALS, IF_GREATER, IF_GREATER_EQUALS, IF_LESS,
		IF_LESS_EQUALS, IF_NOT_EQUALS);
	private static final Seq<EclOpCode> OP_CODE_MATH = Seq(WRITE_MEM, WRITE_MEM_BASE_OFF, COPY_MEM, ADD, SUBTRACT,
		MULTIPLY, DIVIDE, AND, OR, RANDOM, RANDOM0, ENCOUNTER_MENU, PARLAY);
	private static final Seq<EclOpCode> OP_CODE_HEX_ARGS = Seq(AND, OR);

	private int base;
	private int size;
	private int indention = 0;

	private Map<Integer, String> knownAddresses;
	private SortedMap<Integer, Boolean> gotoAddressList;

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

		knownAddresses = Map();
		knownAddresses = knownAddresses.merge(cfg.getEngineAdresses());
		knownAddresses = knownAddresses.put(0x4BFF, "PICS_ARE_DRAWN");
		knownAddresses = knownAddresses.put(0x4C2F, "CURRENT_PIC");
		knownAddresses = knownAddresses.put(0x7B90, "STRING1");
		final Seq<String> celestials = Seq("MERKUR", "VENUS", "EARTH", "MARS", "CERES", "VESTA", "FORTUNA", "PALLAS",
			"PSYCHE", "JUNO", "HYGEIA", "AURORA", "THULE");
		for (int i = 0; i < celestials.size(); i++) {
			knownAddresses = knownAddresses.put(VirtualMemory.MEMLOC_CELESTIAL_POS_START + (2 * i),
				celestials.get(i) + "_X");
			knownAddresses = knownAddresses.put(VirtualMemory.MEMLOC_CELESTIAL_POS_START + (2 * i) + 1,
				celestials.get(i) + "_Y");
		}

		int savedTempStart = cfg.getEngineAddress(SAVED_TEMP_START);
		for (int i = savedTempStart; i < savedTempStart + 0x20; i++) {
			knownAddresses = knownAddresses.put(i, "SAVED_TEMP_" + hex(i));
		}
		int selPCStart = cfg.getEngineAddress(SEL_PC_START);
		knownAddresses = knownAddresses.put(selPCStart, "SEL_PC_NAME");
		for (int i = selPCStart; i < selPCStart + 0x1FF; i++) {
			knownAddresses = knownAddresses.computeIfAbsent(i, key -> "SEL_PC_" + hex(key))._2;
		}
		int tempStart = cfg.getEngineAddress(TEMP_START);
		for (int i = 0; i < 0xA; i++) {
			knownAddresses = knownAddresses.put(tempStart + i, "TEMP" + String.format("%01X", i + 1));
		}

		final Set<Integer> ids = res.idsFor(ECL);
		for (Integer id : ids) {
			currentId = id;
			res.find(id, EclProgram.class, ECL).ifPresentOrElse(t -> {
				t.onFailure(throwable -> System.err.println("failure reading script " + id))
					.onSuccess(e -> System.out.println(id))
					.flatMap(eclCode -> Try.run(() -> start(gameDir, eclCode)));
			}, () -> System.err.println("failure finding script " + id));
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

	private void startSection(String gameDir, ByteBufferWrapper eclCode, EclInstruction inst, String section)
		throws IOException {

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
		gotoAddressList = SortedMap();

		// To collect Goto and Gosub adresses
		disassemble(eclCode, inst.getArgument(0).valueAsInt(), false);
		int sizeBefore = 0, sizeAfter = gotoAddressList.size();
		while (sizeAfter > sizeBefore) {
			sizeBefore = gotoAddressList.size();
			gotoAddressList.keySet().forEach(address -> {
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
			Integer a = gotoAddressList.find(e -> Boolean.FALSE.equals(e._2)).get()._1;
			out.println();
			indention += 1;
			disassemble(eclCode, a, true);
			indention -= 1;
			gotoAddressList = gotoAddressList.put(a, Boolean.TRUE);
		}
	}

	private void addAdjacentGotos(ByteBufferWrapper eclCode) {
		int address = base + eclCode.position();
		while (gotoAddressList.containsKey(address)) {
			disassemble(eclCode, address, true);
			gotoAddressList = gotoAddressList.put(address, Boolean.TRUE);
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
			final Seq<EclArgument> dynArgs = Array.ofAll(inst.getDynArgs());

			if (opCode == ON_GOTO || opCode == ON_GOSUB) {
				gotoAddressList = gotoAddressList
					.merge(dynArgs.filter(a -> !gotoAddressList.containsKey(a.valueAsInt()))
						.map(EclArgument::valueAsInt)
						.sorted()
						.toSet()
						.toMap(Function.identity(), a -> Boolean.FALSE));
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
			if (opCode == GOTO || opCode == GOSUB) {
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
		if (gotoAddressList.containsKey(address) && Boolean.FALSE.equals(gotoAddressList.get(address).get())) {
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
				String argsEM = String.join(", ", argR(inst, 0), argR(inst, 1), argR(inst, 2), argR(inst, 4),
					argR(inst, 5), argR(inst, 6), argR(inst, 7), argR(inst, 8), argR(inst, 9), argR(inst, 10),
					argR(inst, 11), argR(inst, 12), argR(inst, 13));
				out.println(argL(inst, 3) + " = ENCOUNTER_MENU(" + argsEM + ")");
				break;
			case PARLAY:
				String argsP = String.join(", ", argR(inst, 0), argR(inst, 1), argR(inst, 2), argR(inst, 3),
					argR(inst, 4));
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

	private void output(EclInstruction inst, Seq<EclArgument> dynArgs) {
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
		out.println(dynArgs.map(EclArgument::toString).mkString("(", ", ", ")"));
	}

	private String argL(EclInstruction inst, int argNr) {
		EclArgument a = inst.getArgument(argNr);
		if (!a.isMemAddress()) {
			System.err.println("Value is not a memory address at " + hex(inst.getPosition() + base));
		}
		if (!knownAddresses.containsKey(a.valueAsInt())) {
			if (a.isStringValue()) {
				knownAddresses = knownAddresses.put(a.valueAsInt(), "string_" + currentId + "_" + hex(a.valueAsInt()));
			} else if (a.isShortValue()) {
				knownAddresses = knownAddresses.put(a.valueAsInt(), "short_" + currentId + "_" + hex(a.valueAsInt()));
			} else {
				knownAddresses = knownAddresses.put(a.valueAsInt(), "byte_" + currentId + "_" + hex(a.valueAsInt()));
			}
		}
		return knownAddresses.get(a.valueAsInt()).get();
	}

	private String argR(EclInstruction inst, int argNr) {
		EclArgument a = inst.getArgument(argNr);
		if (a.isMemAddress()) {
			if (knownAddresses.containsKey(a.valueAsInt())) {
				return knownAddresses.get(a.valueAsInt()).get();
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
		for (int i = 0; i < args.length; i++) {
			new Decompiler(args[i]);
		}
	}
}
