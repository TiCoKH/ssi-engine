package engine;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import engine.input.ContinueHandler;
import engine.input.DungeonMovementHandler;
import engine.input.InputHandler;
import engine.input.InputNumberStringHandler;
import engine.input.LoadHandler;
import engine.input.MenuHandler;
import engine.input.OverlandMovementHandler;
import engine.input.SaveHandler;
import engine.input.SpaceMovementHandler;
import types.CustomGoldboxString;
import types.GoldboxString;

public class InputAction {
	private static final InputHandler DUNGEON_MOVEMENT_HANDLER = new DungeonMovementHandler();
	private static final InputHandler OVERLAND_MOVEMENT_HANDLER = new OverlandMovementHandler();
	private static final InputHandler SPACE_MOVEMENT_HANDLER = new SpaceMovementHandler();

	public static final InputHandler CONTINUE_HANDLER = new ContinueHandler();
	public static final InputHandler MENU_HANDLER = new MenuHandler();
	public static final InputHandler INPUT_HANDLER = new InputNumberStringHandler();

	private static final InputAction YES = new InputAction(MENU_HANDLER, "YES", 0);
	private static final InputAction NO = new InputAction(MENU_HANDLER, "NO", 1);

	public static final InputAction CONTINUE = new InputAction(CONTINUE_HANDLER, "PRESS BUTTON OR RETURN TO CONTINUE", 0);

	public static final InputAction LOAD = new InputAction(new LoadHandler());
	public static final InputAction SAVE = new InputAction(new SaveHandler());

	public static final InputAction MOVE_FORWARD = new InputAction(DUNGEON_MOVEMENT_HANDLER);
	public static final InputAction TURN_LEFT = new InputAction(DUNGEON_MOVEMENT_HANDLER);
	public static final InputAction TURN_RIGHT = new InputAction(DUNGEON_MOVEMENT_HANDLER);
	public static final InputAction TURN_AROUND = new InputAction(DUNGEON_MOVEMENT_HANDLER);

	public static final InputAction MOVE_OVERLAND_UP = new InputAction(OVERLAND_MOVEMENT_HANDLER);
	public static final InputAction MOVE_OVERLAND_LEFT = new InputAction(OVERLAND_MOVEMENT_HANDLER);
	public static final InputAction MOVE_OVERLAND_RIGHT = new InputAction(OVERLAND_MOVEMENT_HANDLER);
	public static final InputAction MOVE_OVERLAND_DOWN = new InputAction(OVERLAND_MOVEMENT_HANDLER);

	public static final InputAction MOVE_SPACE_UP = new InputAction(SPACE_MOVEMENT_HANDLER);
	public static final InputAction MOVE_SPACE_LEFT = new InputAction(SPACE_MOVEMENT_HANDLER);
	public static final InputAction MOVE_SPACE_RIGHT = new InputAction(SPACE_MOVEMENT_HANDLER);
	public static final InputAction MOVE_SPACE_DOWN = new InputAction(SPACE_MOVEMENT_HANDLER);

	public static final List<InputAction> YES_NO_ACTIONS = ImmutableList.of(YES, NO);
	public static final List<InputAction> DUNGEON_MOVEMENT = ImmutableList.of(MOVE_FORWARD, TURN_LEFT, TURN_RIGHT, TURN_AROUND, SAVE);
	public static final List<InputAction> OVERLAND_MOVEMENT = ImmutableList.of(MOVE_OVERLAND_UP, MOVE_OVERLAND_LEFT, MOVE_OVERLAND_RIGHT,
		MOVE_OVERLAND_DOWN, SAVE);
	public static final List<InputAction> SPACE_MOVEMENT = ImmutableList.of(MOVE_SPACE_UP, MOVE_SPACE_LEFT, MOVE_SPACE_RIGHT, MOVE_SPACE_DOWN, SAVE);
	public static final List<InputAction> CONTINUE_ACTION = ImmutableList.of(CONTINUE);

	private final InputHandler handler;
	private final Optional<GoldboxString> name;
	private final int index;

	public InputAction(@Nonnull InputHandler handler) {
		this(handler, (GoldboxString) null, -1);
	}

	public InputAction(@Nonnull InputHandler handler, @Nullable String name, int index) {
		this(handler, name != null ? new CustomGoldboxString(name) : null, index);
	}

	public InputAction(@Nonnull InputHandler handler, @Nullable GoldboxString name, int index) {
		this.handler = handler;
		this.name = Optional.ofNullable(name);
		this.index = index;
	}

	public InputHandler getHandler() {
		return handler;
	}

	public Optional<GoldboxString> getName() {
		return name;
	}

	public int getIndex() {
		return index;
	}
}
