package main;

import static data.content.DAXContentType.ECL;
import static engine.opcodes.EclOpCode.ADD;
import static engine.opcodes.EclOpCode.AND;
import static engine.opcodes.EclOpCode.CALL;
import static engine.opcodes.EclOpCode.COMPARE;
import static engine.opcodes.EclOpCode.COMPARE_AND;
import static engine.opcodes.EclOpCode.COPY_MEM;
import static engine.opcodes.EclOpCode.DIVIDE;
import static engine.opcodes.EclOpCode.EXIT;
import static engine.opcodes.EclOpCode.GOSUB;
import static engine.opcodes.EclOpCode.GOTO;
import static engine.opcodes.EclOpCode.IF_EQUALS;
import static engine.opcodes.EclOpCode.IF_GREATER;
import static engine.opcodes.EclOpCode.IF_GREATER_EQUALS;
import static engine.opcodes.EclOpCode.IF_LESS;
import static engine.opcodes.EclOpCode.IF_LESS_EQUALS;
import static engine.opcodes.EclOpCode.IF_NOT_EQUALS;
import static engine.opcodes.EclOpCode.INPUT_YES_NO;
import static engine.opcodes.EclOpCode.MENU_HORIZONTAL;
import static engine.opcodes.EclOpCode.MULTIPLY;
import static engine.opcodes.EclOpCode.NEW_ECL;
import static engine.opcodes.EclOpCode.ON_GOSUB;
import static engine.opcodes.EclOpCode.ON_GOTO;
import static engine.opcodes.EclOpCode.OR;
import static engine.opcodes.EclOpCode.RANDOM;
import static engine.opcodes.EclOpCode.RANDOM0;
import static engine.opcodes.EclOpCode.RETURN;
import static engine.opcodes.EclOpCode.SELECT_ACTION;
import static engine.opcodes.EclOpCode.STOP_MOVE;
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
import data.content.EclProgram;
import engine.EngineResources;
import engine.VirtualMemory;
import engine.opcodes.EclArgument;
import engine.opcodes.EclInstruction;
import engine.opcodes.EclOpCode;

public class Decompiler {
	private static final List<EclOpCode> OP_CODE_STOP = ImmutableList.of(EXIT, STOP_MOVE, GOTO, ON_GOTO, RETURN, NEW_ECL);
	private static final List<EclOpCode> OP_CODE_COMP = ImmutableList.of(COMPARE, COMPARE_AND, AND, OR, INPUT_YES_NO);
	private static final List<EclOpCode> OP_CODE_IF = ImmutableList.of(IF_EQUALS, IF_GREATER, IF_GREATER_EQUALS, IF_LESS, IF_LESS_EQUALS,
		IF_NOT_EQUALS);
	private static final List<EclOpCode> OP_CODE_MATH = ImmutableList.of(WRITE_MEM, WRITE_MEM_BASE_OFF, COPY_MEM, ADD, SUBTRACT, MULTIPLY, DIVIDE,
		AND, OR, RANDOM, RANDOM0);
	private static final List<EclOpCode> OP_CODE_HEX_ARGS = ImmutableList.of(AND, OR);

	private static final Map<Integer, String> KNOWN_ADRESSES = new HashMap<>();
	static {
		KNOWN_ADRESSES.put(0x4CF6, "FOR_COUNT");
		KNOWN_ADRESSES.put(0x7B90, "STRING1");
		KNOWN_ADRESSES.put(0x7F79, "TEMP1");
		KNOWN_ADRESSES.put(0x7F7A, "TEMP2");
		KNOWN_ADRESSES.put(0x7F7B, "TEMP3");
		KNOWN_ADRESSES.put(0x7F7C, "TEMP4");
		KNOWN_ADRESSES.put(VirtualMemory.MEMLOC_COMBAT_RESULT, "COMBAT_RESULT");
		KNOWN_ADRESSES.put(VirtualMemory.MEMLOC_MAP_ORIENTATION, "MAP_DIR");
		KNOWN_ADRESSES.put(VirtualMemory.MEMLOC_MAP_POS_X, "MAP_X");
		KNOWN_ADRESSES.put(VirtualMemory.MEMLOC_MAP_POS_Y, "MAP_Y");
		KNOWN_ADRESSES.put(VirtualMemory.MEMLOC_MAP_SQUARE_INFO, "MAP_SQUARE");
		KNOWN_ADRESSES.put(VirtualMemory.MEMLOC_MAP_WALL_TYPE, "MAP_WALL");
	}
	private int base;
	private int size;
	private int indention = 0;

	private SortedMap<Integer, Boolean> gotoAddressList = new TreeMap<>();

	private EclInstruction compare;
	private boolean wasCompare = false;

	private PrintStream out;

	private int currentId;

	public Decompiler(String gameDir) throws IOException {
		EngineResources res = new EngineResources(gameDir);
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
		EclInstruction preMove = EclInstruction.parseNext(eclCode);
		EclInstruction postMove = EclInstruction.parseNext(eclCode);
		EclInstruction onRest = EclInstruction.parseNext(eclCode);
		EclInstruction onRestInterruption = EclInstruction.parseNext(eclCode);
		EclInstruction onInit = EclInstruction.parseNext(eclCode);

		int address = Math.min(preMove.getArgument(0).valueAsInt(), postMove.getArgument(0).valueAsInt());
		address = Math.min(address, onRest.getArgument(0).valueAsInt());
		address = Math.min(address, onRestInterruption.getArgument(0).valueAsInt());
		address = Math.min(address, onInit.getArgument(0).valueAsInt());

		base = address - 0x14;

		startSection(gameDir, eclCode, onInit, "onInit");
		startSection(gameDir, eclCode, preMove, "preMove");
		startSection(gameDir, eclCode, postMove, "postMove");
		startSection(gameDir, eclCode, onRest, "onRest");
		startSection(gameDir, eclCode, onRestInterruption, "onRestInterrupt");
	}

	private void startSection(String gameDir, ByteBufferWrapper eclCode, EclInstruction inst, String section) throws IOException {
		File outFile = new File(gameDir + "/ECL/ECL." + currentId + "." + section);
		outFile.getParentFile().mkdirs();
		out = new PrintStream(outFile);
		try {
			disassemble(eclCode, inst, section);
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
		eclCode.position(address - base);
		EclInstruction inst;
		do {
			inst = EclInstruction.parseNext(eclCode);
			disassembleInst(eclCode, inst, withOutput);
		} while (!OP_CODE_STOP.contains(inst.getOpCode()));
	}

	private void disassembleInst(ByteBufferWrapper eclCode, EclInstruction inst, boolean withOutput) {
		EclOpCode opCode = inst.getOpCode();

		if (wasCompare && (!OP_CODE_IF.contains(opCode) || OP_CODE_MATH.contains(compare.getOpCode())) && withOutput) {
			output(compare);
		}
		wasCompare = false;

		if (inst.hasDynArgs()) {
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
			wasCompare = true;
		} else if (OP_CODE_IF.contains(opCode)) {
			if (withOutput)
				outputCompare(compare, inst);

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
		out.print(Integer.toHexString(address).toUpperCase() + ":");
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
			out.print(inst.getOpCode().name() + "(");
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
			default:
				break;
		}
	}

	private void outputCompare(EclInstruction compInst, EclInstruction ifInst) {
		outputInstStart(base + compInst.getPosition());
		out.print("if (");
		String operator;
		switch (ifInst.getOpCode()) {
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
				throw new IllegalArgumentException("unkown if statement " + ifInst);
		}
		switch (compInst.getOpCode()) {
			case COMPARE:
				out.print(argR(compInst, 0));
				out.print(" ");
				out.print(operator);
				out.print(" ");
				out.print(argR(compInst, 1));
				break;
			case COMPARE_AND:
				out.print("(");
				out.print(argR(compInst, 0));
				out.print(" == ");
				out.print(argR(compInst, 1));
				out.print(" && ");
				out.print(argR(compInst, 2));
				out.print(" == ");
				out.print(argR(compInst, 3));
				out.print(") ");
				out.print(operator);
				out.print(" TRUE");
				break;
			case AND:
				out.print(argR(compInst, 0));
				out.print(" & ");
				out.print(argR(compInst, 1));
				out.print(" ");
				out.print(operator);
				out.print(" 0");
				break;
			case OR:
				out.print(argR(compInst, 0));
				out.print(" | ");
				out.print(argR(compInst, 1));
				out.print(" ");
				out.print(operator);
				out.print(" 0");
				break;
			case INPUT_YES_NO:
				out.print(compInst.toString());
				out.print(" ");
				out.print(operator);
				out.print(" 0");
				break;
			default:
				throw new IllegalArgumentException("unkown compare statement " + compInst);
		}
		out.println(")");
	}

	private void output(EclInstruction inst, List<EclArgument> dynArgs) {
		outputInstStart(base + inst.getPosition());
		if (inst.getOpCode() == ON_GOTO || inst.getOpCode() == ON_GOSUB) {
			out.print("ON " + argL(inst, 0) + (inst.getOpCode() == ON_GOTO ? " GOTO " : " GOSUB "));
		} else if (inst.getOpCode() == MENU_HORIZONTAL || inst.getOpCode() == SELECT_ACTION) {
			out.print(argL(inst, 0) + " = " + inst.getOpCode().name());
		} else {
			out.print(inst.getOpCode().name() + "(");
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
			System.err.println("Value is not a memory address at " + Integer.toHexString(inst.getPosition() + base).toUpperCase());
		}
		if (!KNOWN_ADRESSES.containsKey(a.valueAsInt())) {
			KNOWN_ADRESSES.put(a.valueAsInt(), "var_" + currentId + "_" + Integer.toHexString(a.valueAsInt()).toUpperCase());
		}
		return KNOWN_ADRESSES.get(a.valueAsInt());
	}

	private String argR(EclInstruction inst, int argNr) {
		EclArgument a = inst.getArgument(argNr);
		if (a.isMemAddress()) {
			if (KNOWN_ADRESSES.containsKey(a.valueAsInt())) {
				return KNOWN_ADRESSES.get(a.valueAsInt());
			} else if (a.valueAsInt() >= base && a.valueAsInt() <= (base + size)) {
				return "CODE_" + Integer.toHexString(a.valueAsInt()).toUpperCase();
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

	public static void main(String[] args) throws IOException {
		new Decompiler(args[0]);
	}
}
