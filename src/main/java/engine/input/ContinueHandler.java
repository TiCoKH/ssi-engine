package engine.input;

import engine.Engine;
import engine.InputAction;

public class ContinueHandler implements InputHandler {

	@Override
	public void handle(Engine engine, InputAction action) {
		engine.getUi().setInputNone();
		engine.continueCurrentThread();
	}
}
