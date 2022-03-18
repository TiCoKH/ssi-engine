package engine.rulesystem.buckrogers;

import static engine.rulesystem.LevelInfo.forNonCasters;

import io.vavr.collection.Array;

import engine.rulesystem.LevelInfo;

enum LevelData {
	ROCKETJOCK_LEVELS( //
		forNonCasters(0), //
		forNonCasters(1250), //
		forNonCasters(2500), //
		forNonCasters(5000), //
		forNonCasters(10000), //
		forNonCasters(20000), //
		forNonCasters(40000), //
		forNonCasters(70000)), //
	WARRIOR_LEVELS( //
		forNonCasters(0), //
		forNonCasters(2000), //
		forNonCasters(4000), //
		forNonCasters(8000), //
		forNonCasters(16000), //
		forNonCasters(32000), //
		forNonCasters(64000), //
		forNonCasters(125000)), //
	ENGENEER_LEVELS( //
		forNonCasters(0), //
		forNonCasters(1250), //
		forNonCasters(2500), //
		forNonCasters(5000), //
		forNonCasters(10000), //
		forNonCasters(20000), //
		forNonCasters(40000), //
		forNonCasters(70000)), //
	ROGUE_LEVELS( //
		forNonCasters(0), //
		forNonCasters(1250), //
		forNonCasters(2500), //
		forNonCasters(5000), //
		forNonCasters(10000), //
		forNonCasters(20000), //
		forNonCasters(40000), //
		forNonCasters(70000)), //
	MEDIC_LEVELS( //
		forNonCasters(0), //
		forNonCasters(1500), //
		forNonCasters(3000), //
		forNonCasters(6000), //
		forNonCasters(12000), //
		forNonCasters(24000), //
		forNonCasters(48000), //
		forNonCasters(96000)), //
	;

	private LevelInfo[] levelInfo;

	private LevelData(LevelInfo... levelInfo) {
		this.levelInfo = levelInfo;
	}

	public LevelInfo forLevel(int level) {
		return levelInfo[level - 1];
	}

	public int levelFor(int exp) {
		final Array<LevelInfo> levels = Array.of(levelInfo);
		return levels.findLast(info -> info.getExpNeeded() <= exp).map(levels::indexOf).map(index -> index + 1)
			.getOrElseThrow(IllegalArgumentException::new);
	}
}
