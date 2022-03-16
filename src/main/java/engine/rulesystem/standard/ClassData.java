package engine.rulesystem.standard;

import static character.CharacterAlignment.CHAOTIC_EVIL;
import static character.CharacterAlignment.CHAOTIC_NEUTRAL;
import static character.CharacterAlignment.LAWFUL_EVIL;
import static character.CharacterAlignment.LAWFUL_GOOD;
import static character.CharacterAlignment.LAWFUL_NEUTRAL;
import static character.CharacterAlignment.NEUTRAL_EVIL;
import static character.CharacterAlignment.NEUTRAL_GOOD;
import static character.CharacterAlignment.TRUE_NEUTRAL;
import static engine.rulesystem.standard.LevelData.CLERIC_LEVELS;
import static engine.rulesystem.standard.LevelData.FIGHTER_LEVELS;
import static engine.rulesystem.standard.LevelData.MAGE_LEVELS;
import static engine.rulesystem.standard.LevelData.PALADIN_LEVELS;
import static engine.rulesystem.standard.LevelData.RANGER_LEVELS;
import static engine.rulesystem.standard.LevelData.THIEF_LEVELS;

import character.CharacterAlignment;
import character.CharacterClass;
import character.forgottenrealms.CharacterClassForgottenRealms;

enum ClassData {
	CLERIC(CLERIC_LEVELS), //
	DRUID(null), //
	FIGHTER(FIGHTER_LEVELS), //
	PALADIN(PALADIN_LEVELS, LAWFUL_GOOD), //
	RANGER(RANGER_LEVELS, LAWFUL_GOOD, LAWFUL_NEUTRAL, LAWFUL_EVIL), //
	MAGE(MAGE_LEVELS), //
	THIEF(THIEF_LEVELS, LAWFUL_NEUTRAL, LAWFUL_EVIL, NEUTRAL_GOOD, TRUE_NEUTRAL, NEUTRAL_EVIL, CHAOTIC_NEUTRAL, CHAOTIC_EVIL), //
	MONK(null), //
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
