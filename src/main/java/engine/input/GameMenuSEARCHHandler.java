package engine.input;

import engine.Engine;
import engine.EngineInputAction;

public class GameMenuSEARCHHandler implements InputHandler {

	@Override
	public void handle(Engine engine, EngineInputAction action) {
		engine.getMemory().setSearchFlagsToggleSearchMode();
		engine.setInputStandard(EngineInputAction.SEARCH);
	}
}
