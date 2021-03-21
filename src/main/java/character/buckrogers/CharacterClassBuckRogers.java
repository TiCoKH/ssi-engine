package character.buckrogers;

import character.CharacterClass;
import shared.CustomGoldboxString;
import shared.GoldboxString;

public enum CharacterClassBuckRogers implements CharacterClass {
	NONE("-"), //
	ROCKETJOCK("ROCKETJOCK"), //
	MEDIC("MEDIC"), //
	WARRIOR("WARRIOR"), //
	ENGINEER("ENGINEER"), //
	ROGUE("ROGUE"), //
	SCOUT("SCOUT");

	private GoldboxString name;

	private CharacterClassBuckRogers(String name) {
		this.name = new CustomGoldboxString(name);
	}

	@Override
	public GoldboxString getName() {
		return name;
	}

	public static CharacterClassBuckRogers from(int value) {
		int index = value & 0x7;
		if (index > 6)
			index = 0;
		return values()[index];
	}
}
