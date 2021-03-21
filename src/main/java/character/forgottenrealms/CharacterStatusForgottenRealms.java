package character.forgottenrealms;

import character.CharacterStatus;
import shared.CustomGoldboxString;
import shared.GoldboxString;

public enum CharacterStatusForgottenRealms implements CharacterStatus {
	OKAY("OKAY", 1), //
	ANIMATED("ANIMATED", 0), //
	TEMPGONE("TEMPGONE", 0), //
	RUNNING("RUNNING", 0), //
	UNCONSCIOUS("UNCONSCIOUS", 0), //
	DYING("DYING", 0), //
	DEAD("DEAD", 0), //
	STONED("STONED", 0), //
	GONE("GONE", 0);

	private GoldboxString name;
	private byte memStatus;

	private CharacterStatusForgottenRealms(String name, int memStatus) {
		this.name = new CustomGoldboxString(name);
		this.memStatus = (byte) memStatus;
	}

	@Override
	public GoldboxString getName() {
		return name;
	}

	public byte getMemStatus() {
		return memStatus;
	}

	public static CharacterStatusForgottenRealms from(int value) {
		int index = value & 0xF;
		if (index > 8)
			index = 0;
		return values()[index];
	}
}
