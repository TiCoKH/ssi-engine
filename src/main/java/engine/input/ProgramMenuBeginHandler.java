package engine.input;

import engine.Engine;
import engine.EngineInputAction;

public class ProgramMenuBeginHandler implements InputHandler {

	@Override
	public void handle(Engine engine, EngineInputAction action) {
		engine.getUi().clearAllDialogs();
		engine.loadEcl(false);
	}
}
