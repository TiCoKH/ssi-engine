package engine.input;

import engine.Engine;
import engine.EngineInputAction;

public class MenuHandler implements InputHandler {

	@Override
	public void handle(Engine engine, EngineInputAction action) {
		engine.getUi().setInputNone();
		engine.getMemory().setMenuChoice(action.getIndex());
		engine.continueCurrentThread();
	}
}
