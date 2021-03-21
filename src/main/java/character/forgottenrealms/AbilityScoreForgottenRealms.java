package character.forgottenrealms;

import character.AbilityScore;
import shared.CustomGoldboxString;
import shared.GoldboxString;

public enum AbilityScoreForgottenRealms implements AbilityScore {
	STRENGTH("STR"), //
	DEXTERITY("DEX"), //
	CONSTITUTION("CON"), //
	WISDOM("WIS"), //
	INTELLIGENCE("INT"), //
	CHARISMA("CHA");

	private GoldboxString name;

	private AbilityScoreForgottenRealms(String name) {
		this.name = new CustomGoldboxString(name);
	}

	@Override
	public GoldboxString getName() {
		return name;
	}
}
