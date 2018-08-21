package engine;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

import data.content.EclProgram;
import engine.opcodes.EclArgument;
import engine.opcodes.EclInstruction;
import engine.opcodes.EclOpCode;

public class VirtualMachine {

	private final Map<EclOpCode, Consumer<EclArgument[]>> IMPL = new EnumMap<>(EclOpCode.class);

	private boolean stopped = true;

	private int eclCodeBaseAddress;
	private ByteBuffer eclCode;
	private EclInstruction onEvent1;
	private EclInstruction onEnter;
	private EclInstruction onRest;
	private EclInstruction onRestInterruption;
	private EclInstruction onInit;

	public VirtualMachine() {
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
		exec(onEvent1);
		runVM();
	}

	public void startSearchLocation() {
		stopped = false;
		exec(onEnter);
		runVM();
	}

	public void startPreCampCheck() {
		stopped = false;
		exec(onRest);
		runVM();
	}

	public void startCampInterrupted() {
		stopped = false;
		exec(onRestInterruption);
		runVM();
	}

	public void startInitial() {
		stopped = false;
		exec(onInit);
		runVM();
	}

	private void runVM() {
		while (!stopped) {
			exec(EclInstruction.parseNext(eclCode));
		}
	}

	private void stopVM() {
		stopped = true;
	}

	private void exec(EclInstruction in) {
		System.out.println(in.toString());
		IMPL.get(in.getOpCode()).accept(in.getArguments());
	}

	private void initImpl() {
		IMPL.clear();
		IMPL.put(EclOpCode.EXIT, args -> {
			stopVM();
		});
		IMPL.put(EclOpCode.GOTO, args -> {
			eclCode.position(args[0].valueAsInt() - eclCodeBaseAddress);
		});
		IMPL.put(EclOpCode.GOSUB, args -> {
			eclCode.mark();
			eclCode.position(args[0].valueAsInt() - eclCodeBaseAddress);
		});
		IMPL.put(EclOpCode.COMPARE, args -> {

		});
		IMPL.put(EclOpCode.ADD, args -> {

		});
		IMPL.put(EclOpCode.SUBTRACT, args -> {

		});
		IMPL.put(EclOpCode.DIVIDE, args -> {

		});
		IMPL.put(EclOpCode.MULTIPLY, args -> {

		});
		IMPL.put(EclOpCode.RANDOM, args -> {

		});
		IMPL.put(EclOpCode.SAVE, args -> {

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
			eclCode.reset();
		});
		IMPL.put(EclOpCode.COMPARE_AND, args -> {

		});
		IMPL.put(EclOpCode.MENU_VERTICAL, args -> {

		});
		IMPL.put(EclOpCode.IF_EQUALS, args -> {

		});
		IMPL.put(EclOpCode.IF_NOT_EQUALS, args -> {

		});
		IMPL.put(EclOpCode.IF_LESS, args -> {

		});
		IMPL.put(EclOpCode.IF_GREATER, args -> {

		});
		IMPL.put(EclOpCode.IF_LESS_EQUALS, args -> {

		});
		IMPL.put(EclOpCode.IF_GREATER_EQUALS, args -> {

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

		});
		IMPL.put(EclOpCode.ON_GOSUB, args -> {

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
	}
}
