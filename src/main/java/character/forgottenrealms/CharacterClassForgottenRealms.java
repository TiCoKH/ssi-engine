package character.forgottenrealms;

import character.CharacterClass;
import shared.CustomGoldboxString;
import shared.GoldboxString;

public enum CharacterClassForgottenRealms implements CharacterClass {
	NONE("-"), //
	CLERIC("CLERIC"), //
	DRUID("DRUID"), //
	FIGHTER("FIGHTER"), //
	PALADIN("PALADIN"), //
	RANGER("RANGER"), //
	MAGE("MAGE"), //
	THIEF("THIEF"), //
	MONK("MONK"), //
	KNIGHT("KNIGHT"), //
	;

	private GoldboxString name;

	private CharacterClassForgottenRealms(String name) {
		this.name = new CustomGoldboxString(name);
	}

	@Override
	public GoldboxString getName() {
		return name;
	}
}
