package engine.input;

import engine.Engine;
import engine.EngineInputAction;

public class GameMenuAREAHandler implements InputHandler {

	@Override
	public void handle(Engine engine, EngineInputAction action) {
		engine.getUi().switchDungeonAreaMap();
		engine.setInputStandard(EngineInputAction.AREA);
	}
}
