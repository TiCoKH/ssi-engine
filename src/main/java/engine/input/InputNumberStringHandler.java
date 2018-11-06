package engine.input;

import engine.Engine;
import engine.InputAction;

public class InputNumberStringHandler implements InputHandler {

	@Override
	public void handle(Engine engine, InputAction action) {
		engine.getMemory().setInput(action.getName());
		engine.getUi().setInputNone();
		engine.continueCurrentThread();
	}
}
