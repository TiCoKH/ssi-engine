package shared;

import engine.InputAction;

public interface EngineStub {
	void registerUI(UserInterface ui);

	void deregisterUI(UserInterface ui);

	void start();

	void stop();

	void showStartMenu();

	void textDisplayFinished();

	void handleInput(InputAction action);
}
