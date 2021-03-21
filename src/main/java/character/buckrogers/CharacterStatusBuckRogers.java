package character.buckrogers;

import character.CharacterStatus;
import shared.CustomGoldboxString;
import shared.GoldboxString;

public enum CharacterStatusBuckRogers implements CharacterStatus {
	NONE("-"), //
	OKAY("OKAY"), //
	GONE("GONE"), //
	DEAD("DEAD"), //
	DYING("DYING"), //
	UNCONSCIOUS("UNCONSCIOUS"), //
	FLEEING("FLEEING"), //
	POISONED("POISONED"), //
	COMATOSE("COMATOSE"), //
	NOT_HERE("NOT HERE"), //
	DODGING("DODGING"), //
	SPRINTING("SPRINTING"), //
	FAILED_ZEROG("FAILED ZEROG"), //
	USED_JETPACK("USED JETPACK"), //
	JETPACK("JETPACK");

	private GoldboxString name;

	private CharacterStatusBuckRogers(String name) {
		this.name = new CustomGoldboxString(name);
	}

	@Override
	public GoldboxString getName() {
		return name;
	}

	public static CharacterStatusBuckRogers from(int value) {
		int index = value & 0xF;
		if (index > 14)
			index = 0;
		return values()[index];
	}

}
