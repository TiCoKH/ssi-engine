package character.forgottenrealms;

import character.Money;
import shared.CustomGoldboxString;
import shared.GoldboxString;

public enum MoneyForgottenRealms implements Money {
	COPPER("COPPER"), //
	BRONZE("BRONZE"), //
	SILVER("SILVER"), //
	ELEKTRUM("ELEKTRUM"), //
	STEEL("STEEL"), //
	GOLD("GOLD"), //
	PLATINUM("PLATINUM"), //
	GEM("GEM"), //
	JEWELRY("JEWELRY"), //
	;

	private GoldboxString name;

	private MoneyForgottenRealms(String name) {
		this.name = new CustomGoldboxString(name);
	}

	@Override
	public GoldboxString getName() {
		return name;
	}
}
