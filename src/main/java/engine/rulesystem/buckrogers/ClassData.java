package engine.rulesystem.buckrogers;

import static engine.rulesystem.buckrogers.LevelData.ENGENEER_LEVELS;
import static engine.rulesystem.buckrogers.LevelData.MEDIC_LEVELS;
import static engine.rulesystem.buckrogers.LevelData.ROCKETJOCK_LEVELS;
import static engine.rulesystem.buckrogers.LevelData.ROGUE_LEVELS;
import static engine.rulesystem.buckrogers.LevelData.WARRIOR_LEVELS;

enum ClassData {
	ROCKETJOCK(ROCKETJOCK_LEVELS), //
	MEDIC(MEDIC_LEVELS), //
	WARRIOR(WARRIOR_LEVELS), //
	ENGINEER(ENGENEER_LEVELS), //
	ROGUE(ROGUE_LEVELS), //
	SCOUT(null), //
	;

	private LevelData levelInfo;

	private ClassData(LevelData levelInfo) {
		this.levelInfo = levelInfo;
	}

	public LevelData getLevelInfo() {
		return levelInfo;
	}
}
