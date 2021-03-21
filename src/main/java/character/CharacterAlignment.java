package character;

import shared.CustomGoldboxString;
import shared.GoldboxString;

public enum CharacterAlignment {
	LAWFUL_GOOD("LAWFUL GOOD", AlignmentEthics.LAWFUL, AlignmentMorals.GOOD), //
	LAWFUL_NEUTRAL("LAWFUL NEUTRAL", AlignmentEthics.LAWFUL, AlignmentMorals.NEUTRAL), //
	LAWFUL_EVIL("LAWFUL EVIL", AlignmentEthics.LAWFUL, AlignmentMorals.EVIL), //
	NEUTRAL_GOOD("NEUTRAL GOOD", AlignmentEthics.NEUTRAL, AlignmentMorals.GOOD), //
	TRUE_NEUTRAL("TRUE NEUTRAL", AlignmentEthics.NEUTRAL, AlignmentMorals.NEUTRAL), //
	NEUTRAL_EVIL("NEUTRAL EVIL", AlignmentEthics.NEUTRAL, AlignmentMorals.EVIL), //
	CHAOTIC_GOOD("CHAOTIC GOOD", AlignmentEthics.CHAOTIC, AlignmentMorals.GOOD), //
	CHAOTIC_NEUTRAL("CHAOTIC NEUTRAL", AlignmentEthics.CHAOTIC, AlignmentMorals.NEUTRAL), //
	CHAOTIC_EVIL("CHAOTIC EVIL", AlignmentEthics.CHAOTIC, AlignmentMorals.EVIL), //
	;

	private GoldboxString description;
	private AlignmentEthics ethics;
	private AlignmentMorals morals;

	private CharacterAlignment(String description, AlignmentEthics ethics, AlignmentMorals morals) {
		this.description = new CustomGoldboxString(description);
		this.ethics = ethics;
		this.morals = morals;
	}

	public GoldboxString getDescription() {
		return description;
	}

	public AlignmentEthics getEthics() {
		return ethics;
	}

	public AlignmentMorals getMorals() {
		return morals;
	}

	public int getValue() {
		return ordinal();
	}

	public static CharacterAlignment from(int value) {
		return values()[value];
	}

	public enum AlignmentEthics {
		LAWFUL, NEUTRAL, CHAOTIC;
	}

	public enum AlignmentMorals {
		GOOD, NEUTRAL, EVIL;
	}
}
