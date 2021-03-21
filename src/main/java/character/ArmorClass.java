package character;

import java.util.stream.Stream;

import shared.CustomGoldboxString;
import shared.GoldboxString;

public enum ArmorClass {
	TEN(10), //
	NINE(9), //
	EIGHT(8), //
	SEVEN(7), //
	SIX(6), //
	FIVE(5), //
	FOUR(4), //
	THREE(3), //
	TWO(2), //
	ONE(1), //
	ZERP(0), //
	MINUS_ONE(-1), //
	MINUS_TWO(-2), //
	MINUS_THREE(-3), //
	MINUS_FOUR(-4), //
	MINUS_FIVE(-5), //
	MINUS_SIX(-6), //
	MINUS_SEVEN(-7), //
	MINUS_EIGHT(-8), //
	MINUS_NINE(-9), //
	MINUS_TEN(-10);

	private final GoldboxString name;
	private final int value;

	private ArmorClass(int value) {
		this.name = new CustomGoldboxString(Integer.toString(value));
		this.value = value;
	}

	public GoldboxString getName() {
		return name;
	}

	public int getValue() {
		return value;
	}

	public ArmorClass byValue(int value) {
		return Stream.of(values()) //
			.filter(ac -> ac.getValue() == value) //
			.findAny().orElse(null);
	}
}
