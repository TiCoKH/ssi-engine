package engine.input;

import engine.Engine;
import engine.EngineInputAction;

public class GameMenuEXITHandler implements InputHandler {

	@Override
	public void handle(Engine engine, EngineInputAction action) {
		engine.setShowGameMenu(true);
		engine.setInputStandard(null);
	}
}
