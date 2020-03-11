package engine.rulesystem;

import java.util.Random;
import java.util.stream.IntStream;

public enum Dice {
	D4(4), //
	D6(6), //
	D8(8), //
	D10(10), //
	D12(12), //
	D20(20), //
	D100(100);

	private static final Random rnd = new Random();
	private int sides;

	private Dice(int sides) {
		this.sides = sides;
	}

	public int roll() {
		return 1 + rnd.nextInt(sides);
	}

	public int roll(int count) {
		return IntStream.range(0, count).map(i -> roll()).sum();
	}

	public int rollIgnoreLowest(int count) {
		return IntStream.range(0, count).map(i -> roll()).sorted().skip(1).sum();
	}

	public int max() {
		return sides;
	}
}
