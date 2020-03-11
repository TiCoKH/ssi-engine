package data.character;

import static data.character.CharacterValue.value;
import static data.character.CharacterValues.NameFormat.NAME_LENGTH_NAME;
import static data.character.CharacterValues.NameFormat.NAME_STRING_TERMINATOR;
import static data.character.CharacterValues.RaceType.BUCK_ROGERS_RACES;
import static data.character.CharacterValues.RaceType.KRYNN_RACES;
import static data.character.CharacterValues.RaceType.SOTSB_RACES;
import static data.character.CharacterValues.RaceType.STANDARD_RACES;
import static data.character.CharacterValues.RaceType.STANDARD_RACES2;
import static data.character.ClassSelections.CLASS_SELECTION_BUCK_ROGERS;
import static data.character.ClassSelections.CLASS_SELECTION_KRYNN;
import static data.character.ClassSelections.CLASS_SELECTION_STANDARD;
import static java.nio.charset.StandardCharsets.US_ASCII;

import java.util.Arrays;
import java.util.function.IntFunction;

import javax.annotation.Nonnull;

import io.vavr.Function2;
import io.vavr.Function3;
import io.vavr.collection.List;
import io.vavr.collection.Map;

import character.CharacterRace;
import character.ClassSelection;
import character.buckrogers.CharacterRaceBuckRogers;
import character.forgottenrealms.CharacterRaceKrynn;
import character.forgottenrealms.CharacterRaceStandard;
import character.forgottenrealms.CharacterRaceStandard2;
import character.forgottenrealms.CharacterRaceStandardSotSB;
import common.ByteBufferWrapper;

public enum CharacterValues {
	BUCK_ROGERS(259, NAME_LENGTH_NAME, 0, BUCK_ROGERS_RACES, CLASS_SELECTION_BUCK_ROGERS, //
		value(CharacterValueType.STR_CURRENT, 0x10), //
		value(CharacterValueType.DEX_CURRENT, 0x11), //
		value(CharacterValueType.CON_CURRENT, 0x12), //
		value(CharacterValueType.INT_CURRENT, 0x13), //
		value(CharacterValueType.WIS_CURRENT, 0x14), //
		value(CharacterValueType.CHA_CURRENT, 0x15), //
		value(CharacterValueType.TCH_CURRENT, 0x16), //
		value(CharacterValueType.STR_NATURAL, 0x17), //
		value(CharacterValueType.DEX_NATURAL, 0x18), //
		value(CharacterValueType.CON_NATURAL, 0x19), //
		value(CharacterValueType.INT_NATURAL, 0x1A), //
		value(CharacterValueType.WIS_NATURAL, 0x19), //
		value(CharacterValueType.CHA_NATURAL, 0x1C), //
		value(CharacterValueType.TCH_NATURAL, 0x1D), //
		value(CharacterValueType.HP_NATURAL, 0x45, 0x45), //
		value(CharacterValueType.HP_CURRENT, 0xE3, 0x119), //

		value(CharacterValueType.RACE, 0x27, 0x27), //
		value(CharacterValueType.CLASS, 0x28, 0x28), //
		value(CharacterValueType.AGE, 0x38), //
		value(CharacterValueType.GENDER, 0x26, 0x26), //
		value(CharacterValueType.NPC_FLAG, 0x4C, 0x4C), //

		value(CharacterValueType.MONEY_CREDITS, 0x2B, 0x2B), //

		value(CharacterValueType.LEVEL_BUCK_ROGERS, 0x29), //

		value(CharacterValueType.EXP, 0x2F), //
		value(CharacterValueType.STATUS, 0xD4, 0x100), //

		value(CharacterValueType.SKILL_PILOT_ROCKET, 0x7F, 0x7F), //

		value(CharacterValueType.BUCK_ROGERS_INFECTION_STATUS, 0xA1, 0xA1), //
		value(CharacterValueType.BUCK_ROGERS_GRENADE_MUFFLED, 0xA2, 0xA2), //
		value(CharacterValueType.BUCK_ROGERS_TALOS_DEFEATED, 0xA3, 0xA3) //
	), //
	POOL_OF_RADIANCE(285, NAME_LENGTH_NAME, 0, STANDARD_RACES, CLASS_SELECTION_STANDARD, //
		value(CharacterValueType.STR_NATURAL, 0x10), //
		value(CharacterValueType.STR_CURRENT, 0x10), //
		value(CharacterValueType.INT_NATURAL, 0x11), //
		value(CharacterValueType.INT_CURRENT, 0x11), //
		value(CharacterValueType.WIS_NATURAL, 0x12), //
		value(CharacterValueType.WIS_CURRENT, 0x12), //
		value(CharacterValueType.DEX_NATURAL, 0x13), //
		value(CharacterValueType.DEX_CURRENT, 0x13), //
		value(CharacterValueType.CON_NATURAL, 0x14), //
		value(CharacterValueType.CON_CURRENT, 0x14), //
		value(CharacterValueType.CHA_NATURAL, 0x15), //
		value(CharacterValueType.CHA_CURRENT, 0x15), //
		value(CharacterValueType.STR_EXC_NATURAL, 0x16), //
		value(CharacterValueType.STR_EXC_CURRENT, 0x16), //
		value(CharacterValueType.HP_NATURAL, 0x32), //
		value(CharacterValueType.HP_CURRENT, 0x11B), //

		value(CharacterValueType.RACE, 0x2E), //
		value(CharacterValueType.CLASS, 0x2F), //
		value(CharacterValueType.AGE, 0x30), //
		value(CharacterValueType.GENDER, 0x9E), //
		value(CharacterValueType.ALIGNMENT, 0xA0), //
		value(CharacterValueType.NPC_FLAG, 0x84), //

		value(CharacterValueType.MONEY_COPPER, 0x88), //
		value(CharacterValueType.MONEY_SILVER, 0x8A), //
		value(CharacterValueType.MONEY_ELEKTRUM, 0x8C), //
		value(CharacterValueType.MONEY_GOLD, 0x8E), //
		value(CharacterValueType.MONEY_PLATINUM, 0x90), //
		value(CharacterValueType.MONEY_GEM, 0x92), //
		value(CharacterValueType.MONEY_JEWELRY, 0x94), //

		value(CharacterValueType.LEVEL_CLERIC, 0x96), //
		value(CharacterValueType.LEVEL_DRUID, 0x97), //
		value(CharacterValueType.LEVEL_FIGHTER, 0x98), //
		value(CharacterValueType.LEVEL_PALADIN, 0x99), //
		value(CharacterValueType.LEVEL_RANGER, 0x9A), //
		value(CharacterValueType.LEVEL_MAGE, 0x9B), //
		value(CharacterValueType.LEVEL_THIEF, 0x9C), //
		value(CharacterValueType.LEVEL_MONK, 0x9D), //

		value(CharacterValueType.EXP, 0xAC), //
		value(CharacterValueType.STATUS, 0x10C, 0x100) //
	), //
	CURSE_OF_THE_AZURE_BONDS(422, NAME_LENGTH_NAME, 0, STANDARD_RACES, CLASS_SELECTION_STANDARD, //
		value(CharacterValueType.STR_NATURAL, 0x10), //
		value(CharacterValueType.STR_CURRENT, 0x11), //
		value(CharacterValueType.INT_NATURAL, 0x12), //
		value(CharacterValueType.INT_CURRENT, 0x13), //
		value(CharacterValueType.WIS_NATURAL, 0x14), //
		value(CharacterValueType.WIS_CURRENT, 0x15), //
		value(CharacterValueType.DEX_NATURAL, 0x16), //
		value(CharacterValueType.DEX_CURRENT, 0x17), //
		value(CharacterValueType.CON_NATURAL, 0x18), //
		value(CharacterValueType.CON_CURRENT, 0x19), //
		value(CharacterValueType.CHA_NATURAL, 0x1A), //
		value(CharacterValueType.CHA_CURRENT, 0x1B), //
		value(CharacterValueType.STR_EXC_NATURAL, 0x1C), //
		value(CharacterValueType.STR_EXC_CURRENT, 0x1D), //
		value(CharacterValueType.HP_NATURAL, 0x78), //
		value(CharacterValueType.HP_CURRENT, 0x1A4), //

		value(CharacterValueType.RACE, 0x74), //
		value(CharacterValueType.CLASS, 0x75), //
		value(CharacterValueType.AGE, 0x76), //
		value(CharacterValueType.GENDER, 0x119), //
		value(CharacterValueType.ALIGNMENT, 0x11B), //
		value(CharacterValueType.NPC_FLAG, 0xF7), //

		value(CharacterValueType.MONEY_COPPER, 0xFB), //
		value(CharacterValueType.MONEY_SILVER, 0xFD), //
		value(CharacterValueType.MONEY_ELEKTRUM, 0xFF), //
		value(CharacterValueType.MONEY_GOLD, 0x101), //
		value(CharacterValueType.MONEY_PLATINUM, 0x103), //
		value(CharacterValueType.MONEY_GEM, 0x105), //
		value(CharacterValueType.MONEY_JEWELRY, 0x107), //

		value(CharacterValueType.LEVEL_CLERIC, 0x109), //
		value(CharacterValueType.LEVEL_DRUID, 0x10A), //
		value(CharacterValueType.LEVEL_FIGHTER, 0x10B), //
		value(CharacterValueType.LEVEL_PALADIN, 0x10C), //
		value(CharacterValueType.LEVEL_RANGER, 0x10D), //
		value(CharacterValueType.LEVEL_MAGE, 0x10E), //
		value(CharacterValueType.LEVEL_THIEF, 0x10F), //
		value(CharacterValueType.LEVEL_MONK, 0x110), //

		value(CharacterValueType.EXP, 0x127), //
		value(CharacterValueType.STATUS, 0x195, 0x100) //
	), //
	SECRET_OF_THE_SILVER_BLADES(439, NAME_LENGTH_NAME, 0, SOTSB_RACES, CLASS_SELECTION_STANDARD, //
		value(CharacterValueType.STR_NATURAL, 0x10), //
		value(CharacterValueType.STR_CURRENT, 0x11), //
		value(CharacterValueType.INT_NATURAL, 0x12), //
		value(CharacterValueType.INT_CURRENT, 0x13), //
		value(CharacterValueType.WIS_NATURAL, 0x14), //
		value(CharacterValueType.WIS_CURRENT, 0x15), //
		value(CharacterValueType.DEX_NATURAL, 0x16), //
		value(CharacterValueType.DEX_CURRENT, 0x17), //
		value(CharacterValueType.CON_NATURAL, 0x18), //
		value(CharacterValueType.CON_CURRENT, 0x19), //
		value(CharacterValueType.CHA_NATURAL, 0x1A), //
		value(CharacterValueType.CHA_CURRENT, 0x1B), //
		value(CharacterValueType.STR_EXC_NATURAL, 0x1C), //
		value(CharacterValueType.STR_EXC_CURRENT, 0x1D), //
		value(CharacterValueType.HP_NATURAL, 0x70), //
		value(CharacterValueType.HP_CURRENT, 0x1B5), //

		value(CharacterValueType.RACE, 0x6B), //
		value(CharacterValueType.CLASS, 0x6C), //
		value(CharacterValueType.AGE, 0x6E), //
		value(CharacterValueType.GENDER, 0x11F), //
		value(CharacterValueType.ALIGNMENT, 0x120), //
		value(CharacterValueType.NPC_FLAG, 0xFF), //

		value(CharacterValueType.MONEY_COPPER, 0x103), //
		value(CharacterValueType.MONEY_SILVER, 0x105), //
		value(CharacterValueType.MONEY_ELEKTRUM, 0x107), //
		value(CharacterValueType.MONEY_GOLD, 0x109), //
		value(CharacterValueType.MONEY_PLATINUM, 0x10B), //
		value(CharacterValueType.MONEY_GEM, 0x10D), //
		value(CharacterValueType.MONEY_JEWELRY, 0x10F), //

		value(CharacterValueType.LEVEL_CLERIC, 0x111), //
		value(CharacterValueType.LEVEL_DRUID, 0x112), //
		value(CharacterValueType.LEVEL_FIGHTER, 0x113), //
		value(CharacterValueType.LEVEL_PALADIN, 0x114), //
		value(CharacterValueType.LEVEL_RANGER, 0x115), //
		value(CharacterValueType.LEVEL_MAGE, 0x116), //
		value(CharacterValueType.LEVEL_THIEF, 0x117), //

		value(CharacterValueType.EXP, 0x12C), //
		value(CharacterValueType.STATUS, 0x1A6, 0x100) //
	), //
	POOLS_OF_DARKNESS(510, NAME_LENGTH_NAME, 0, STANDARD_RACES2, CLASS_SELECTION_STANDARD, //
		value(CharacterValueType.STR_NATURAL, 0x10), //
		value(CharacterValueType.STR_CURRENT, 0x11), //
		value(CharacterValueType.INT_NATURAL, 0x12), //
		value(CharacterValueType.INT_CURRENT, 0x13), //
		value(CharacterValueType.WIS_NATURAL, 0x14), //
		value(CharacterValueType.WIS_CURRENT, 0x15), //
		value(CharacterValueType.DEX_NATURAL, 0x16), //
		value(CharacterValueType.DEX_CURRENT, 0x17), //
		value(CharacterValueType.CON_NATURAL, 0x18), //
		value(CharacterValueType.CON_CURRENT, 0x19), //
		value(CharacterValueType.CHA_NATURAL, 0x1A), //
		value(CharacterValueType.CHA_CURRENT, 0x1B), //
		value(CharacterValueType.STR_EXC_NATURAL, 0x1C), //
		value(CharacterValueType.STR_EXC_CURRENT, 0x1D), //
		value(CharacterValueType.HP_NATURAL, 0xB2), //
		value(CharacterValueType.HP_CURRENT, 0x1FC), //

		value(CharacterValueType.RACE, 0xAD), //
		value(CharacterValueType.CLASS, 0xAE), //
		value(CharacterValueType.AGE, 0xB0), //
		value(CharacterValueType.GENDER, 0x166), //
		value(CharacterValueType.ALIGNMENT, 0x167), //
		value(CharacterValueType.NPC_FLAG, 0x147), //

		value(CharacterValueType.MONEY_PLATINUM, 0x14B), //
		value(CharacterValueType.MONEY_GEM, 0x14D), //
		value(CharacterValueType.MONEY_JEWELRY, 0x14F), //

		value(CharacterValueType.LEVEL_CLERIC, 0x151), //
		value(CharacterValueType.LEVEL_DRUID, 0x152), //
		value(CharacterValueType.LEVEL_FIGHTER, 0x153), //
		value(CharacterValueType.LEVEL_PALADIN, 0x154), //
		value(CharacterValueType.LEVEL_RANGER, 0x155), //
		value(CharacterValueType.LEVEL_MAGE, 0x156), //
		value(CharacterValueType.LEVEL_THIEF, 0x157), //

		value(CharacterValueType.EXP, 0x172), //
		value(CharacterValueType.STATUS, 0x1ED, 0x100) //
	), //
	CHAMPIONS_OF_KRYNN(409, NAME_LENGTH_NAME, 0, KRYNN_RACES, CLASS_SELECTION_KRYNN, //
		value(CharacterValueType.STR_NATURAL, 0x10), //
		value(CharacterValueType.STR_CURRENT, 0x11), //
		value(CharacterValueType.INT_NATURAL, 0x12), //
		value(CharacterValueType.INT_CURRENT, 0x13), //
		value(CharacterValueType.WIS_NATURAL, 0x14), //
		value(CharacterValueType.WIS_CURRENT, 0x15), //
		value(CharacterValueType.DEX_NATURAL, 0x16), //
		value(CharacterValueType.DEX_CURRENT, 0x17), //
		value(CharacterValueType.CON_NATURAL, 0x18), //
		value(CharacterValueType.CON_CURRENT, 0x19), //
		value(CharacterValueType.CHA_NATURAL, 0x1A), //
		value(CharacterValueType.CHA_CURRENT, 0x1B), //
		value(CharacterValueType.STR_EXC_NATURAL, 0x1C), //
		value(CharacterValueType.STR_EXC_CURRENT, 0x1D), //
		value(CharacterValueType.HP_NATURAL, 0x62), //
		value(CharacterValueType.HP_CURRENT, 0x197), //

		value(CharacterValueType.RACE, 0x5A), //
		value(CharacterValueType.CLASS, 0x5B), //
		value(CharacterValueType.AGE, 0x60), //
		value(CharacterValueType.GENDER, 0x109), //
		value(CharacterValueType.ALIGNMENT, 0x10A), //
		value(CharacterValueType.NPC_FLAG, 0xE7), //

		value(CharacterValueType.MONEY_COPPER, 0xED), //
		value(CharacterValueType.MONEY_BRONZE, 0xEF), //
		value(CharacterValueType.MONEY_PLATINUM, 0xF1), //
		value(CharacterValueType.MONEY_STEEL, 0xF3), //
		value(CharacterValueType.MONEY_GEM, 0xF5), //
		value(CharacterValueType.MONEY_JEWELRY, 0xF7), //

		value(CharacterValueType.LEVEL_CLERIC, 0xF9), //
		value(CharacterValueType.LEVEL_DRUID, 0xFA), //
		value(CharacterValueType.LEVEL_FIGHTER, 0xFB), //
		value(CharacterValueType.LEVEL_PALADIN, 0xFC), //
		value(CharacterValueType.LEVEL_RANGER, 0xFD), //
		value(CharacterValueType.LEVEL_MAGE, 0xFE), //
		value(CharacterValueType.LEVEL_THIEF, 0xFF), //
		value(CharacterValueType.LEVEL_KNIGHT, 0x100), //

		value(CharacterValueType.EXP, 0x116), //
		value(CharacterValueType.STATUS, 0x188, 0x100) //
	), //
	DEATH_KNIGHTS_OF_KRYNN(216, NAME_LENGTH_NAME, 0, KRYNN_RACES, CLASS_SELECTION_KRYNN, //
		value(CharacterValueType.STR_NATURAL, 0x10), //
		value(CharacterValueType.STR_CURRENT, 0x11), //
		value(CharacterValueType.INT_NATURAL, 0x12), //
		value(CharacterValueType.INT_CURRENT, 0x13), //
		value(CharacterValueType.WIS_NATURAL, 0x14), //
		value(CharacterValueType.WIS_CURRENT, 0x15), //
		value(CharacterValueType.DEX_NATURAL, 0x16), //
		value(CharacterValueType.DEX_CURRENT, 0x17), //
		value(CharacterValueType.CON_NATURAL, 0x18), //
		value(CharacterValueType.CON_CURRENT, 0x19), //
		value(CharacterValueType.CHA_NATURAL, 0x1A), //
		value(CharacterValueType.CHA_CURRENT, 0x1B), //
		value(CharacterValueType.STR_EXC_NATURAL, 0x1C), //
		value(CharacterValueType.STR_EXC_CURRENT, 0x1D), //
		value(CharacterValueType.HP_NATURAL, 0x28), //
		value(CharacterValueType.HP_CURRENT, 0xD6), //

		value(CharacterValueType.RACE, 0x20), //
		value(CharacterValueType.CLASS, 0x21), //
		value(CharacterValueType.AGE, 0x26), //
		value(CharacterValueType.GENDER, 0x55), //
		value(CharacterValueType.ALIGNMENT, 0x56), //
		value(CharacterValueType.NPC_FLAG, 0x3E), //

		value(CharacterValueType.MONEY_STEEL, 0x3F), //
		value(CharacterValueType.MONEY_GEM, 0x41), //
		value(CharacterValueType.MONEY_JEWELRY, 0x43), //

		value(CharacterValueType.LEVEL_CLERIC, 0x45), //
		value(CharacterValueType.LEVEL_DRUID, 0x46), //
		value(CharacterValueType.LEVEL_FIGHTER, 0x47), //
		value(CharacterValueType.LEVEL_PALADIN, 0x48), //
		value(CharacterValueType.LEVEL_RANGER, 0x49), //
		value(CharacterValueType.LEVEL_MAGE, 0x4A), //
		value(CharacterValueType.LEVEL_THIEF, 0x4B), //
		value(CharacterValueType.LEVEL_KNIGHT, 0x4C), //

		value(CharacterValueType.EXP, 0x62), //
		value(CharacterValueType.STATUS, 0xC7, 0x100) //
	), //
	DARK_QUEEN_OF_KRYNN(598, NAME_STRING_TERMINATOR, 0x68, KRYNN_RACES, CLASS_SELECTION_KRYNN, //
		value(CharacterValueType.STR_NATURAL, 0x78), //
		value(CharacterValueType.STR_CURRENT, 0x79), //
		value(CharacterValueType.INT_NATURAL, 0x7A), //
		value(CharacterValueType.INT_CURRENT, 0x7B), //
		value(CharacterValueType.WIS_NATURAL, 0x7C), //
		value(CharacterValueType.WIS_CURRENT, 0x7D), //
		value(CharacterValueType.DEX_NATURAL, 0x7E), //
		value(CharacterValueType.DEX_CURRENT, 0x7F), //
		value(CharacterValueType.CON_NATURAL, 0x80), //
		value(CharacterValueType.CON_CURRENT, 0x81), //
		value(CharacterValueType.CHA_NATURAL, 0x82), //
		value(CharacterValueType.CHA_CURRENT, 0x83), //
		value(CharacterValueType.STR_EXC_NATURAL, 0x84), //
		value(CharacterValueType.STR_EXC_CURRENT, 0x85), //
		value(CharacterValueType.HP_NATURAL, 0x89), //
		value(CharacterValueType.HP_CURRENT, 0x198), //

		value(CharacterValueType.RACE, 0x58), //
		value(CharacterValueType.CLASS, 0x5A), //
		value(CharacterValueType.AGE, 0x52), //
		value(CharacterValueType.GENDER, 0x60), //
		value(CharacterValueType.ALIGNMENT, 0x62), //
		value(CharacterValueType.NPC_FLAG, 0x9B), //

		value(CharacterValueType.MONEY_STEEL, 0x4C), //
		value(CharacterValueType.MONEY_GEM, 0x4E), //
		value(CharacterValueType.MONEY_JEWELRY, 0x50), //

		value(CharacterValueType.LEVEL_CLERIC, 0xA5), //
		value(CharacterValueType.LEVEL_FIGHTER, 0xA7), //
		value(CharacterValueType.LEVEL_PALADIN, 0xA8), //
		value(CharacterValueType.LEVEL_RANGER, 0xA9), //
		value(CharacterValueType.LEVEL_MAGE, 0xAA), //
		value(CharacterValueType.LEVEL_THIEF, 0xAB), //
		value(CharacterValueType.LEVEL_KNIGHT, 0xA6), //

		value(CharacterValueType.EXP, 0x44), //
		value(CharacterValueType.STATUS, 0x64, 0x100) //
	), //
	UNLIMITED_ADVENTURES(450, NAME_STRING_TERMINATOR, 0x60, STANDARD_RACES2, CLASS_SELECTION_STANDARD, //
		value(CharacterValueType.STR_NATURAL, 0x70), //
		value(CharacterValueType.STR_CURRENT, 0x71), //
		value(CharacterValueType.INT_NATURAL, 0x72), //
		value(CharacterValueType.INT_CURRENT, 0x73), //
		value(CharacterValueType.WIS_NATURAL, 0x74), //
		value(CharacterValueType.WIS_CURRENT, 0x75), //
		value(CharacterValueType.DEX_NATURAL, 0x76), //
		value(CharacterValueType.DEX_CURRENT, 0x77), //
		value(CharacterValueType.CON_NATURAL, 0x78), //
		value(CharacterValueType.CON_CURRENT, 0x79), //
		value(CharacterValueType.CHA_NATURAL, 0x7A), //
		value(CharacterValueType.CHA_CURRENT, 0x7B), //
		value(CharacterValueType.STR_EXC_NATURAL, 0x7C), //
		value(CharacterValueType.STR_EXC_CURRENT, 0x7D), //
		value(CharacterValueType.HP_NATURAL, 0x81), //
		value(CharacterValueType.HP_CURRENT, 0x18B), //

		value(CharacterValueType.RACE, 0x58), //
		value(CharacterValueType.CLASS, 0x59), //
		value(CharacterValueType.AGE, 0x52), //
		value(CharacterValueType.GENDER, 0x5C), //
		value(CharacterValueType.ALIGNMENT, 0x5D), //
		value(CharacterValueType.NPC_FLAG, 0x93), //

		value(CharacterValueType.MONEY_PLATINUM, 0x4C), //
		value(CharacterValueType.MONEY_GEM, 0x4E), //
		value(CharacterValueType.MONEY_JEWELRY, 0x50), //

		value(CharacterValueType.LEVEL_CLERIC, 0x9D), //
		value(CharacterValueType.LEVEL_FIGHTER, 0x9F), //
		value(CharacterValueType.LEVEL_PALADIN, 0xA0), //
		value(CharacterValueType.LEVEL_RANGER, 0xA1), //
		value(CharacterValueType.LEVEL_MAGE, 0xA2), //
		value(CharacterValueType.LEVEL_THIEF, 0xA3), //
		value(CharacterValueType.LEVEL_KNIGHT, 0x9E), //

		value(CharacterValueType.EXP, 0x44), //
		value(CharacterValueType.STATUS, 0x5E, 0x100) //
	), //
	GATEWAY_TO_THE_SAVAGE_FRONTIER(422, NAME_LENGTH_NAME, 0, STANDARD_RACES, CLASS_SELECTION_STANDARD, //
		value(CharacterValueType.STR_NATURAL, 0x10), //
		value(CharacterValueType.STR_CURRENT, 0x11), //
		value(CharacterValueType.INT_NATURAL, 0x12), //
		value(CharacterValueType.INT_CURRENT, 0x13), //
		value(CharacterValueType.WIS_NATURAL, 0x14), //
		value(CharacterValueType.WIS_CURRENT, 0x15), //
		value(CharacterValueType.DEX_NATURAL, 0x16), //
		value(CharacterValueType.DEX_CURRENT, 0x17), //
		value(CharacterValueType.CON_NATURAL, 0x18), //
		value(CharacterValueType.CON_CURRENT, 0x19), //
		value(CharacterValueType.CHA_NATURAL, 0x1A), //
		value(CharacterValueType.CHA_CURRENT, 0x1B), //
		value(CharacterValueType.STR_EXC_NATURAL, 0x1C), //
		value(CharacterValueType.STR_EXC_CURRENT, 0x1D), //
		value(CharacterValueType.HP_NATURAL, 0x78), //
		value(CharacterValueType.HP_CURRENT, 0x1A4), //

		value(CharacterValueType.RACE, 0x74), //
		value(CharacterValueType.CLASS, 0x75), //
		value(CharacterValueType.AGE, 0x76), //
		value(CharacterValueType.GENDER, 0x119), //
		value(CharacterValueType.ALIGNMENT, 0x11B), //
		value(CharacterValueType.NPC_FLAG, 0xF7), //

		value(CharacterValueType.MONEY_COPPER, 0xFB), //
		value(CharacterValueType.MONEY_SILVER, 0xFD), //
		value(CharacterValueType.MONEY_ELEKTRUM, 0xFF), //
		value(CharacterValueType.MONEY_GOLD, 0x101), //
		value(CharacterValueType.MONEY_PLATINUM, 0x103), //
		value(CharacterValueType.MONEY_GEM, 0x105), //
		value(CharacterValueType.MONEY_JEWELRY, 0x107), //

		value(CharacterValueType.LEVEL_CLERIC, 0x109), //
		value(CharacterValueType.LEVEL_DRUID, 0x10A), //
		value(CharacterValueType.LEVEL_FIGHTER, 0x10B), //
		value(CharacterValueType.LEVEL_PALADIN, 0x10C), //
		value(CharacterValueType.LEVEL_RANGER, 0x10D), //
		value(CharacterValueType.LEVEL_MAGE, 0x10E), //
		value(CharacterValueType.LEVEL_THIEF, 0x10F), //
		value(CharacterValueType.LEVEL_MONK, 0x110), //

		value(CharacterValueType.EXP, 0x127), //
		value(CharacterValueType.STATUS, 0x195, 0x100) //
	), //
	TREASURES_OF_THE_SAVAGE_FRONTIER(510, NAME_LENGTH_NAME, 0, STANDARD_RACES2, CLASS_SELECTION_STANDARD, //
		value(CharacterValueType.STR_NATURAL, 0x10), //
		value(CharacterValueType.STR_CURRENT, 0x11), //
		value(CharacterValueType.INT_NATURAL, 0x12), //
		value(CharacterValueType.INT_CURRENT, 0x13), //
		value(CharacterValueType.WIS_NATURAL, 0x14), //
		value(CharacterValueType.WIS_CURRENT, 0x15), //
		value(CharacterValueType.DEX_NATURAL, 0x16), //
		value(CharacterValueType.DEX_CURRENT, 0x17), //
		value(CharacterValueType.CON_NATURAL, 0x18), //
		value(CharacterValueType.CON_CURRENT, 0x19), //
		value(CharacterValueType.CHA_NATURAL, 0x1A), //
		value(CharacterValueType.CHA_CURRENT, 0x1B), //
		value(CharacterValueType.STR_EXC_NATURAL, 0x1C), //
		value(CharacterValueType.STR_EXC_CURRENT, 0x1D), //
		value(CharacterValueType.HP_NATURAL, 0xB2), //
		value(CharacterValueType.HP_CURRENT, 0x1FC), //

		value(CharacterValueType.RACE, 0xAD), //
		value(CharacterValueType.CLASS, 0xAE), //
		value(CharacterValueType.AGE, 0xB0), //
		value(CharacterValueType.GENDER, 0x166), //
		value(CharacterValueType.ALIGNMENT, 0x167), //
		value(CharacterValueType.NPC_FLAG, 0x147), //

		value(CharacterValueType.MONEY_PLATINUM, 0x14B), //
		value(CharacterValueType.MONEY_GEM, 0x14D), //
		value(CharacterValueType.MONEY_JEWELRY, 0x14F), //

		value(CharacterValueType.LEVEL_CLERIC, 0x151), //
		value(CharacterValueType.LEVEL_DRUID, 0x152), //
		value(CharacterValueType.LEVEL_FIGHTER, 0x153), //
		value(CharacterValueType.LEVEL_PALADIN, 0x154), //
		value(CharacterValueType.LEVEL_RANGER, 0x155), //
		value(CharacterValueType.LEVEL_MAGE, 0x156), //
		value(CharacterValueType.LEVEL_THIEF, 0x157), //

		value(CharacterValueType.EXP, 0x172), //
		value(CharacterValueType.STATUS, 0x1ED, 0x100) //
	), //
	;

	private final int fileSize;
	private final NameFormat nameFormat;
	private final int nameStart;
	private final RaceType raceType;
	private final Map<ClassSelection, Integer> classSelections;
	private final Map<CharacterValueType, CharacterValue> values;

	private CharacterValues(int fileSize, NameFormat nameFormat, int nameStart, RaceType raceType, Map<ClassSelection, Integer> classSelections,
		CharacterValue... values) {

		this.fileSize = fileSize;
		this.nameFormat = nameFormat;
		this.nameStart = nameStart;
		this.raceType = raceType;
		this.classSelections = classSelections;
		this.values = List.of(values).toMap(CharacterValue::getType, v -> v);
	}

	public int getFileSize() {
		return fileSize;
	}

	public boolean hasValueType(CharacterValueType type) {
		return values.containsKey(type);
	}

	public Map<ClassSelection, Integer> getClassSelections() {
		return classSelections;
	}

	public int read(@Nonnull CharacterValueType type, @Nonnull ByteBufferWrapper file, @Nonnull ByteBufferWrapper memory) {
		return values.getOrElse(type, null).read(file, memory);
	}

	public void write(@Nonnull CharacterValueType type, @Nonnull ByteBufferWrapper file, @Nonnull ByteBufferWrapper memory, int value) {
		values.getOrElse(type, null).write(file, memory, value);
	}

	public String readName(@Nonnull ByteBufferWrapper file) {
		return nameFormat.readName(file, nameStart);
	}

	public void writeName(@Nonnull ByteBufferWrapper file, @Nonnull String name) {
		nameFormat.writeName(file, nameStart, name);
	}

	public CharacterRace interpret(int value) {
		return raceType.interpret(value);
	}

	public int readStatus(@Nonnull ByteBufferWrapper file) {
		return values.getOrElse(CharacterValueType.STATUS, null).readFile(file);
	}

	public void writeStatus(@Nonnull ByteBufferWrapper file, @Nonnull ByteBufferWrapper memory, int fileValue, int memoryValue) {
		values.getOrElse(CharacterValueType.STATUS, null).write(file, memory, memoryValue);
		values.getOrElse(CharacterValueType.STATUS, null).writeFile(file, fileValue);
	}

	public void copyToFile(@Nonnull ByteBufferWrapper file, @Nonnull ByteBufferWrapper memory) {
		values.forEach((t, v) -> v.copyToFile(file, memory));
	}

	public void copyToMemory(@Nonnull ByteBufferWrapper file, @Nonnull ByteBufferWrapper memory) {
		values.forEach((t, v) -> v.copyToMemory(file, memory));
	}

	enum NameFormat {
		NAME_LENGTH_NAME( //
			(buf, index) -> {
				int nameLength = buf.getUnsigned(index);
				byte[] nameArray = new byte[nameLength];
				buf.get(index + 1, nameArray);
				return new String(nameArray, US_ASCII);
			}, //
			(buf, index, name) ->

			{
				buf.put(index, (byte) name.length());
				byte[] nameArray = Arrays.copyOf(name.getBytes(US_ASCII), 16);
				buf.put(index + 1, nameArray);
				return buf;
			} //
		), //

		NAME_STRING_TERMINATOR( //
			(buf, index) -> {
				byte[] nameArray = new byte[16];
				buf.get(index, nameArray);
				int i = 15;
				while (i >= 0 && nameArray[i] == '\0')
					i--;
				return new String(nameArray, 0, i + 1, US_ASCII);
			}, //
			(buf, index, name) -> {
				byte[] nameArray = Arrays.copyOf(name.getBytes(US_ASCII), 16);
				buf.put(index, nameArray);
				return buf;
			} //
		), //
		;

		private final Function2<ByteBufferWrapper, Integer, String> reader;
		private final Function3<ByteBufferWrapper, Integer, String, ByteBufferWrapper> writer;

		private NameFormat(Function2<ByteBufferWrapper, Integer, String> reader,
			Function3<ByteBufferWrapper, Integer, String, ByteBufferWrapper> writer) {

			this.reader = reader;
			this.writer = writer;
		}

		public String readName(@Nonnull ByteBufferWrapper file, int startIndex) {
			return reader.apply(file, startIndex);
		}

		public void writeName(@Nonnull ByteBufferWrapper file, int startIndex, @Nonnull String name) {
			writer.apply(file, startIndex, name);
		}
	}

	enum RaceType {
		BUCK_ROGERS_RACES(CharacterRaceBuckRogers::from), //
		KRYNN_RACES(CharacterRaceKrynn::from), //
		STANDARD_RACES(CharacterRaceStandard::from), //
		STANDARD_RACES2(CharacterRaceStandard2::from), //
		SOTSB_RACES(CharacterRaceStandardSotSB::from), //
		;

		private final IntFunction<CharacterRace> func;

		private RaceType(IntFunction<CharacterRace> func) {
			this.func = func;
		}

		public CharacterRace interpret(int value) {
			return this.func.apply(value);
		}
	}
}
