package engine.rulesystem;

public class LevelInfo {

	private int expNeeded;
	private int availableLvl1Spells;
	private int availableLvl2Spells;
	private int availableLvl3Spells;
	private int availableLvl4Spells;
	private int availableLvl5Spells;
	private int availableLvl6Spells;
	private int availableLvl7Spells;
	private int availableLvl8Spells;
	private int availableLvl9Spells;

	private LevelInfo(int expNeeded, int availableLvl1Spells, int availableLvl2Spells, int availableLvl3Spells, int availableLvl4Spells,
		int availableLvl5Spells, int availableLvl6Spells, int availableLvl7Spells, int availableLvl8Spells, int availableLvl9Spells) {
		this.expNeeded = expNeeded;
		this.availableLvl1Spells = availableLvl1Spells;
		this.availableLvl2Spells = availableLvl2Spells;
		this.availableLvl3Spells = availableLvl3Spells;
		this.availableLvl4Spells = availableLvl4Spells;
		this.availableLvl5Spells = availableLvl5Spells;
		this.availableLvl6Spells = availableLvl6Spells;
		this.availableLvl7Spells = availableLvl7Spells;
		this.availableLvl8Spells = availableLvl8Spells;
		this.availableLvl9Spells = availableLvl9Spells;
	}

	public static LevelInfo forNonCasters(int expNeeded) {
		return new LevelInfo(expNeeded, 0, 0, 0, 0, 0, 0, 0, 0, 0);
	}

	public static LevelInfo forCasters(int expNeeded, int availableLvl1Spells, int availableLvl2Spells, int availableLvl3Spells,
		int availableLvl4Spells, int availableLvl5Spells, int availableLvl6Spells, int availableLvl7Spells, int availableLvl8Spells,
		int availableLvl9Spells) {

		return new LevelInfo(expNeeded, availableLvl1Spells, availableLvl2Spells, availableLvl3Spells, availableLvl4Spells, availableLvl5Spells,
			availableLvl6Spells, availableLvl7Spells, availableLvl8Spells, availableLvl9Spells);
	}

	public int getTHACO() {
		return 0;
	}

	public int getExpNeeded() {
		return expNeeded;
	}

	public int getAvailableLvl1Spells() {
		return availableLvl1Spells;
	}

	public int getAvailableLvl2Spells() {
		return availableLvl2Spells;
	}

	public int getAvailableLvl3Spells() {
		return availableLvl3Spells;
	}

	public int getAvailableLvl4Spells() {
		return availableLvl4Spells;
	}

	public int getAvailableLvl5Spells() {
		return availableLvl5Spells;
	}

	public int getAvailableLvl6Spells() {
		return availableLvl6Spells;
	}

	public int getAvailableLvl7Spells() {
		return availableLvl7Spells;
	}

	public int getAvailableLvl8Spells() {
		return availableLvl8Spells;
	}

	public int getAvailableLvl9Spells() {
		return availableLvl9Spells;
	}
}
