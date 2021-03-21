package character.forgottenrealms;

import character.CharacterRace;
import shared.CustomGoldboxString;
import shared.GoldboxString;

public enum CharacterRaceStandard implements CharacterRace {
	MONSTER("MONSTER"), //
	DWARF("DWARF"), //
	ELF("ELF"), //
	GNOME("GNOME"), //
	HALF_ELF("HALF-ELF"), //
	HALFLING("HALFLING"), //
	HALF_ORC("HALF-ORC"), //
	HUMAN("HUMAN"), //
	;

	private GoldboxString name;

	private CharacterRaceStandard(String name) {
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

	public static CharacterRaceStandard from(int value) {
		int index = value & 0x7;
		if (index > 7)
			index = 0;
		return values()[index];
	}
}
