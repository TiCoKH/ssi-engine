package engine.input;

import engine.Engine;
import engine.EngineInputAction;
import engine.character.CharacterCreator;
import engine.character.PlayerDataFactory;
import engine.rulesystem.Flavor;
import shared.UserInterface;

public class ProgramMenuCreateCharacterHandler implements InputHandler {

	@Override
	public void handle(Engine engine, EngineInputAction action) {
		final Flavor flavor = engine.getConfig().getFlavor();
		final PlayerDataFactory factory = engine.getPlayerDataFactory();
		final UserInterface ui = engine.getUi();
		final CharacterCreator creator = new CharacterCreator(flavor, factory, ui, engine::setNextTask);
		creator.start();
	}
}
