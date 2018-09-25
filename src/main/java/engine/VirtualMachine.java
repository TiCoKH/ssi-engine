package engine;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Deque;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import data.content.EclProgram;
import engine.EngineCallback.InputType;
import engine.opcodes.EclArgument;
import engine.opcodes.EclInstruction;
import engine.opcodes.EclOpCode;
import engine.opcodes.EclString;

public class VirtualMachine {
	private static final EclArgument SELECTED_PLAYER_NAME = new EclArgument(1, 0x7C00);
	private static final EclArgument SELECTED_PLAYER_STATUS = new EclArgument(1, 0x7D00);

	private final Map<EclOpCode, Consumer<EclInstruction>> IMPL = new EnumMap<>(EclOpCode.class);

	private EngineCallback engine;

	private VirtualMemory mem;
	private Deque<Integer> gosubStack;
	private int compareResult;
	private Random rnd;

	private boolean stopped;

	private int eclCodeBaseAddress;
	private ByteBuffer eclCode;
	private EclInstruction onEvent1; // Gets called on entering or exiting a NEW_ECL
	private EclInstruction onEnter;
	private EclInstruction onRest;
	private EclInstruction onRestInterruption;
	private EclInstruction onInit;

	public VirtualMachine(EngineCallback engine) {
		this.engine = engine;

		mem = new VirtualMemory();
		gosubStack = new ConcurrentLinkedDeque<>();
		compareResult = 0;
		rnd = new Random();
		stopped = true;
		initImpl();
	}

	VirtualMemory getMem() {
		return mem;
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
		startEvent(onEvent1);
	}

	public void startSearchLocation() {
		startEvent(onEnter);
	}

	public void startPreCampCheck() {
		startEvent(onRest);
	}

	public void startCampInterrupted() {
		startEvent(onRestInterruption);
	}

	public void startInitial() {
		startEvent(onInit);
	}

	private void startEvent(EclInstruction eventInst) {
		stopped = false;
		engine.setInputHandler(InputType.NONE, null, null);
		exec(eventInst, true);
		runVM();
		engine.setInputHandler(InputType.MOVEMENT, null, InputAction.STANDARD_ACTIONS);
	}

	private void runVM() {
		while (!stopped) {
			exec(EclInstruction.parseNext(eclCode), true);
		}
	}

	public void stopVM() {
		stopped = true;
	}

	private void exec(EclInstruction in, boolean execute) {
		System.out
			.println(Integer.toHexString(eclCodeBaseAddress + in.getPosition()).toUpperCase() + ":" + in.toString() + (execute ? "" : " (SKIPPED)"));
		if (execute) {
			IMPL.get(in.getOpCode()).accept(in);
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

	private EclString stringValue(EclArgument a) {
		if (!a.isStringValue()) {
			return null;
		}
		return a.isMemAddress() ? mem.readMemString(a) : a.valueAsString();
	}

	private void initImpl() {
		IMPL.clear();
		IMPL.put(EclOpCode.EXIT, inst -> {
			stopVM();
		});
		IMPL.put(EclOpCode.GOTO, inst -> {
			goTo(inst.getArgument(0));
		});
		IMPL.put(EclOpCode.GOSUB, inst -> {
			gosubStack.push(eclCode.position());
			goTo(inst.getArgument(0));
		});
		IMPL.put(EclOpCode.COMPARE, inst -> {
			if (inst.getArgument(0).isStringValue() && inst.getArgument(1).isStringValue()) {
				compareResult = stringValue(inst.getArgument(0)).toString().compareTo(stringValue(inst.getArgument(1)).toString());
			} else if (inst.getArgument(0).isNumberValue() && inst.getArgument(1).isNumberValue()) {
				compareResult = intValue(inst.getArgument(0)) - intValue(inst.getArgument(1));
			}
		});
		IMPL.put(EclOpCode.ADD, inst -> {
			mem.writeMemInt(inst.getArgument(2), intValue(inst.getArgument(0)) + intValue(inst.getArgument(1)));
		});
		IMPL.put(EclOpCode.SUBTRACT, inst -> {
			mem.writeMemInt(inst.getArgument(2), intValue(inst.getArgument(1)) - intValue(inst.getArgument(0)));
		});
		IMPL.put(EclOpCode.DIVIDE, inst -> {
			mem.writeMemInt(inst.getArgument(2), intValue(inst.getArgument(0)) / intValue(inst.getArgument(1)));
		});
		IMPL.put(EclOpCode.MULTIPLY, inst -> {
			mem.writeMemInt(inst.getArgument(2), intValue(inst.getArgument(0)) * intValue(inst.getArgument(1)));
		});
		IMPL.put(EclOpCode.RANDOM, inst -> {
			mem.writeMemInt(inst.getArgument(1), rnd.nextInt(intValue(inst.getArgument(0)) + 1));
		});
		IMPL.put(EclOpCode.WRITE_MEM, inst -> {
			if (inst.getArgument(0).isNumberValue()) {
				mem.writeMemInt(inst.getArgument(1), intValue(inst.getArgument(0)));
			}
			if (inst.getArgument(0).isStringValue()) {
				mem.writeMemString(inst.getArgument(1), stringValue(inst.getArgument(0)));
			}
		});
		IMPL.put(EclOpCode.LOAD_CHAR, inst -> {

		});
		IMPL.put(EclOpCode.LOAD_MON, inst -> {

		});
		IMPL.put(EclOpCode.SETUP_MON, inst -> {

		});
		IMPL.put(EclOpCode.APPROACH, inst -> {

		});
		IMPL.put(EclOpCode.PICTURE, inst -> {
			engine.showPicture(intValue(inst.getArgument(0)));
		});
		IMPL.put(EclOpCode.INPUT_NUMBER, inst -> {

		});
		IMPL.put(EclOpCode.INPUT_STRING, inst -> {

		});
		IMPL.put(EclOpCode.PRINT, inst -> {
			EclArgument a0 = inst.getArgument(0);
			if (a0.isStringValue())
				engine.addText(stringValue(a0), false);
			else
				engine.addText(new EclString(Integer.toString(intValue(a0))), false);
		});
		IMPL.put(EclOpCode.PRINT_CLEAR, inst -> {
			EclArgument a0 = inst.getArgument(0);
			if (a0.isStringValue())
				engine.addText(stringValue(a0), true);
			else
				engine.addText(new EclString(Integer.toString(intValue(a0))), true);
		});
		IMPL.put(EclOpCode.RETURN, inst -> {
			eclCode.position(gosubStack.pop());
		});
		IMPL.put(EclOpCode.COMPARE_AND, inst -> {
			boolean r1 = intValue(inst.getArgument(0)) == intValue(inst.getArgument(1));
			boolean r2 = intValue(inst.getArgument(2)) == intValue(inst.getArgument(3));
			compareResult = r1 && r2 ? 0 : 1;
		});
		IMPL.put(EclOpCode.MENU_VERTICAL, inst -> {

		});
		IMPL.put(EclOpCode.IF_EQUALS, inst -> {
			EclInstruction next = EclInstruction.parseNext(eclCode);
			exec(next, compareResult == 0);
		});
		IMPL.put(EclOpCode.IF_NOT_EQUALS, inst -> {
			EclInstruction next = EclInstruction.parseNext(eclCode);
			exec(next, compareResult != 0);
		});
		IMPL.put(EclOpCode.IF_LESS, inst -> {
			EclInstruction next = EclInstruction.parseNext(eclCode);
			exec(next, compareResult < 0);
		});
		IMPL.put(EclOpCode.IF_GREATER, inst -> {
			EclInstruction next = EclInstruction.parseNext(eclCode);
			exec(next, compareResult > 0);
		});
		IMPL.put(EclOpCode.IF_LESS_EQUALS, inst -> {
			EclInstruction next = EclInstruction.parseNext(eclCode);
			exec(next, compareResult <= 0);
		});
		IMPL.put(EclOpCode.IF_GREATER_EQUALS, inst -> {
			EclInstruction next = EclInstruction.parseNext(eclCode);
			exec(next, compareResult >= 0);
		});
		IMPL.put(EclOpCode.CLEAR_MON, inst -> {

		});
		IMPL.put(EclOpCode.PARTY_STRENGTH, inst -> {

		});
		IMPL.put(EclOpCode.PARTY_CHECK, inst -> {

		});
		IMPL.put(EclOpCode.UNKNOWN_1F, inst -> {

		});
		IMPL.put(EclOpCode.NEW_ECL, inst -> {
			stopVM();
			engine.loadEcl(intValue(inst.getArgument(0)));
		});
		IMPL.put(EclOpCode.LOAD_AREA_MAP, inst -> {
			engine.loadArea(intValue(inst.getArgument(0)), intValue(inst.getArgument(1)), intValue(inst.getArgument(2)));
		});
		IMPL.put(EclOpCode.PARTY_SURPRISE, inst -> {

		});
		IMPL.put(EclOpCode.SURPRISE, inst -> {

		});
		IMPL.put(EclOpCode.COMBAT, inst -> {
			// TODO: Implement combat
			// For now set combat to success
			mem.setCombatResult(0);
		});
		IMPL.put(EclOpCode.ON_GOTO, inst -> {
			printDynArgs(inst);
			if (intValue(inst.getArgument(0)) >= intValue(inst.getArgument(1)) || intValue(inst.getArgument(0)) < 0) {
				System.err.println("ON GOTO value=" + intValue(inst.getArgument(0)));
				return;
			}
			goTo(inst.getDynArgs().get(intValue(inst.getArgument(0))));
		});
		IMPL.put(EclOpCode.ON_GOSUB, inst -> {
			printDynArgs(inst);
			if (intValue(inst.getArgument(0)) >= intValue(inst.getArgument(1)) || intValue(inst.getArgument(0)) < 0) {
				System.err.println("ON GOSUB value=" + intValue(inst.getArgument(0)));
				return;
			}
			gosubStack.push(eclCode.position());
			goTo(inst.getDynArgs().get(intValue(inst.getArgument(0))));
		});
		IMPL.put(EclOpCode.TREASURE, inst -> {
			printDynArgs(inst);
		});
		IMPL.put(EclOpCode.ROB, inst -> {

		});
		IMPL.put(EclOpCode.INPUT_RETURN, inst -> {
			engine.setInputHandler(InputType.RETURN, "PRESS BUTTON OR RETURN TO CONTINUE", InputAction.RETURN_ACTIONS);
		});
		IMPL.put(EclOpCode.COPY_MEM, inst -> {
			mem.copyMemInt(inst.getArgument(0), intValue(inst.getArgument(1)), inst.getArgument(2));
		});
		IMPL.put(EclOpCode.MENU_HORIZONTAL, inst -> {
			printDynArgs(inst);
			engine.setInputHandler(InputType.MENU, null,
				inst.getDynArgs().stream().map(arg -> new InputAction(arg.valueAsString().toString())).collect(Collectors.toList()));
			mem.writeMemInt(inst.getArgument(0), mem.getMenuChoice());
		});
		IMPL.put(EclOpCode.PARLAY, inst -> {

		});
		IMPL.put(EclOpCode.CALL, inst -> {

		});
		IMPL.put(EclOpCode.DAMAGE, inst -> {

		});
		IMPL.put(EclOpCode.AND, inst -> {
			int result = intValue(inst.getArgument(0)) & intValue(inst.getArgument(1));
			mem.writeMemInt(inst.getArgument(2), result);
			compareResult = result == 0 ? 0 : 1;
		});
		IMPL.put(EclOpCode.OR, inst -> {
			int result = intValue(inst.getArgument(0)) | intValue(inst.getArgument(1));
			mem.writeMemInt(inst.getArgument(2), result);
			compareResult = result == 0 ? 0 : 1;
		});
		IMPL.put(EclOpCode.SELECT_ACTION, inst -> {
			printDynArgs(inst);
			engine.addText(new EclString("WHAT DO YOU DO?"), false);
			engine.setInputHandler(InputType.MENU, null,
				inst.getDynArgs().stream().map(arg -> new InputAction(arg.valueAsString().toString())).collect(Collectors.toList()));
			mem.writeMemInt(inst.getArgument(0), mem.getMenuChoice());
		});
		IMPL.put(EclOpCode.FIND_ITEM, inst -> {

		});
		IMPL.put(EclOpCode.PRINT_RETURN, inst -> {
			engine.addNewline();
		});
		IMPL.put(EclOpCode.CLOCK, inst -> {

		});
		IMPL.put(EclOpCode.WRITE_MEM_BASE_OFF, inst -> {
			mem.writeMemInt(inst.getArgument(1), intValue(inst.getArgument(2)), intValue(inst.getArgument(0)));
		});
		IMPL.put(EclOpCode.ADD_NPC, inst -> {

		});
		IMPL.put(EclOpCode.LOAD_AREA_DECO, inst -> {
			engine.loadAreaDecoration(intValue(inst.getArgument(0)), intValue(inst.getArgument(1)), intValue(inst.getArgument(2)));
		});
		IMPL.put(EclOpCode.PROGRAM, inst -> {

		});
		IMPL.put(EclOpCode.WHO, inst -> {
			// TODO Implement party management
			mem.writeMemString(SELECTED_PLAYER_NAME, new EclString("THIS ONE"));
			mem.writeMemInt(SELECTED_PLAYER_STATUS, 1);
		});
		IMPL.put(EclOpCode.DELAY, inst -> {
			try {
				Thread.sleep(900);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		IMPL.put(EclOpCode.SPELL, inst -> {

		});
		IMPL.put(EclOpCode.PROTECTION, inst -> {

		});
		IMPL.put(EclOpCode.CLEAR_BOX, inst -> {

		});
		IMPL.put(EclOpCode.DUMP, inst -> {

		});
		IMPL.put(EclOpCode.LOGBOOK_ENTRY, inst -> {
			engine.addText(new EclString(" AND YOU RECORD "), false);
			engine.addText(stringValue(inst.getArgument(0)), false);
			engine.addText(new EclString(" AS LOGBOOK ENTRY " + intValue(inst.getArgument(1)) + "."), false);
			engine.setInputHandler(InputType.RETURN, "PRESS BUTTON OR RETURN TO CONTINUE", InputAction.RETURN_ACTIONS);
		});
		IMPL.put(EclOpCode.DESTROY_ITEM, inst -> {

		});
		IMPL.put(EclOpCode.UNKNOWN_41, inst -> {

		});
		IMPL.put(EclOpCode.STOP_MOVE, inst -> {
			stopVM();
		});
		IMPL.put(EclOpCode.UNKNOWN_43, inst -> {

		});
		IMPL.put(EclOpCode.UNKNOWN_44, inst -> {

		});
		IMPL.put(EclOpCode.UNKNOWN_45, inst -> {

		});
		IMPL.put(EclOpCode.UNKNOWN_46, inst -> {

		});
		IMPL.put(EclOpCode.UNKNOWN_47, inst -> {

		});
		IMPL.put(EclOpCode.UNKNOWN_48, inst -> {

		});
		IMPL.put(EclOpCode.UNKNOWN_49, inst -> {

		});
		IMPL.put(EclOpCode.UNKNOWN_4A, inst -> {

		});
		IMPL.put(EclOpCode.UNKNOWN_4B, inst -> {

		});
		IMPL.put(EclOpCode.PICTURE2, inst -> {
			engine.showPicture(intValue(inst.getArgument(1)));
		});
	}

	private void printDynArgs(EclInstruction inst) {
		System.out.println(String.join(", ", inst.getDynArgs().stream().map(EclArgument::toString).collect(Collectors.toList())));
	}
}
