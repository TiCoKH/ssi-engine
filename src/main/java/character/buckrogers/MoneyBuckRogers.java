package character.buckrogers;

import character.Money;
import shared.CustomGoldboxString;
import shared.GoldboxString;

public enum MoneyBuckRogers implements Money {
	CREDITS("CREDITS");

	private GoldboxString name;

	private MoneyBuckRogers(String name) {
		this.name = new CustomGoldboxString(name);
	}

	@Override
	public GoldboxString getName() {
		return name;
	}
}
