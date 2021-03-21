package character;

import javax.annotation.Nonnull;

import shared.CustomGoldboxString;
import shared.GoldboxString;

public enum CharacterGender {
	MALE(new CustomGoldboxString("MALE")), //
	FEMALE(new CustomGoldboxString("FEMALE"));

	private GoldboxString name;

	private CharacterGender(@Nonnull GoldboxString name) {
		this.name = name;
	}

	public GoldboxString getName() {
		return name;
	}

	public int getValue() {
		return ordinal();
	}

	public static CharacterGender from(int value) {
		int index = value & 0x1;
		return values()[index];
	}
}
