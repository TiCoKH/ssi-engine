package engine.input;

import engine.Engine;
import engine.EngineInputAction;

public class GameMenuMOVEHandler implements InputHandler {

	@Override
	public void handle(Engine engine, EngineInputAction action) {
		engine.setShowGameMenu(false);
		engine.setInputStandard(EngineInputAction.MOVE);
	}
}
