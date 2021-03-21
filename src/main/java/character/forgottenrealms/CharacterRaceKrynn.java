package character.forgottenrealms;

import character.CharacterRace;
import shared.CustomGoldboxString;
import shared.GoldboxString;

public enum CharacterRaceKrynn implements CharacterRace {
	SILVANESTI_ELF("SILVANESTI ELF"), //
	QUALINESTI_ELF("QUALINESTI ELF"), //
	HALF_ELF("HALF-ELF"), //
	MOUNTAIN_DWARF("MOUNTAIN DWARF"), //
	HILL_DWARF("HILL DWARF"), //
	KENDER("KENDER"), //
	HUMAN("HUMAN"), //
	MONSTER("MONSTER"), //
	;

	private GoldboxString name;

	private CharacterRaceKrynn(String name) {
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

	public static CharacterRaceKrynn from(int value) {
		int index = value & 0x7;
		if (index > 7)
			index = 0;
		return values()[index];
	}
}
