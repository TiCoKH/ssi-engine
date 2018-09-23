package main;

import static data.content.DAXContentType.ECL;
import static engine.opcodes.EclOpCode.AND;
import static engine.opcodes.EclOpCode.COMPARE;
import static engine.opcodes.EclOpCode.COMPARE_AND;
import static engine.opcodes.EclOpCode.EXIT;
import static engine.opcodes.EclOpCode.GOSUB;
import static engine.opcodes.EclOpCode.GOTO;
import static engine.opcodes.EclOpCode.IF_EQUALS;
import static engine.opcodes.EclOpCode.IF_GREATER;
import static engine.opcodes.EclOpCode.IF_GREATER_EQUALS;
import static engine.opcodes.EclOpCode.IF_LESS;
import static engine.opcodes.EclOpCode.IF_LESS_EQUALS;
import static engine.opcodes.EclOpCode.IF_NOT_EQUALS;
import static engine.opcodes.EclOpCode.ON_GOSUB;
import static engine.opcodes.EclOpCode.ON_GOTO;
import static engine.opcodes.EclOpCode.OR;
import static engine.opcodes.EclOpCode.RETURN;
import static engine.opcodes.EclOpCode.STOP_MOVE;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

import data.content.EclProgram;
import engine.EngineResources;
import engine.opcodes.EclArgument;
import engine.opcodes.EclInstruction;
import engine.opcodes.EclOpCode;

public class Decompiler {
	private static final List<EclOpCode> OP_CODE_STOP = ImmutableList.of(EXIT, STOP_MOVE, GOTO, ON_GOTO, RETURN);
	private static final List<EclOpCode> OP_CODE_COMP = ImmutableList.of(COMPARE, COMPARE_AND, AND, OR);
	private static final List<EclOpCode> OP_CODE_IF = ImmutableList.of(IF_EQUALS, IF_GREATER, IF_GREATER_EQUALS, IF_LESS, IF_LESS_EQUALS,
		IF_NOT_EQUALS);

	private int base;
	private int indention = 0;

	private SortedMap<Integer, Boolean> gotoAddressList = new TreeMap<>();

	private EclInstruction compare;
	private boolean wasCompare = false;

	private PrintStream out;

	public Decompiler(String gameDir) throws IOException {
		EngineResources res = new EngineResources(gameDir);
		Set<Integer> ids = res.idsFor(ECL);
		for (Integer id : ids) {
			EclProgram eclCode = res.find(id, EclProgram.class, ECL);
			System.out.println(id);
			start(gameDir, id, eclCode);
		}
	}

	private void start(String gameDir, int id, EclProgram ecl) throws IOException {
		ByteBuffer eclCode = ecl.getCode();
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

		startSection(gameDir, id, eclCode, onInit, "onInit");
		startSection(gameDir, id, eclCode, preMove, "preMove");
		startSection(gameDir, id, eclCode, postMove, "postMove");
		startSection(gameDir, id, eclCode, onRest, "onRest");
		startSection(gameDir, id, eclCode, onRestInterruption, "onRestInterrupt");
	}

	private void startSection(String gameDir, int id, ByteBuffer eclCode, EclInstruction inst, String section) throws IOException {
		File outFile = new File(gameDir + "/ECL/ECL." + id + "." + section);
		outFile.getParentFile().mkdirs();
		out = new PrintStream(outFile);
		try {
			disassemble(eclCode, inst, section);
		} finally {
			out.close();
		}
	}

	private void disassemble(ByteBuffer eclCode, EclInstruction inst, String name) {
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
			disassemble(eclCode, a, true);
			gotoAddressList.put(a, Boolean.TRUE);
		}
	}

	private void addAdjacentGotos(ByteBuffer eclCode) {
		int address = base + eclCode.position();
		while (gotoAddressList.containsKey(address)) {
			disassemble(eclCode, address, true);
			gotoAddressList.put(address, Boolean.TRUE);
			address = base + eclCode.position();
		}
	}

	private void disassemble(ByteBuffer eclCode, int address, boolean withOutput) {
		eclCode.position(address - base);
		EclInstruction inst;
		do {
			inst = EclInstruction.parseNext(eclCode);
			disassembleInst(eclCode, inst, withOutput);
		} while (!OP_CODE_STOP.contains(inst.getOpCode()));
	}

	private void disassembleInst(ByteBuffer eclCode, EclInstruction inst, boolean withOutput) {
		EclOpCode opCode = inst.getOpCode();

		if (wasCompare && !OP_CODE_IF.contains(opCode) && withOutput) {
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
		out.println(inst);
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
				out.print(compInst.getArgument(0));
				out.print(" ");
				out.print(operator);
				out.print(" ");
				out.print(compInst.getArgument(1));
				break;
			case COMPARE_AND:
				out.print(compInst.getArgument(0));
				out.print(" == ");
				out.print(compInst.getArgument(1));
				out.print(" && ");
				out.print(compInst.getArgument(2));
				out.print(" == ");
				out.print(compInst.getArgument(3));
				break;
			case AND:
				out.print(compInst.getArgument(0));
				out.print(" & ");
				out.print(compInst.getArgument(1));
				out.print(" ");
				out.print(operator);
				out.print(" ");
				out.print(compInst.getArgument(2));
				break;
			case OR:
				out.print(compInst.getArgument(0));
				out.print(" | ");
				out.print(compInst.getArgument(1));
				out.print(" ");
				out.print(operator);
				out.print(" ");
				out.print(compInst.getArgument(2));
				break;
			default:
				throw new IllegalArgumentException("unkown compare statement " + compInst);
		}
		out.println(")");
	}

	private void output(EclInstruction inst, List<EclArgument> dynArgs) {
		outputInstStart(base + inst.getPosition());
		if (inst.getOpCode() == ON_GOTO || inst.getOpCode() == ON_GOSUB) {
			out.print("ON " + inst.getArgument(0) + (inst.getOpCode() == ON_GOTO ? " GOTO" : " GOSUB"));
		} else {
			out.print(inst);
		}
		out.println(" (" + String.join(", ", dynArgs.stream().map(EclArgument::toString).collect(Collectors.toList())) + ")");
	}

	public static void main(String[] args) throws IOException {
		new Decompiler(args[0]);
	}
}
