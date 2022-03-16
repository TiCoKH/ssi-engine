package engine.rulesystem.standard;

import static engine.rulesystem.LevelInfo.forCasters;
import static engine.rulesystem.LevelInfo.forNonCasters;

import engine.rulesystem.LevelInfo;

enum LevelData {
	CLERIC_LEVELS( //
		forCasters(0, 0, 0, 0, 0, 0, 0, 0, 0, 0), //
		forCasters(1500, 0, 0, 0, 0, 0, 0, 0, 0, 0), //
		forCasters(3000, 0, 0, 0, 0, 0, 0, 0, 0, 0), //
		forCasters(6000, 0, 0, 0, 0, 0, 0, 0, 0, 0), //
		forCasters(13000, 0, 0, 0, 0, 0, 0, 0, 0, 0), //
		forCasters(27500, 0, 0, 0, 0, 0, 0, 0, 0, 0), //
		forCasters(48000, 0, 0, 0, 0, 0, 0, 0, 0, 0), //
		forCasters(96000, 0, 0, 0, 0, 0, 0, 0, 0, 0)), //
	FIGHTER_LEVELS( //
		forNonCasters(0), //
		forNonCasters(2000), //
		forNonCasters(4000), //
		forNonCasters(8000), //
		forNonCasters(18000), //
		forNonCasters(35000), //
		forNonCasters(70000), //
		forNonCasters(125000)), //
	PALADIN_LEVELS( //
		forCasters(0, 0, 0, 0, 0, 0, 0, 0, 0, 0), //
		forCasters(2750, 0, 0, 0, 0, 0, 0, 0, 0, 0), //
		forCasters(5500, 0, 0, 0, 0, 0, 0, 0, 0, 0), //
		forCasters(12000, 0, 0, 0, 0, 0, 0, 0, 0, 0), //
		forCasters(24000, 0, 0, 0, 0, 0, 0, 0, 0, 0), //
		forCasters(45000, 0, 0, 0, 0, 0, 0, 0, 0, 0), //
		forCasters(95000, 0, 0, 0, 0, 0, 0, 0, 0, 0), //
		forCasters(175000, 0, 0, 0, 0, 0, 0, 0, 0, 0)), //
	RANGER_LEVELS( //
		forCasters(0, 0, 0, 0, 0, 0, 0, 0, 0, 0), //
		forCasters(2250, 0, 0, 0, 0, 0, 0, 0, 0, 0), //
		forCasters(4500, 0, 0, 0, 0, 0, 0, 0, 0, 0), //
		forCasters(10000, 0, 0, 0, 0, 0, 0, 0, 0, 0), //
		forCasters(20000, 0, 0, 0, 0, 0, 0, 0, 0, 0), //
		forCasters(40000, 0, 0, 0, 0, 0, 0, 0, 0, 0), //
		forCasters(90000, 0, 0, 0, 0, 0, 0, 0, 0, 0), //
		forCasters(175000, 0, 0, 0, 0, 0, 0, 0, 0, 0)), //
	MAGE_LEVELS( //
		forCasters(0, 0, 0, 0, 0, 0, 0, 0, 0, 0), //
		forCasters(2500, 0, 0, 0, 0, 0, 0, 0, 0, 0), //
		forCasters(5000, 0, 0, 0, 0, 0, 0, 0, 0, 0), //
		forCasters(10000, 0, 0, 0, 0, 0, 0, 0, 0, 0), //
		forCasters(22500, 0, 0, 0, 0, 0, 0, 0, 0, 0), //
		forCasters(40000, 0, 0, 0, 0, 0, 0, 0, 0, 0), //
		forCasters(70000, 0, 0, 0, 0, 0, 0, 0, 0, 0), //
		forCasters(150000, 0, 0, 0, 0, 0, 0, 0, 0, 0)), //
	THIEF_LEVELS( //
		forNonCasters(0), //
		forNonCasters(1250), //
		forNonCasters(2500), //
		forNonCasters(5000), //
		forNonCasters(10000), //
		forNonCasters(20000), //
		forNonCasters(42500), //
		forNonCasters(70000)), //
	;

	private LevelInfo[] levelInfo;

	private LevelData(LevelInfo... levelInfo) {
		this.levelInfo = levelInfo;
	}

	public LevelInfo forLevel(int level) {
		return levelInfo[level - 1];
	}
}
