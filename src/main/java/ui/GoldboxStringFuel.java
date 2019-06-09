package ui;

import javax.annotation.Nonnull;

import shared.CustomGoldboxString;
import shared.ViewSpacePosition;

public class GoldboxStringFuel extends CustomGoldboxString {
	private ViewSpacePosition position;
	private int fuel = -1;

	public GoldboxStringFuel(@Nonnull ViewSpacePosition position) {
		super(createFuelText(position.getFuel()));
		this.position = position;
		fuel = position.getFuel();
	}

	@Override
	public byte getChar(int index) {
		if (index == 0 && fuel != position.getFuel()) {
			fuel = position.getFuel();
			setText(createFuelText(fuel));
		}
		return super.getChar(index);
	}

	private static String createFuelText(int fuel) {
		return "EN ROUTE. FUEL = " + fuel;
	}
}
