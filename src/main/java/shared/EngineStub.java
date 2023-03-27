package shared;

import data.Resource;
import shared.party.CharacterSheet;

public interface EngineStub {
	void registerUI(UserInterface ui);

	void deregisterUI(UserInterface ui);

	void start();

	void stop();

	void loadGame();

	void showModeMenu();

	void textDisplayFinished();

	void handleInput(InputAction action);

	void handleInput(String input);

	Resource<CharacterSheet> readCharacter(int id);
}
