package engine.input;

import engine.Engine;
import engine.EngineInputAction;

public class DialogBackHandler implements InputHandler {

	@Override
	public void handle(Engine engine, EngineInputAction action) {
		engine.getUi().clearCurrentDialog();
	}
}
