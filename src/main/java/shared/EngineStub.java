package shared;

public interface EngineStub {
	void registerUI(UserInterface ui);

	void deregisterUI(UserInterface ui);

	void start();

	void stop();

	void loadGame();

	void showStartMenu();

	void textDisplayFinished();

	void handleInput(InputAction action);

	void handleInput(String input);
}
