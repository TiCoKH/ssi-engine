package engine.input;

import engine.Engine;
import engine.EngineConfiguration;
import engine.EngineInputAction;
import engine.character.CharacterCreator;
import engine.character.PlayerDataFactory;
import shared.UserInterface;

public class ProgramMenuCreateCharacterHandler implements InputHandler {

	@Override
	public void handle(Engine engine, EngineInputAction action) {
		final EngineConfiguration cfg = engine.getConfig();
		final PlayerDataFactory factory = engine.getPlayerDataFactory();
		final UserInterface ui = engine.getUi();
		final CharacterCreator creator = new CharacterCreator(cfg, factory, ui, engine::setNextTask);
		creator.start();
	}
}
