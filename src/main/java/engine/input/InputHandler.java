package engine.input;

import engine.Engine;
import engine.EngineInputAction;

public interface InputHandler {
	void handle(Engine engine, EngineInputAction action);
}
