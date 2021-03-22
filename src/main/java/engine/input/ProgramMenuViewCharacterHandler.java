package engine.input;

import static engine.EngineInputAction.CHAR_SHEET_ACTION;

import engine.Engine;
import engine.EngineInputAction;
import engine.VirtualMemory;
import engine.character.CharacterSheetImpl;

public class ProgramMenuViewCharacterHandler implements InputHandler {

	@Override
	public void handle(Engine engine, EngineInputAction action) {
		engine.setNextTask(() -> {
			final VirtualMemory mem = engine.getMemory();
			final CharacterSheetImpl sheet = mem.getPartyMemberAsCharacterSheet(mem.getSelectedPartyMember());
			engine.getUi().showCharacterSheet(sheet, CHAR_SHEET_ACTION);
		});
	}
}
