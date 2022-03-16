package engine.rulesystem.krynn;

import static character.CharacterAlignment.CHAOTIC_GOOD;
import static character.CharacterAlignment.CHAOTIC_NEUTRAL;
import static character.CharacterAlignment.LAWFUL_GOOD;
import static character.CharacterAlignment.LAWFUL_NEUTRAL;
import static character.CharacterAlignment.NEUTRAL_GOOD;
import static character.CharacterAlignment.TRUE_NEUTRAL;
import static engine.rulesystem.krynn.LevelData.CLERIC_LEVELS;
import static engine.rulesystem.krynn.LevelData.DRUID_LEVELS;
import static engine.rulesystem.krynn.LevelData.FIGHTER_LEVELS;
import static engine.rulesystem.krynn.LevelData.KNIGHT_LEVELS;
import static engine.rulesystem.krynn.LevelData.MAGE_LEVELS;
import static engine.rulesystem.krynn.LevelData.PALADIN_LEVELS;
import static engine.rulesystem.krynn.LevelData.RANGER_LEVELS;
import static engine.rulesystem.krynn.LevelData.THIEF_LEVELS;

import character.CharacterAlignment;
import character.CharacterClass;
import character.forgottenrealms.CharacterClassForgottenRealms;

enum ClassData {
	CLERIC(CLERIC_LEVELS), //
	DRUID(DRUID_LEVELS), //
	FIGHTER(FIGHTER_LEVELS), //
	PALADIN(PALADIN_LEVELS, LAWFUL_GOOD), //
	RANGER(RANGER_LEVELS, LAWFUL_GOOD, NEUTRAL_GOOD, CHAOTIC_GOOD), //
	MAGE(MAGE_LEVELS), //
	THIEF(THIEF_LEVELS, LAWFUL_NEUTRAL, NEUTRAL_GOOD, TRUE_NEUTRAL, CHAOTIC_NEUTRAL), //
	KNIGHT(KNIGHT_LEVELS, LAWFUL_GOOD), //
	;

	private LevelData levelInfo;
	private CharacterAlignment[] allowedAlignments;

	private ClassData(LevelData levelInfo, CharacterAlignment... allowedAlignments) {
		this.levelInfo = levelInfo;
		this.allowedAlignments = allowedAlignments == null || allowedAlignments.length == 0 ? CharacterAlignment.values() : allowedAlignments;
	}

	public LevelData getLevelInfo() {
		return levelInfo;
	}

	public CharacterAlignment[] getAllowedAlignments() {
		return allowedAlignments;
	}

	public static ClassData forClass(CharacterClass clazz) {
		if (!(clazz instanceof CharacterClassForgottenRealms)) {
			throw new IllegalArgumentException("clazz not of type CharacterClassForgottenRealms");
		}
		return valueOf(((CharacterClassForgottenRealms) clazz).name());
	}
}
