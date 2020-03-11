package engine.rulesystem;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class DiceTest {

	@Test
	public void testRoll() {
		for (int i = 0; i < 100000; i++) {
			int value = Dice.D6.roll();
			assertTrue(value >= 1);
			assertTrue(value <= 6);
		}
	}

	@Test
	public void testRollCount() {
		for (int i = 0; i < 100000; i++) {
			int value = Dice.D6.roll(3);
			assertTrue(value >= 1);
			assertTrue(value <= 18);
		}
	}

	@Test
	public void testRollIgnoreLowest() {
		for (int i = 0; i < 100000; i++) {
			int value = Dice.D6.rollIgnoreLowest(4);
			assertTrue(value >= 1);
			assertTrue(value <= 18);
		}
	}
}
