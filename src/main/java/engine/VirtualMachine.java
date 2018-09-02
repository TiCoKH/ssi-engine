package engine;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Deque;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import data.content.EclProgram;
import engine.opcodes.EclArgument;
import engine.opcodes.EclInstruction;
import engine.opcodes.EclOpCode;

public class VirtualMachine {

	private final Map<EclOpCode, Consumer<EclArgument[]>> IMPL = new EnumMap<>(EclOpCode.class);

	private VirtualMemory mem;
	private Deque<Integer> gosubStack;
	private int compareResult;

	private boolean stopped;

	private int eclCodeBaseAddress;
	private ByteBuffer eclCode;
	private EclInstruction onEvent1;
	private EclInstruction onEnter;
	private EclInstruction onRest;
	private EclInstruction onRestInterruption;
	private EclInstruction onInit;

	public VirtualMachine() {
		mem = new VirtualMemory();
		gosubStack = new ConcurrentLinkedDeque<>();
		compareResult = 0;
		stopped = true;
		initImpl();
	}

	public void newEcl(EclProgram ecl) {
		eclCode = ecl.getCode().duplicate();
		eclCode.order(ByteOrder.LITTLE_ENDIAN);
		onEvent1 = EclInstruction.parseNext(eclCode);
		onEnter = EclInstruction.parseNext(eclCode);
		onRest = EclInstruction.parseNext(eclCode);
		onRestInterruption = EclInstruction.parseNext(eclCode);
		onInit = EclInstruction.parseNext(eclCode);
		initCodeBase();
	}

	private void initCodeBase() {
		// An Ecl Block starts with 5 GOTO Statements to different parts of the Code.
		// One of them will jump to XX14, the first instruction after these.
		// This is the lowest Address. Use it to determine CodeBase as XX00.
		int address = Math.min(onEvent1.getArgument(0).valueAsInt(), onEnter.getArgument(0).valueAsInt());
		address = Math.min(address, onRest.getArgument(0).valueAsInt());
		address = Math.min(address, onRestInterruption.getArgument(0).valueAsInt());
		address = Math.min(address, onInit.getArgument(0).valueAsInt());

		eclCodeBaseAddress = address - 0x14;
	}

	public void startAddress1() {
		stopped = false;
		exec(onEvent1, true);
		runVM();
	}

	public void startSearchLocation() {
		stopped = false;
		exec(onEnter, true);
		runVM();
	}

	public void startPreCampCheck() {
		stopped = false;
		exec(onRest, true);
		runVM();
	}

	public void startCampInterrupted() {
		stopped = false;
		exec(onRestInterruption, true);
		runVM();
	}

	public void startInitial() {
		stopped = false;
		exec(onInit, true);
		runVM();
	}

	private void runVM() {
		while (!stopped) {
			exec(EclInstruction.parseNext(eclCode), true);
		}
	}

	private void stopVM() {
		stopped = true;
	}

	private void exec(EclInstruction in, boolean execute) {
		System.out.println(Integer.toHexString(eclCodeBaseAddress + in.getPosition()).toUpperCase() + ":"
				+ in.toString() + (execute ? "" : " (SKIPPED)"));
		if (execute) {
			IMPL.get(in.getOpCode()).accept(in.getArguments());
		}
	}

	private void goTo(EclArgument a) {
		if (a.isMemAddress()) {
			eclCode.position(a.valueAsInt() - eclCodeBaseAddress);
		} else {
			System.err.println("GOTO " + a.toString());
		}
	}

	private int intValue(EclArgument a) {
		if (!a.isNumberValue()) {
			return 0;
		}
		return a.isMemAddress() ? mem.readMemInt(a) : a.valueAsInt();
	}

	private String stringValue(EclArgument a) {
		if (!a.isStringValue()) {
			return "";
		}
		return a.isMemAddress() ? mem.readMemString(a) : a.valueAsString();
	}

	private void initImpl() {
		IMPL.clear();
		IMPL.put(EclOpCode.EXIT, args -> {
			stopVM();
		});
		IMPL.put(EclOpCode.GOTO, args -> {
			goTo(args[0]);
		});
		IMPL.put(EclOpCode.GOSUB, args -> {
			gosubStack.push(eclCode.position());
			goTo(args[0]);
		});
		IMPL.put(EclOpCode.COMPARE, args -> {
			if (args[0].isStringValue() && args[1].isStringValue()) {
				compareResult = stringValue(args[1]).compareTo(stringValue(args[0]));
			} else if (args[0].isNumberValue() && args[1].isNumberValue()) {
				compareResult = intValue(args[1]) - intValue(args[0]);
			}
		});
		IMPL.put(EclOpCode.ADD, args -> {
			mem.writeMemInt(args[2], intValue(args[0]) + intValue(args[1]));
		});
		IMPL.put(EclOpCode.SUBTRACT, args -> {
			mem.writeMemInt(args[2], intValue(args[1]) - intValue(args[0]));
		});
		IMPL.put(EclOpCode.DIVIDE, args -> {
			mem.writeMemInt(args[2], intValue(args[0]) / intValue(args[1]));
		});
		IMPL.put(EclOpCode.MULTIPLY, args -> {
			mem.writeMemInt(args[2], intValue(args[0]) * intValue(args[1]));
		});
		IMPL.put(EclOpCode.RANDOM, args -> {

		});
		IMPL.put(EclOpCode.WRITE_MEM, args -> {
			mem.writeMemInt(args[1], intValue(args[0]));
		});
		IMPL.put(EclOpCode.LOAD_CHAR, args -> {

		});
		IMPL.put(EclOpCode.LOAD_MON, args -> {

		});
		IMPL.put(EclOpCode.SETUP_MON, args -> {

		});
		IMPL.put(EclOpCode.APPROACH, args -> {

		});
		IMPL.put(EclOpCode.PICTURE, args -> {

		});
		IMPL.put(EclOpCode.INPUT_NUMBER, args -> {

		});
		IMPL.put(EclOpCode.INPUT_STRING, args -> {

		});
		IMPL.put(EclOpCode.PRINT, args -> {

		});
		IMPL.put(EclOpCode.PRINT_CLEAR, args -> {

		});
		IMPL.put(EclOpCode.RETURN, args -> {
			eclCode.position(gosubStack.pop());
		});
		IMPL.put(EclOpCode.COMPARE_AND, args -> {
			compareResult = (intValue(args[0]) == intValue(args[1]) && intValue(args[2]) == intValue(args[3])) ? 0 : 1;
		});
		IMPL.put(EclOpCode.MENU_VERTICAL, args -> {

		});
		IMPL.put(EclOpCode.IF_EQUALS, args -> {
			EclInstruction inst = EclInstruction.parseNext(eclCode);
			exec(inst, compareResult == 0);
		});
		IMPL.put(EclOpCode.IF_NOT_EQUALS, args -> {
			EclInstruction inst = EclInstruction.parseNext(eclCode);
			exec(inst, compareResult != 0);
		});
		IMPL.put(EclOpCode.IF_LESS, args -> {
			EclInstruction inst = EclInstruction.parseNext(eclCode);
			exec(inst, compareResult < 0);
		});
		IMPL.put(EclOpCode.IF_GREATER, args -> {
			EclInstruction inst = EclInstruction.parseNext(eclCode);
			exec(inst, compareResult > 0);
		});
		IMPL.put(EclOpCode.IF_LESS_EQUALS, args -> {
			EclInstruction inst = EclInstruction.parseNext(eclCode);
			exec(inst, compareResult <= 0);
		});
		IMPL.put(EclOpCode.IF_GREATER_EQUALS, args -> {
			EclInstruction inst = EclInstruction.parseNext(eclCode);
			exec(inst, compareResult >= 0);
		});
		IMPL.put(EclOpCode.CLEAR_MON, args -> {

		});
		IMPL.put(EclOpCode.PARTY_STRENGTH, args -> {

		});
		IMPL.put(EclOpCode.PARTY_CHECK, args -> {

		});
		IMPL.put(EclOpCode.UNKNOWN_1F, args -> {

		});
		IMPL.put(EclOpCode.NEW_ECL, args -> {

		});
		IMPL.put(EclOpCode.LOAD_FILES, args -> {

		});
		IMPL.put(EclOpCode.PARTY_SURPRISE, args -> {

		});
		IMPL.put(EclOpCode.SURPRISE, args -> {

		});
		IMPL.put(EclOpCode.COMBAT, args -> {

		});
		IMPL.put(EclOpCode.ON_GOTO, args -> {
			EclArgument[] dynArgs = new EclArgument[intValue(args[1])];
			for (int i = 0; i < dynArgs.length; i++) {
				dynArgs[i] = EclArgument.parseNext(eclCode);
			}
			System.out.println(String.join(", ",
					Arrays.asList(dynArgs).stream().map(EclArgument::toString).collect(Collectors.toList())));
			if (intValue(args[0]) >= intValue(args[1]) || intValue(args[0]) < 0) {
				System.err.println("ON GOTO value=" + intValue(args[0]));
				return;
			}
			goTo(dynArgs[intValue(args[0])]);
		});
		IMPL.put(EclOpCode.ON_GOSUB, args -> {
			EclArgument[] dynArgs = new EclArgument[intValue(args[1])];
			for (int i = 0; i < dynArgs.length; i++) {
				dynArgs[i] = EclArgument.parseNext(eclCode);
			}
			System.out.println(String.join(", ",
					Arrays.asList(dynArgs).stream().map(EclArgument::toString).collect(Collectors.toList())));
			if (intValue(args[0]) >= intValue(args[1]) || intValue(args[0]) < 0) {
				System.err.println("ON GOSUB value=" + intValue(args[0]));
				return;
			}
			gosubStack.push(eclCode.position());
			goTo(dynArgs[intValue(args[0])]);
		});
		IMPL.put(EclOpCode.TREASURE, args -> {

		});
		IMPL.put(EclOpCode.ROB, args -> {

		});
		IMPL.put(EclOpCode.MENU_ENCOUNTER, args -> {

		});
		IMPL.put(EclOpCode.TABLE_GET, args -> {

		});
		IMPL.put(EclOpCode.MENU_HORIZONTAL, args -> {
			EclArgument[] dynArgs = new EclArgument[args[1].valueAsInt()];
			for (int i = 0; i < args[1].valueAsInt(); i++) {
				dynArgs[i] = EclArgument.parseNext(eclCode);
			}
			System.out.println(String.join(", ",
					Arrays.asList(dynArgs).stream().map(EclArgument::toString).collect(Collectors.toList())));
		});
		IMPL.put(EclOpCode.PARLAY, args -> {

		});
		IMPL.put(EclOpCode.CALL, args -> {

		});
		IMPL.put(EclOpCode.DAMAGE, args -> {

		});
		IMPL.put(EclOpCode.AND, args -> {

		});
		IMPL.put(EclOpCode.OR, args -> {

		});
		IMPL.put(EclOpCode.SPRITE_OFF, args -> {
			EclArgument[] dynArgs = new EclArgument[args[1].valueAsInt()];
			for (int i = 0; i < args[1].valueAsInt(); i++) {
				dynArgs[i] = EclArgument.parseNext(eclCode);
			}
			System.out.println(String.join(", ",
					Arrays.asList(dynArgs).stream().map(EclArgument::toString).collect(Collectors.toList())));
		});
		IMPL.put(EclOpCode.FIND_ITEM, args -> {

		});
		IMPL.put(EclOpCode.PRINT_RETURN, args -> {

		});
		IMPL.put(EclOpCode.CLOCK, args -> {

		});
		IMPL.put(EclOpCode.TABLE_SET, args -> {

		});
		IMPL.put(EclOpCode.ADD_NPC, args -> {

		});
		IMPL.put(EclOpCode.LOAD_PIECES, args -> {

		});
		IMPL.put(EclOpCode.PROGRAM, args -> {

		});
		IMPL.put(EclOpCode.WHO, args -> {

		});
		IMPL.put(EclOpCode.DELAY, args -> {

		});
		IMPL.put(EclOpCode.SPELL, args -> {

		});
		IMPL.put(EclOpCode.PROTECTION, args -> {

		});
		IMPL.put(EclOpCode.CLEAR_BOX, args -> {

		});
		IMPL.put(EclOpCode.DUMP, args -> {

		});
		IMPL.put(EclOpCode.FIND_SPECIAL, args -> {

		});
		IMPL.put(EclOpCode.DESTROY_ITEM, args -> {

		});
		IMPL.put(EclOpCode.UNKNOWN_42, args -> {

		});
		IMPL.put(EclOpCode.UNKNOWN_43, args -> {

		});
		IMPL.put(EclOpCode.UNKNOWN_46, args -> {

		});
		IMPL.put(EclOpCode.UNKNOWN_47, args -> {

		});
		IMPL.put(EclOpCode.UNKNOWN_4C, args -> {

		});
	}
}
