package engine.input;

import engine.Engine;
import engine.EngineInputAction;

public class ContinueHandler implements InputHandler {

	@Override
	public void handle(Engine engine, EngineInputAction action) {
		engine.getUi().setInputNone();
		engine.continueCurrentThread();
	}
}
