package engine;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class InputAction {
	public static final InputAction GAME = new InputAction("GAME");
	public static final InputAction DEMO = new InputAction("DEMO");
	public static final InputAction ACCEPT = new InputAction("RETURN/ENTER");
	public static final InputAction MOVE_FORWARD = new InputAction("Forward");
	public static final InputAction TURN_LEFT = new InputAction("Left");
	public static final InputAction TURN_RIGHT = new InputAction("Right");
	public static final InputAction TURN_AROUND = new InputAction("U-Turn");

	public static final List<InputAction> STANDARD_ACTIONS = Collections
		.unmodifiableList(Arrays.asList(new InputAction[] { MOVE_FORWARD, TURN_LEFT, TURN_RIGHT, TURN_AROUND }));
	public static final List<InputAction> MAINMENU_ACTIONS = Collections.unmodifiableList(Arrays.asList(new InputAction[] { GAME, DEMO }));
	public static final List<InputAction> RETURN_ACTIONS = Collections.unmodifiableList(Arrays.asList(new InputAction[] { ACCEPT }));

	private final String name;

	public InputAction(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
