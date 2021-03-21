package character.forgottenrealms;

import character.CharacterRace;
import shared.CustomGoldboxString;
import shared.GoldboxString;

public enum CharacterRaceStandardSotSB implements CharacterRace {
	TRIBBLE("TRIBBLE"), //
	ELF("ELF"), //
	HALF_ELF("HALF-ELF"), //
	DWARF("DWARF"), //
	GNOME("GNOME"), //
	HALFLING("HALFLING"), //
	HUMAN("HUMAN"), //
	MONSTER("MONSTER"), //
	;

	private GoldboxString name;

	private CharacterRaceStandardSotSB(String name) {
		this.name = new CustomGoldboxString(name);
	}

	@Override
	public GoldboxString getName() {
		return name;
	}

	@Override
	public int getValue() {
		return ordinal();
	}

	public static CharacterRaceStandardSotSB from(int value) {
		int index = value & 0x7;
		if (index > 7)
			index = 0;
		return values()[index];
	}
}
