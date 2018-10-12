package engine.input;

import engine.Engine;
import engine.InputAction;

public class MenuHandler implements InputHandler {

	@Override
	public void handle(Engine engine, InputAction action) {
		engine.getUi().setInputNone();
		engine.getMemory().setMenuChoice(action.getIndex());
		engine.continueCurrentThread();
	}
}
