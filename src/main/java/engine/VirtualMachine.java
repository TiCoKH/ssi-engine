package engine;

import static engine.EngineCallback.InputType.CONTINUE;
import static engine.InputAction.MENU_HANDLER;
import static engine.InputAction.YES_NO_ACTIONS;

import java.util.Deque;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import common.ByteBufferWrapper;
import data.content.EclProgram;
import engine.opcodes.EclArgument;
import engine.opcodes.EclInstruction;
import engine.opcodes.EclOpCode;
import engine.opcodes.EclString;

public class VirtualMachine {
	private static final EclArgument SELECTED_PLAYER_NAME = new EclArgument(1, 3, 0x7C00);
	private static final EclArgument SELECTED_PLAYER_STATUS = new EclArgument(1, 3, 0x7D00);

	private final Map<EclOpCode, Consumer<EclInstruction>> IMPL = new EnumMap<>(EclOpCode.class);

	private EngineCallback engine;

	private VirtualMemory memory = new VirtualMemory();
	private Deque<Integer> gosubStack = new ConcurrentLinkedDeque<>();
	private int compareResult = 0;
	private Random rnd = new Random();

	private int forLoopAddress;
	private int forLoopMax;

	private boolean stopped = true;

	private int eclCodeBaseAddress;
	private ByteBufferWrapper eclCode;
	private EclInstruction onEvent1; // Gets called on entering or exiting a NEW_ECL
	private EclInstruction onEnter;
	private EclInstruction onRest;
	private EclInstruction onRestInterruption;
	private EclInstruction onInit;

	public VirtualMachine(EngineCallback engine) {
		this.engine = engine;
		initImpl();
	}

	VirtualMemory getMemory() {
		return memory;
	}

	public void newEcl(EclProgram ecl) {
		eclCode = ecl.getCode().duplicate();
		onEvent1 = EclInstruction.parseNext(eclCode);
		onEnter = EclInstruction.parseNext(eclCode);
		onRest = EclInstruction.parseNext(eclCode);
		onRestInterruption = EclInstruction.parseNext(eclCode);
		onInit = EclInstruction.parseNext(eclCode);
		initCodeBase();
		memory.writeProgram(eclCodeBaseAddress, eclCode);
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
		exec(eventInst, true);
		runVM();
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
		return a.isMemAddress() ? memory.readMemInt(a) : a.valueAsInt();
	}

	private EclString stringValue(EclArgument a) {
		if (!a.isStringValue()) {
			return null;
		}
		return a.isMemAddress() ? memory.readMemString(a) : a.valueAsString();
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
			memory.writeMemInt(inst.getArgument(2), intValue(inst.getArgument(0)) + intValue(inst.getArgument(1)));
		});
		IMPL.put(EclOpCode.SUBTRACT, inst -> {
			memory.writeMemInt(inst.getArgument(2), intValue(inst.getArgument(1)) - intValue(inst.getArgument(0)));
		});
		IMPL.put(EclOpCode.DIVIDE, inst -> {
			memory.writeMemInt(inst.getArgument(2), intValue(inst.getArgument(0)) / intValue(inst.getArgument(1)));
		});
		IMPL.put(EclOpCode.MULTIPLY, inst -> {
			memory.writeMemInt(inst.getArgument(2), intValue(inst.getArgument(0)) * intValue(inst.getArgument(1)));
		});
		IMPL.put(EclOpCode.RANDOM, inst -> {
			memory.writeMemInt(inst.getArgument(1), rnd.nextInt(intValue(inst.getArgument(0)) + 1));
		});
		IMPL.put(EclOpCode.WRITE_MEM, inst -> {
			if (inst.getArgument(0).isNumberValue()) {
				memory.writeMemInt(inst.getArgument(1), intValue(inst.getArgument(0)));
			}
			if (inst.getArgument(0).isStringValue()) {
				memory.writeMemString(inst.getArgument(1), stringValue(inst.getArgument(0)));
			}
		});
		IMPL.put(EclOpCode.LOAD_CHAR, inst -> {

		});
		IMPL.put(EclOpCode.LOAD_MON, inst -> {

		});
		IMPL.put(EclOpCode.SPRITE_START, inst -> {
			engine.showSprite(intValue(inst.getArgument(0)), intValue(inst.getArgument(1)), intValue(inst.getArgument(2)));
		});
		IMPL.put(EclOpCode.SPRITE_ADVANCE, inst -> {
			engine.advanceSprite();
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
			engine.clearSprite();
		});
		IMPL.put(EclOpCode.PARTY_STRENGTH, inst -> {

		});
		IMPL.put(EclOpCode.PARTY_CHECK, inst -> {

		});
		IMPL.put(EclOpCode.SPACE_COMBAT, inst -> {

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
			memory.setCombatResult(0);
		});
		IMPL.put(EclOpCode.ON_GOTO, inst -> {
			if (intValue(inst.getArgument(0)) >= intValue(inst.getArgument(1)) || intValue(inst.getArgument(0)) < 0) {
				System.err.println("ON GOTO value=" + intValue(inst.getArgument(0)));
				return;
			}
			goTo(inst.getDynArgs().get(intValue(inst.getArgument(0))));
		});
		IMPL.put(EclOpCode.ON_GOSUB, inst -> {
			if (intValue(inst.getArgument(0)) >= intValue(inst.getArgument(1)) || intValue(inst.getArgument(0)) < 0) {
				System.err.println("ON GOSUB value=" + intValue(inst.getArgument(0)));
				return;
			}
			gosubStack.push(eclCode.position());
			goTo(inst.getDynArgs().get(intValue(inst.getArgument(0))));
		});
		IMPL.put(EclOpCode.TREASURE, inst -> {

		});
		IMPL.put(EclOpCode.ROB, inst -> {

		});
		IMPL.put(EclOpCode.INPUT_RETURN, inst -> {
			engine.setInput(CONTINUE);
		});
		IMPL.put(EclOpCode.COPY_MEM, inst -> {
			memory.copyMemInt(inst.getArgument(0), intValue(inst.getArgument(1)), inst.getArgument(2));
		});
		IMPL.put(EclOpCode.MENU_HORIZONTAL, inst -> {
			engine.setMenu(
				inst.getDynArgs().stream().map(arg -> new InputAction(MENU_HANDLER, arg.valueAsString().toString(), inst.getDynArgs().indexOf(arg)))
					.collect(Collectors.toList()));
			memory.writeMemInt(inst.getArgument(0), memory.getMenuChoice());
		});
		IMPL.put(EclOpCode.INPUT_YES_NO, inst -> {
			engine.setMenu(YES_NO_ACTIONS);
			compareResult = memory.getMenuChoice();
		});
		IMPL.put(EclOpCode.CALL, inst -> {
			switch (inst.getArgument(0).valueAsInt()) {
				case 0x2DCB:
					engine.updatePosition();
					engine.clearPics();
					break;
				default:
			}
		});
		IMPL.put(EclOpCode.DAMAGE, inst -> {

		});
		IMPL.put(EclOpCode.AND, inst -> {
			int result = intValue(inst.getArgument(0)) & intValue(inst.getArgument(1));
			memory.writeMemInt(inst.getArgument(2), result);
			compareResult = result == 0 ? 0 : 1;
		});
		IMPL.put(EclOpCode.OR, inst -> {
			int result = intValue(inst.getArgument(0)) | intValue(inst.getArgument(1));
			memory.writeMemInt(inst.getArgument(2), result);
			compareResult = result == 0 ? 0 : 1;
		});
		IMPL.put(EclOpCode.SELECT_ACTION, inst -> {
			engine.addText(new EclString("WHAT DO YOU DO?"), false);
			engine.setMenu(
				inst.getDynArgs().stream().map(arg -> new InputAction(MENU_HANDLER, arg.valueAsString().toString(), inst.getDynArgs().indexOf(arg)))
					.collect(Collectors.toList()));
			memory.writeMemInt(inst.getArgument(0), memory.getMenuChoice());
		});
		IMPL.put(EclOpCode.FIND_ITEM, inst -> {

		});
		IMPL.put(EclOpCode.PRINT_RETURN, inst -> {
			engine.addNewline();
		});
		IMPL.put(EclOpCode.CLOCK, inst -> {

		});
		IMPL.put(EclOpCode.WRITE_MEM_BASE_OFF, inst -> {
			memory.writeMemInt(inst.getArgument(1), intValue(inst.getArgument(2)), intValue(inst.getArgument(0)));
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
			memory.writeMemString(SELECTED_PLAYER_NAME, new EclString("THIS ONE"));
			memory.writeMemInt(SELECTED_PLAYER_STATUS, 1);
		});
		IMPL.put(EclOpCode.DELAY, inst -> {
			engine.delayCurrentThread();
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
			engine.setInput(CONTINUE);
		});
		IMPL.put(EclOpCode.DESTROY_ITEM, inst -> {

		});
		IMPL.put(EclOpCode.UNKNOWN_41, inst -> {

		});
		IMPL.put(EclOpCode.STOP_MOVE, inst -> {
			stopVM();
			engine.updatePosition();
			engine.clear();
		});
		IMPL.put(EclOpCode.SOUND_EVENT, inst -> {

		});
		IMPL.put(EclOpCode.UNKNOWN_44, inst -> {

		});
		IMPL.put(EclOpCode.RANDOM0, inst -> {
			int rndVal = intValue(inst.getArgument(1));
			if (rndVal > 0)
				rndVal = rnd.nextInt(rndVal + 1);
			memory.writeMemInt(inst.getArgument(0), rndVal);
		});
		IMPL.put(EclOpCode.FOR_START, inst -> {
			memory.setForLoopCount(intValue(inst.getArgument(0)));
			forLoopMax = intValue(inst.getArgument(1));
			forLoopAddress = inst.getPosition() + inst.getSize();
		});
		IMPL.put(EclOpCode.FOR_REPEAT, inst -> {
			memory.setForLoopCount(memory.getForLoopCount() + 1);
			if (memory.getForLoopCount() <= forLoopMax) {
				eclCode.position(forLoopAddress);
			}
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
			engine.showPicture(intValue(inst.getArgument(0)), intValue(inst.getArgument(1)));
		});
	}
}
