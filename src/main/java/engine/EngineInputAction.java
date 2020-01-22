package engine;

import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;

import engine.input.GameMenuAREAHandler;
import engine.input.GameMenuEXITHandler;
import engine.input.GameMenuLOOKHandler;
import engine.input.GameMenuMOVEHandler;
import engine.input.GameMenuSEARCHHandler;
import engine.input.InputHandler;
import engine.input.MenuHandler;
import engine.input.ModeMenuHandler;
import engine.input.MovementHandler;
import engine.input.SaveHandler;
import shared.CustomGoldboxString;
import shared.GoldboxString;
import shared.InputAction;

public class EngineInputAction implements InputAction {
	static final MovementHandler MOVEMENT_HANDLER = new MovementHandler();
	static final InputHandler MENU_HANDLER = new MenuHandler();
	static final InputHandler MODE_MENU_HANDLER = new ModeMenuHandler();

	static final InputHandler GAME_MENU_MOVE_HANDLER = new GameMenuMOVEHandler();
	static final InputHandler GAME_MENU_AREA_HANDLER = new GameMenuAREAHandler();
	static final InputHandler GAME_MENU_SEARCH_HANDLER = new GameMenuSEARCHHandler();
	static final InputHandler GAME_MENU_LOOK_HANDLER = new GameMenuLOOKHandler();
	static final InputHandler GAME_MENU_EXIT_HANDLER = new GameMenuEXITHandler();

	private static final EngineInputAction YES = new EngineInputAction(MENU_HANDLER, "YES", 0);
	private static final EngineInputAction NO = new EngineInputAction(MENU_HANDLER, "NO", 1);

	private static final EngineInputAction CONTINUE = new EngineInputAction(MENU_HANDLER, "PRESS BUTTON OR RETURN TO CONTINUE", 0);

	private static final EngineInputAction DO_SAVE = new EngineInputAction(new SaveHandler(), SAVE);

	private static final EngineInputAction MOVE_FORWARD_UP = new EngineInputAction(MOVEMENT_HANDLER, FORWARD_UP);
	private static final EngineInputAction MOVE_TURN_LEFT = new EngineInputAction(MOVEMENT_HANDLER, TURN_LEFT);
	private static final EngineInputAction MOVE_TURN_RIGHT = new EngineInputAction(MOVEMENT_HANDLER, TURN_RIGHT);
	private static final EngineInputAction TURN_AROUND_DOWN = new EngineInputAction(MOVEMENT_HANDLER, UTURN_DOWN);
	private static final EngineInputAction EXIT = new EngineInputAction(GAME_MENU_EXIT_HANDLER, "EXIT");

	public static final EngineInputAction MOVE = new EngineInputAction(GAME_MENU_MOVE_HANDLER, "MOVE");
	public static final EngineInputAction AREA = new EngineInputAction(GAME_MENU_AREA_HANDLER, "AREA");
	public static final EngineInputAction SEARCH = new EngineInputAction(GAME_MENU_SEARCH_HANDLER, "SEARCH");
	public static final EngineInputAction LOOK = new EngineInputAction(GAME_MENU_LOOK_HANDLER, "LOOK");

	public static final List<InputAction> YES_NO_ACTIONS = ImmutableList.of(YES, NO);
	public static final List<InputAction> MOVEMENT_ACTIONS = ImmutableList.of(DO_SAVE, MOVE_FORWARD_UP, MOVE_TURN_LEFT, MOVE_TURN_RIGHT,
		TURN_AROUND_DOWN, EXIT);
	public static final List<InputAction> GAME_MENU_ACTIONS = ImmutableList.of(DO_SAVE, MOVE, AREA, SEARCH, LOOK);
	public static final List<InputAction> CONTINUE_ACTION = ImmutableList.of(CONTINUE);

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
