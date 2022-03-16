package engine.rulesystem.buckrogers;

import static engine.rulesystem.buckrogers.LevelData.ENGENEER_LEVELS;
import static engine.rulesystem.buckrogers.LevelData.MEDIC_LEVELS;
import static engine.rulesystem.buckrogers.LevelData.ROCKETJOCK_LEVELS;
import static engine.rulesystem.buckrogers.LevelData.ROGUE_LEVELS;
import static engine.rulesystem.buckrogers.LevelData.WARRIOR_LEVELS;

import character.CharacterClass;
import character.buckrogers.CharacterClassBuckRogers;

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

	public static ClassData forClass(CharacterClass clazz) {
		if (!(clazz instanceof CharacterClassBuckRogers)) {
			throw new IllegalArgumentException("clazz not of type CharacterBuckRogers");
		}
		return valueOf(((CharacterClassBuckRogers) clazz).name());
	}
}
