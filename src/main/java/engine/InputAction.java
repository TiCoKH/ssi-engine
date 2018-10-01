package engine;

import java.util.List;

import com.google.common.collect.ImmutableList;

import engine.input.ContinueHandler;
import engine.input.DungeonMovementHandler;
import engine.input.InputHandler;
import engine.input.MenuHandler;
import engine.input.QuitHandler;

public class InputAction {
	private static final InputHandler MOVEMENT_HANDLER = new DungeonMovementHandler();

	public static final InputHandler MENU_HANDLER = new MenuHandler();

	private static final InputAction GAME = new InputAction(MENU_HANDLER, "GAME", 0);
	private static final InputAction DEMO = new InputAction(MENU_HANDLER, "DEMO", 1);

	private static final InputAction YES = new InputAction(MENU_HANDLER, "YES", 0);
	private static final InputAction NO = new InputAction(MENU_HANDLER, "NO", 1);

	public static final InputAction CONTINUE = new InputAction(new ContinueHandler());

	public static final InputAction QUIT = new InputAction(new QuitHandler());

	public static final InputAction MOVE_FORWARD = new InputAction(MOVEMENT_HANDLER);
	public static final InputAction TURN_LEFT = new InputAction(MOVEMENT_HANDLER);
	public static final InputAction TURN_RIGHT = new InputAction(MOVEMENT_HANDLER);
	public static final InputAction TURN_AROUND = new InputAction(MOVEMENT_HANDLER);

	public static final List<InputAction> MAINMENU_ACTIONS = ImmutableList.of(GAME, DEMO);
	public static final List<InputAction> YES_NO_ACTIONS = ImmutableList.of(YES, NO);
	public static final List<InputAction> STANDARD_ACTIONS = ImmutableList.of(MOVE_FORWARD, TURN_LEFT, TURN_RIGHT, TURN_AROUND);

	private final InputHandler handler;
	private final String name;
	private final int index;

	public InputAction(InputHandler handler) {
		this(handler, "", -1);
	}

	public InputAction(InputHandler handler, String name, int index) {
		this.handler = handler;
		this.name = name;
		this.index = index;
	}

	public InputHandler getHandler() {
		return handler;
	}

	public String getName() {
		return name;
	}

	public int getIndex() {
		return index;
	}
}
