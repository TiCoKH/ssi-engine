package engine.input;

import engine.Engine;
import engine.InputAction;

public interface InputHandler {
	void handle(Engine engine, InputAction action);
}
