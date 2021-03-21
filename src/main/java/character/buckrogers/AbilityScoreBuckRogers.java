package character.buckrogers;

import character.AbilityScore;
import shared.CustomGoldboxString;
import shared.GoldboxString;

public enum AbilityScoreBuckRogers implements AbilityScore {
	STRENGTH("STR"), //
	DEXTERITY("DEX"), //
	CONSTITUTION("CON"), //
	WISDOM("WIS"), //
	INTELLIGENCE("INT"), //
	CHARISMA("CHA"), //
	TECH("TCH");

	private GoldboxString name;

	private AbilityScoreBuckRogers(String name) {
		this.name = new CustomGoldboxString(name);
	}

	@Override
	public GoldboxString getName() {
		return name;
	}
}
