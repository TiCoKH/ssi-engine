package engine;

import static engine.InputAction.CONTINUE_ACTION;
import static engine.InputAction.CONTINUE_HANDLER;
import static engine.InputAction.MENU_HANDLER;
import static engine.InputAction.YES_NO_ACTIONS;
import static types.MenuType.HORIZONTAL;
import static types.MenuType.VERTICAL;

import java.util.Deque;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

import common.ByteBufferWrapper;
import data.content.EclProgram;
import engine.opcodes.EclArgument;
import engine.opcodes.EclInstruction;
import engine.opcodes.EclOpCode;
import types.CustomGoldboxString;
import types.GoldboxString;

public class VirtualMachine {
	private static final EclArgument SELECTED_PLAYER_NAME = new EclArgument(1, 3, 0x7C00);
	private static final EclArgument SELECTED_PLAYER_MONEY = new EclArgument(5, 3, 0x7C2B);
	private static final EclArgument SELECTED_PLAYER_STATUS = new EclArgument(1, 3, 0x7D00);

	private final Map<EclOpCode, Consumer<EclInstruction>> IMPL = new EnumMap<>(EclOpCode.class);

	private EngineCallback engine;

	private VirtualMemory memory;
	private Deque<Integer> gosubStack = new ConcurrentLinkedDeque<>();
	private int compareResult = 0;
	private Random rnd = new Random();

	private int forLoopAddress;
	private int forLoopMax;

	private boolean stopped = true;

	private int eclCodeBaseAddress;
	private ByteBufferWrapper eclCode;
	private EclInstruction onMove;
	private EclInstruction onSearchLocation;
	private EclInstruction onRest;
	private EclInstruction onRestInterruption;
	private EclInstruction onInit;

	public VirtualMachine(EngineCallback engine, VirtualMemory memory, int eclCodeBaseAddress) {
		this.engine = engine;
		this.memory = memory;
		this.eclCodeBaseAddress = eclCodeBaseAddress;
		initImpl();
	}

	VirtualMemory getMemory() {
		return memory;
	}

	public void newEcl(EclProgram ecl) {
		eclCode = ecl.getCode().duplicate();
		onMove = EclInstruction.parseNext(eclCode);
		onSearchLocation = EclInstruction.parseNext(eclCode);
		onRest = EclInstruction.parseNext(eclCode);
		onRestInterruption = EclInstruction.parseNext(eclCode);
		onInit = EclInstruction.parseNext(eclCode);
		memory.writeProgram(eclCodeBaseAddress, eclCode);
	}

	public void startMove() {
		System.out.println("onMove:");
		startEvent(onMove);
	}

	public void startSearchLocation() {
		System.out.println("onSearchLocation:");
		startEvent(onSearchLocation);
	}

	public void startRest() {
		System.out.println("onRest:");
		startEvent(onRest);
	}

	public void startRestInterruption() {
		System.out.println("onRestInterruption:");
		startEvent(onRestInterruption);
	}

	public void startInit() {
		System.out.println("onInit:");
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

	private GoldboxString stringValue(EclArgument a) {
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
		IMPL.put(EclOpCode.SPRITE_START3, inst -> {
			engine.showSprite(intValue(inst.getArgument(0)), intValue(inst.getArgument(1)), intValue(inst.getArgument(2)));
		});
		IMPL.put(EclOpCode.SPRITE_START4, inst -> {
			engine.showSprite(intValue(inst.getArgument(0)), intValue(inst.getArgument(1)), intValue(inst.getArgument(2)));
		});
		IMPL.put(EclOpCode.SPRITE_ADVANCE, inst -> {
			engine.advanceSprite();
		});
		IMPL.put(EclOpCode.PICTURE, inst -> {
			engine.showPicture(intValue(inst.getArgument(0)));
		});
		IMPL.put(EclOpCode.INPUT_NUMBER, inst -> {
			engine.setInputNumber(intValue(inst.getArgument(0)));
			int value = Integer.parseUnsignedInt(memory.getInput().toString());
			memory.writeMemInt(inst.getArgument(1), value);
		});
		IMPL.put(EclOpCode.INPUT_STRING, inst -> {
			engine.setInputString(intValue(inst.getArgument(0)));
			GoldboxString value = memory.getInput();
			memory.writeMemString(inst.getArgument(1), value);
		});
		IMPL.put(EclOpCode.PRINT, inst -> {
			EclArgument a0 = inst.getArgument(0);
			if (a0.isStringValue())
				engine.addText(stringValue(a0), false);
			else
				engine.addText(new CustomGoldboxString(Integer.toString(intValue(a0))), false);
		});
		IMPL.put(EclOpCode.PRINT_CLEAR, inst -> {
			EclArgument a0 = inst.getArgument(0);
			if (a0.isStringValue())
				engine.addText(stringValue(a0), true);
			else
				engine.addText(new CustomGoldboxString(Integer.toString(intValue(a0))), true);
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
			engine.setMenu(VERTICAL, //
				inst.getDynArgs().stream().map(arg -> new InputAction(MENU_HANDLER, arg.valueAsString(), inst.getDynArgs().indexOf(arg)))
					.collect(Collectors.toList()),
				stringValue(inst.getArgument(1)));
			memory.writeMemInt(inst.getArgument(0), memory.getMenuChoice());
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
		IMPL.put(EclOpCode.INPUT_RETURN_1D, inst -> {
			engine.setMenu(HORIZONTAL, CONTINUE_ACTION, null);
		});
		IMPL.put(EclOpCode.PARTY_STRENGTH, inst -> {

		});
		IMPL.put(EclOpCode.PARTY_CHECK, inst -> {

		});
		IMPL.put(EclOpCode.JOURNAL_ENTRY, inst -> {
			engine.addText(new CustomGoldboxString("THIS IS RECORDED AS JOURNAL ENTRY " + intValue(inst.getArgument(0)) + "."), true);
			engine.setMenu(HORIZONTAL, CONTINUE_ACTION, null);
		});
		IMPL.put(EclOpCode.SPACE_COMBAT, inst -> {

		});
		IMPL.put(EclOpCode.NEW_ECL, inst -> {
			stopVM();
			engine.loadEcl(intValue(inst.getArgument(0)));
		});
		IMPL.put(EclOpCode.LOAD_AREA_MAP_DECO, inst -> {
			engine.loadArea(intValue(inst.getArgument(0)), 127, 127);
			engine.loadAreaDecoration(intValue(inst.getArgument(1)), 127, 127);
		});
		IMPL.put(EclOpCode.LOAD_AREA_MAP, inst -> {
			engine.loadArea(intValue(inst.getArgument(0)), intValue(inst.getArgument(1)), intValue(inst.getArgument(2)));
		});
		IMPL.put(EclOpCode.INPUT_YES_NO_22, inst -> {
			engine.setMenu(HORIZONTAL, YES_NO_ACTIONS, null);
			compareResult = memory.getMenuChoice();
		});
		IMPL.put(EclOpCode.PARTY_SKILL_CHECK2, inst -> {

		});
		IMPL.put(EclOpCode.PARTY_SKILL_CHECK3, inst -> {
			memory.writeMemInt(inst.getArgument(2), 100);
		});
		IMPL.put(EclOpCode.STOP_MOVE_23, inst -> {
			stopVM();
			engine.updatePosition();
			engine.clear();
		});
		IMPL.put(EclOpCode.SURPRISE, inst -> {

		});
		IMPL.put(EclOpCode.SKILL_CHECK, inst -> {

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
		IMPL.put(EclOpCode.TREASURE_MULTICOIN, inst -> {

		});
		IMPL.put(EclOpCode.TREASURE_MULTICOIN4, inst -> {

		});
		IMPL.put(EclOpCode.ROB, inst -> {

		});
		IMPL.put(EclOpCode.ENCOUNTER_MENU, inst -> {
			// TODO Determine party movement rates
			int pt_move_min = 6, pt_move_max = 12;
			int distance = engine.showSprite(intValue(inst.getArgument(0)), intValue(inst.getArgument(1)), intValue(inst.getArgument(2)));

			boolean do_another_round;
			do {
				do_another_round = false;

				GoldboxString desc;
				int index = distance;
				do {
					desc = stringValue(inst.getArgument(9 + index));
					index = (index + 1) % 3;
				} while (desc.getLength() == 0 && index != distance);
				if (desc.getLength() != 0) {
					engine.addText(desc, true);
				}
				engine.setMenu(HORIZONTAL, CONTINUE_ACTION, null);

				engine.setMenu(HORIZONTAL, ImmutableList.of( //
					new InputAction(MENU_HANDLER, "COMBAT", 0), //
					new InputAction(MENU_HANDLER, "WAIT", 1), //
					new InputAction(MENU_HANDLER, "FLEE", 2), //
					distance != 0 ? new InputAction(MENU_HANDLER, "ADVANCE", 3) : new InputAction(MENU_HANDLER, "PARLAY", 4) //
				), null);

				int enc_move_min = intValue(inst.getArgument(12));
				int enc_move_max = intValue(inst.getArgument(13));
				int choice = memory.getMenuChoice();
				int behaviour = intValue(inst.getArgument(4 + choice));
				switch (behaviour) {
					case 0: // aggressive monsters
						int result0 = choice != 2 || pt_move_min < enc_move_min ? 1 : 2; // Combat or Party flees
						memory.writeMemInt(inst.getArgument(3), result0);
						break;
					case 1: // passive monsters
						if (choice == 0) {
							memory.writeMemInt(inst.getArgument(3), 1); // Combat
						} else if (choice == 1) {
							engine.addText(new CustomGoldboxString("BOTH SIDES WAIT."), true);
							engine.setMenu(HORIZONTAL, CONTINUE_ACTION, null);
							do_another_round = true;
						} else if (choice == 2) {
							memory.writeMemInt(inst.getArgument(3), 2); // Party flees
						} else if (choice == 4) {
							memory.writeMemInt(inst.getArgument(3), 3); // Parlay
						} else if (distance > 0) {
							engine.advanceSprite();
							distance--;
							do_another_round = true;
						} else {
							engine.addText(new CustomGoldboxString("BOTH SIDES WAIT."), true);
							engine.setMenu(HORIZONTAL, CONTINUE_ACTION, null);
							do_another_round = true;
						}
						break;
					case 2: // cowardly monsters
						int result2 = choice == 0 && pt_move_max > enc_move_max ? 1 : 0; // Combat or Monsters flee
						memory.writeMemInt(inst.getArgument(3), result2);
						if (choice != 0 || enc_move_max > pt_move_max) {
							engine.addText(new CustomGoldboxString("THE MONSTERS FLEE."), true);
							engine.setMenu(HORIZONTAL, CONTINUE_ACTION, null);
						}
						break;
					case 3: // cautious monsters
						if (choice == 0) {
							memory.writeMemInt(inst.getArgument(3), 1); // Combat
						} else if (choice == 1) {
							engine.addText(new CustomGoldboxString("BOTH SIDES WAIT."), true);
							engine.setMenu(HORIZONTAL, CONTINUE_ACTION, null);
							do_another_round = true;
						} else if (choice == 2) {
							memory.writeMemInt(inst.getArgument(3), 2); // Party flees
						} else if (distance > 0) {
							engine.advanceSprite();
							distance--;
							do_another_round = true;
						} else {
							memory.writeMemInt(inst.getArgument(3), 3); // Parlay
						}
						break;
					case 4: // confident monsters
						if (choice == 0) {
							memory.writeMemInt(inst.getArgument(3), 1); // Combat
						} else if (choice == 2) {
							memory.writeMemInt(inst.getArgument(3), 2); // Party flees
						} else if (distance > 0) {
							engine.advanceSprite();
							distance--;
							do_another_round = true;
						} else {
							memory.writeMemInt(inst.getArgument(3), 3); // Parlay
						}
						break;
				}
			} while (do_another_round);
		});
		IMPL.put(EclOpCode.INPUT_RETURN_29, inst -> {
			engine.setMenu(HORIZONTAL, CONTINUE_ACTION, null);
		});
		IMPL.put(EclOpCode.POD_29, inst -> {

		});
		IMPL.put(EclOpCode.COPY_MEM, inst -> {
			memory.copyMemInt(inst.getArgument(0), intValue(inst.getArgument(1)), inst.getArgument(2));
		});
		IMPL.put(EclOpCode.MENU_HORIZONTAL, inst -> {
			if (inst.getDynArgs().size() == 1) {
				engine.setMenu(HORIZONTAL, ImmutableList.of( //
					new InputAction(CONTINUE_HANDLER, stringValue(inst.getDynArgs().get(0)), -1) //
				), null);
			} else {
				engine.setMenu(HORIZONTAL, //
					inst.getDynArgs().stream().map(arg -> new InputAction(MENU_HANDLER, arg.valueAsString(), inst.getDynArgs().indexOf(arg)))
						.collect(Collectors.toList()),
					null);
				memory.writeMemInt(inst.getArgument(0), memory.getMenuChoice());
			}
		});
		IMPL.put(EclOpCode.PARLAY, inst -> {
			engine.setMenu(HORIZONTAL, ImmutableList.of( //
				new InputAction(MENU_HANDLER, "HAUGHTY", 0), //
				new InputAction(MENU_HANDLER, "SLY", 1), //
				new InputAction(MENU_HANDLER, "NICE", 2), //
				new InputAction(MENU_HANDLER, "MEEK", 3), //
				new InputAction(MENU_HANDLER, "ABUSIVE", 4) //
			), null);
			memory.writeMemInt(inst.getArgument(5), intValue(inst.getArgument(memory.getMenuChoice())));
		});
		IMPL.put(EclOpCode.SOUND_EVENT_2C, inst -> {

		});
		IMPL.put(EclOpCode.INPUT_YES_NO_2C, inst -> {
			engine.setMenu(HORIZONTAL, YES_NO_ACTIONS, null);
			compareResult = memory.getMenuChoice();
		});
		IMPL.put(EclOpCode.CALL, inst -> {
			switch (inst.getArgument(0).valueAsInt()) {
				case 0x0809: // GttSF
					engine.setMenu(HORIZONTAL, CONTINUE_ACTION, null);
					break;
				case 0x080C: // GttSF
					engine.setMenu(HORIZONTAL, YES_NO_ACTIONS, null);
					compareResult = memory.getMenuChoice() == 0 ? 1 : 0;
					break;
				case 0x0001: // PoD
				case 0x2C90: // PoR
				case 0x2DCB: // BR, SotSB
				case 0x2E10: // CotAB, GttSF
					engine.updatePosition();
					engine.clearSprite();
					break;
				case 0x0002: // PoD
				case 0xC01E: // PoR, CotAB
					memory.setDungeonX((memory.getDungeonX() + memory.getDungeonDir().getDeltaX()) & 0xF);
					memory.setDungeonY((memory.getDungeonY() + memory.getDungeonDir().getDeltaY()) & 0xF);
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
		IMPL.put(EclOpCode.SPRITE_OFF, inst -> {
			engine.clearSprite();
		});
		IMPL.put(EclOpCode.SELECT_ACTION, inst -> {
			engine.addText(new CustomGoldboxString("WHAT DO YOU DO?"), false);
			engine.setMenu(HORIZONTAL, inst.getDynArgs().stream()
				.map(arg -> new InputAction(MENU_HANDLER, arg.valueAsString(), inst.getDynArgs().indexOf(arg))).collect(Collectors.toList()), null);
			memory.writeMemInt(inst.getArgument(0), memory.getMenuChoice());
		});
		IMPL.put(EclOpCode.FIND_ITEM, inst -> {

		});
		IMPL.put(EclOpCode.GTTSF_32, inst -> {

		});
		IMPL.put(EclOpCode.PRINT_RETURN, inst -> {
			engine.addNewline();
		});
		IMPL.put(EclOpCode.CLOCK1, inst -> {

		});
		IMPL.put(EclOpCode.CLOCK2, inst -> {

		});
		IMPL.put(EclOpCode.WRITE_MEM_BASE_OFF, inst -> {
			memory.writeMemInt(inst.getArgument(1), intValue(inst.getArgument(2)), intValue(inst.getArgument(0)));
		});
		IMPL.put(EclOpCode.NPC_ADD, inst -> {

		});
		IMPL.put(EclOpCode.NPC_FIND, inst -> {

		});
		IMPL.put(EclOpCode.LOAD_AREA_DECO, inst -> {
			engine.loadAreaDecoration(intValue(inst.getArgument(0)), intValue(inst.getArgument(1)), intValue(inst.getArgument(2)));
		});
		IMPL.put(EclOpCode.PROGRAM, inst -> {

		});
		IMPL.put(EclOpCode.WHO, inst -> {
			// TODO Implement party management
			memory.writeMemString(SELECTED_PLAYER_NAME, new CustomGoldboxString("THIS ONE"));
			memory.writeMemInt(SELECTED_PLAYER_MONEY, 10000);
			memory.writeMemInt(SELECTED_PLAYER_STATUS, 1);
		});
		IMPL.put(EclOpCode.DELAY, inst -> {
			engine.delayCurrentThread();
		});
		IMPL.put(EclOpCode.SPELL, inst -> {

		});
		IMPL.put(EclOpCode.PRINT_RUNES, inst -> {

		});
		IMPL.put(EclOpCode.COPY_PROTECTION, inst -> {

		});
		IMPL.put(EclOpCode.STORE, inst -> {

		});
		IMPL.put(EclOpCode.CLEAR_BOX, inst -> {

		});
		IMPL.put(EclOpCode.NPC_REMOVE, inst -> {

		});
		IMPL.put(EclOpCode.HAS_EFFECT, inst -> {

		});
		IMPL.put(EclOpCode.LOGBOOK_ENTRY, inst -> {
			engine.addText(new CustomGoldboxString(" AND YOU RECORD "), false);
			engine.addText(stringValue(inst.getArgument(0)), false);
			engine.addText(new CustomGoldboxString(" AS LOGBOOK ENTRY " + intValue(inst.getArgument(1)) + "."), false);
			engine.setMenu(HORIZONTAL, CONTINUE_ACTION, null);
		});
		IMPL.put(EclOpCode.DESTROY_ITEM, inst -> {

		});
		IMPL.put(EclOpCode.GTTSF_40, inst -> {

		});
		IMPL.put(EclOpCode.GIVE_EXP, inst -> {

		});
		IMPL.put(EclOpCode.STOP_MOVE_42, inst -> {
			stopVM();
			engine.updatePosition();
			engine.clear();
		});
		IMPL.put(EclOpCode.LOAD_AREA_MAP_DECOS, inst -> {
			engine.loadArea(intValue(inst.getArgument(0)), 127, 127);
			engine.loadAreaDecoration(intValue(inst.getArgument(1)), intValue(inst.getArgument(2)), intValue(inst.getArgument(3)));
		});
		IMPL.put(EclOpCode.SOUND_EVENT_43, inst -> {

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
