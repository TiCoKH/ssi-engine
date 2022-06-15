package engine;

import static io.vavr.API.Seq;

import javax.annotation.Nonnull;

import io.vavr.collection.Seq;

import engine.input.CharacterSheetEXITHandler;
import engine.input.DialogBackHandler;
import engine.input.GameMenuAREAHandler;
import engine.input.GameMenuEXITHandler;
import engine.input.GameMenuLOOKHandler;
import engine.input.GameMenuMOVEHandler;
import engine.input.GameMenuSEARCHHandler;
import engine.input.InputHandler;
import engine.input.LoadHandler;
import engine.input.MenuHandler;
import engine.input.ModeMenuHandler;
import engine.input.MovementHandler;
import engine.input.ProgramMenuAddCharacterHandler;
import engine.input.ProgramMenuBeginHandler;
import engine.input.ProgramMenuCreateCharacterHandler;
import engine.input.ProgramMenuModifyCharacterHandler;
import engine.input.ProgramMenuRemoveCharacterHandler;
import engine.input.ProgramMenuViewCharacterHandler;
import engine.input.SaveHandler;
import shared.CustomGoldboxString;
import shared.GoldboxString;
import shared.InputAction;

public class EngineInputAction implements InputAction {
	static final MovementHandler MOVEMENT_HANDLER = new MovementHandler();
	static final InputHandler MENU_HANDLER = new MenuHandler();
	static final InputHandler MODE_MENU_HANDLER = new ModeMenuHandler();

	static final InputHandler BACK_HANDLER = new DialogBackHandler();

	static final InputHandler LOAD_HANDLER = new LoadHandler();
	static final InputHandler SAVE_HANDLER = new SaveHandler();

	static final InputHandler GAME_MENU_MOVE_HANDLER = new GameMenuMOVEHandler();
	static final InputHandler GAME_MENU_AREA_HANDLER = new GameMenuAREAHandler();
	static final InputHandler GAME_MENU_SEARCH_HANDLER = new GameMenuSEARCHHandler();
	static final InputHandler GAME_MENU_LOOK_HANDLER = new GameMenuLOOKHandler();
	static final InputHandler GAME_MENU_EXIT_HANDLER = new GameMenuEXITHandler();

	public static final InputHandler LOAD = new LoadHandler();

	private static final InputHandler PROGRAM_MENU_CREATE_CHAR_HANDLER = new ProgramMenuCreateCharacterHandler();
	private static final InputHandler PROGRAM_MENU_ADD_CHAR_HANDLER = new ProgramMenuAddCharacterHandler();
	private static final InputHandler PROGRAM_MENU_VIEW_CHAR_HANDLER = new ProgramMenuViewCharacterHandler();
	private static final InputHandler PROGRAM_MENU_MODIFY_CHAR_HANDLER = new ProgramMenuModifyCharacterHandler();
	private static final InputHandler PROGRAM_MENU_REMOVE_CHAR_HANDLER = new ProgramMenuRemoveCharacterHandler();
	private static final InputHandler PROGRAM_MENU_BEGIN_HANDLER = new ProgramMenuBeginHandler();

	private static final InputHandler CHAR_SHEET_EXIT_HANDLER = new CharacterSheetEXITHandler();

	private static final EngineInputAction YES = new EngineInputAction(MENU_HANDLER, "YES", 0);
	private static final EngineInputAction NO = new EngineInputAction(MENU_HANDLER, "NO", 1);

	private static final EngineInputAction CONTINUE = new EngineInputAction(MENU_HANDLER,
		"PRESS BUTTON OR RETURN TO CONTINUE", 0);

	private static final EngineInputAction DO_SAVE = new EngineInputAction(SAVE_HANDLER, SAVE);

	private static final EngineInputAction MOVE_FORWARD_UP = new EngineInputAction(MOVEMENT_HANDLER, FORWARD_UP);
	private static final EngineInputAction MOVE_TURN_LEFT = new EngineInputAction(MOVEMENT_HANDLER, TURN_LEFT);
	private static final EngineInputAction MOVE_TURN_RIGHT = new EngineInputAction(MOVEMENT_HANDLER, TURN_RIGHT);
	private static final EngineInputAction TURN_AROUND_DOWN = new EngineInputAction(MOVEMENT_HANDLER, UTURN_DOWN);
	private static final EngineInputAction EXIT_MOVE = new EngineInputAction(GAME_MENU_EXIT_HANDLER, "EXIT");

	public static final EngineInputAction SELECT = new EngineInputAction(MENU_HANDLER, "SELECT");
	public static final EngineInputAction DIALOG_BACK = new EngineInputAction(BACK_HANDLER, "BACK");

	public static final EngineInputAction MOVE = new EngineInputAction(GAME_MENU_MOVE_HANDLER, "MOVE");
	public static final EngineInputAction AREA = new EngineInputAction(GAME_MENU_AREA_HANDLER, "AREA");
	public static final EngineInputAction SEARCH = new EngineInputAction(GAME_MENU_SEARCH_HANDLER, "SEARCH");
	public static final EngineInputAction LOOK = new EngineInputAction(GAME_MENU_LOOK_HANDLER, "LOOK");

	public static final EngineInputAction CREATE_CHAR = new EngineInputAction(PROGRAM_MENU_CREATE_CHAR_HANDLER,
		"CREATE NEW CHARACTER");
	public static final EngineInputAction ADD_CHAR = new EngineInputAction(PROGRAM_MENU_ADD_CHAR_HANDLER,
		"ADD CHARACTER TO PARTY");
	public static final EngineInputAction VIEW_CHAR = new EngineInputAction(PROGRAM_MENU_VIEW_CHAR_HANDLER,
		"VIEW CHARACTER");
	public static final EngineInputAction MODIFY_CHAR = new EngineInputAction(PROGRAM_MENU_MODIFY_CHAR_HANDLER,
		"MODIFY CHARACTER");
	public static final EngineInputAction REMOVE_CHAR = new EngineInputAction(PROGRAM_MENU_REMOVE_CHAR_HANDLER,
		"REMOVE CHARACTER FROM PARTY");
	public static final EngineInputAction BEGIN_ADVENTURE = new EngineInputAction(PROGRAM_MENU_BEGIN_HANDLER,
		"BEGIN ADVENTURING");
	public static final EngineInputAction LOAD_GAME = new EngineInputAction(LOAD_HANDLER, "LOAD SAVED GAME");
	public static final EngineInputAction SAVE_GAME = new EngineInputAction(SAVE_HANDLER, "SAVE CURRENT GAME");

	public static final EngineInputAction EXIT_SHEET = new EngineInputAction(CHAR_SHEET_EXIT_HANDLER, "EXIT");

	public static final Seq<InputAction> YES_NO_ACTIONS = Seq(YES, NO);
	public static final Seq<InputAction> MOVEMENT_ACTIONS = Seq(DO_SAVE, MOVE_FORWARD_UP, MOVE_TURN_LEFT,
		MOVE_TURN_RIGHT, TURN_AROUND_DOWN, EXIT_MOVE);
	public static final Seq<InputAction> GAME_MENU_ACTIONS = Seq(DO_SAVE, MOVE, AREA, SEARCH, LOOK);
	public static final Seq<InputAction> CONTINUE_ACTION = Seq(CONTINUE);
	public static final Seq<InputAction> DIALOG_MENU_ACTIONS = Seq(SELECT, DIALOG_BACK);
	public static final Seq<InputAction> CHAR_SHEET_ACTION = Seq(EXIT_SHEET);
	public static final Seq<InputAction> SELECT_ACTION = Seq(SELECT);

	private final InputHandler handler;
	private final GoldboxString name;
	private final int index;

	public EngineInputAction(@Nonnull InputHandler handler, @Nonnull String name) {
		this(handler, new CustomGoldboxString(name), -1);
	}

	public EngineInputAction(@Nonnull InputHandler handler, @Nonnull String name, int index) {
		this(handler, new CustomGoldboxString(name), index);
	}

	public EngineInputAction(@Nonnull InputHandler handler, @Nonnull GoldboxString name) {
		this(handler, name, -1);
	}

	public EngineInputAction(@Nonnull InputHandler handler, @Nonnull GoldboxString name, int index) {
		this.handler = handler;
		this.name = name;
		this.index = index;
	}

	public InputHandler getHandler() {
		return handler;
	}

	@Override
	public GoldboxString getName() {
		return name;
	}

	public int getIndex() {
		return index;
	}

	@Override
	public String toString() {
		return name.toString();
	}
}
