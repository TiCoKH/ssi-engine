package engine.input;

import engine.Engine;
import engine.InputAction;

public class QuitHandler implements InputHandler {

	@Override
	public void handle(Engine engine, InputAction action) {
		engine.stop();
		System.exit(0);
	}
}
