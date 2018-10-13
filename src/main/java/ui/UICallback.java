package ui;

import engine.InputAction;

public interface UICallback {
	void textDisplayFinished();

	void handleInput(InputAction action);
}
