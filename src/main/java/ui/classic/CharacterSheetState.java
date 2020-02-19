package ui.classic;

import javax.annotation.Nonnull;
import javax.swing.ActionMap;
import javax.swing.InputMap;

import shared.party.CharacterSheet;
import ui.shared.Menu;

public class CharacterSheetState extends AbstractDialogState {

	private final CharacterSheet sheet;

	public CharacterSheetState(@Nonnull CharacterSheet sheet, @Nonnull Menu horizontalMenu, @Nonnull ActionMap actionMap,
		@Nonnull InputMap inputMap) {

		super(horizontalMenu, actionMap, inputMap);
		this.sheet = sheet;
	}

	@Override
	public Class<? extends AbstractDialogRenderer> getRendererClass() {
		return CharacterSheetRenderer.class;
	}

	public CharacterSheet getSheet() {
		return sheet;
	}
}
