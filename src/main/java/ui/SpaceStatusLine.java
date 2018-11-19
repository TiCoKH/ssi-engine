package ui;

import java.util.Optional;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;

import engine.ViewSpacePosition;
import engine.opcodes.EclString;

public class SpaceStatusLine extends StatusLine {
	private ViewSpacePosition position;
	private int fuel = -1;

	public SpaceStatusLine(@Nonnull ViewSpacePosition position) {
		super(Optional.of(createFuelText(position.getFuel())), FontType.FUEL, Optional.of(ImmutableList.of("EXIT")));
		this.position = position;
		fuel = position.getFuel();
	}

	@Override
	public Optional<EclString> getText() {
		if (fuel != position.getFuel()) {
			fuel = position.getFuel();
			setText(createFuelText(fuel));
		}
		return super.getText();
	}

	private static String createFuelText(int fuel) {
		return "EN ROUTE. FUEL = " + fuel;
	}
}
