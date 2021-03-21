package character.buckrogers;

import character.CharacterRace;
import shared.CustomGoldboxString;
import shared.GoldboxString;

public enum CharacterRaceBuckRogers implements CharacterRace {
	MONSTER("MONSTER"), //
	TERRAN("TERRAN"), //
	MARTIAN("MARTIAN"), //
	VENUSIAN("VENUSIAN"), //
	MERCURIAN("MERCURIAN"), //
	TINKER("TINKER"), //
	DESERT_RUNNER("DESERT RUNNER"), //
	LUNARIAN("LUNARIAN"), //
	LOWLANDER("LOWLANDER");

	private GoldboxString name;

	private CharacterRaceBuckRogers(String name) {
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

	public static CharacterRaceBuckRogers from(int value) {
		int index = value & 0xF;
		if (index > 8)
			index = 0;
		return values()[index];
	}
}
