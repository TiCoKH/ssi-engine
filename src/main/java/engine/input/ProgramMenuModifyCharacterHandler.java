package engine.input;

import engine.Engine;
import engine.EngineInputAction;

public class ProgramMenuModifyCharacterHandler implements InputHandler {

	@Override
	public void handle(Engine engine, EngineInputAction action) {
		engine.setNextTask(engine::showProgramMenu);
	}
}
