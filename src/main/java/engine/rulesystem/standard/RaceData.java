package engine.rulesystem.standard;

import static character.ClassSelection.of;
import static character.forgottenrealms.CharacterClassForgottenRealms.CLERIC;
import static character.forgottenrealms.CharacterClassForgottenRealms.FIGHTER;
import static character.forgottenrealms.CharacterClassForgottenRealms.MAGE;
import static character.forgottenrealms.CharacterClassForgottenRealms.PALADIN;
import static character.forgottenrealms.CharacterClassForgottenRealms.RANGER;
import static character.forgottenrealms.CharacterClassForgottenRealms.THIEF;

import io.vavr.collection.List;
import io.vavr.collection.Seq;

import character.CharacterRace;
import character.ClassSelection;
import character.forgottenrealms.CharacterRaceStandard;

enum RaceData {
	DWARF(0, 0, 0, 0, 0, 0, of(FIGHTER), of(THIEF), of(FIGHTER, THIEF)), //
	ELF(0, 0, 0, 0, 0, 0, of(FIGHTER), of(MAGE), of(THIEF), of(FIGHTER, MAGE), of(FIGHTER, THIEF), of(FIGHTER, MAGE, THIEF), of(MAGE, THIEF)), //
	HALF_ELF(0, 0, 0, 0, 0, 0, of(CLERIC), of(CLERIC, FIGHTER), of(CLERIC, FIGHTER, MAGE), of(CLERIC, RANGER), of(CLERIC, MAGE), of(FIGHTER),
		of(FIGHTER, MAGE), of(FIGHTER, THIEF), of(FIGHTER, MAGE, THIEF), of(RANGER), of(MAGE), of(MAGE, THIEF), of(THIEF)), //
	GNOME(0, 0, 0, 0, 0, 0, of(FIGHTER), of(THIEF), of(FIGHTER, THIEF)), //
	HALFLING(0, 0, 0, 0, 0, 0, of(FIGHTER), of(THIEF), of(FIGHTER, THIEF)), //
	HALF_ORC(0, 0, 0, 0, 0, 0), //
	HUMAN(0, 0, 0, 0, 0, 0, of(CLERIC), of(FIGHTER), of(PALADIN), of(RANGER), of(MAGE), of(THIEF)), //
	;

	private int strMod;
	private int dexMod;
	private int conMod;
	private int intMod;
	private int wisMod;
	private int chrMod;
	private Seq<ClassSelection> availableClasses;

	private RaceData(int strMod, int dexMod, int conMod, int intMod, int wisMod, int chrMod, ClassSelection... availableClasses) {

		this.strMod = strMod;
		this.dexMod = dexMod;
		this.conMod = conMod;
		this.intMod = intMod;
		this.wisMod = wisMod;
		this.chrMod = chrMod;
		this.availableClasses = List.of(availableClasses);
	}

	public static RaceData by(CharacterRaceStandard race) {
		return RaceData.valueOf(race.name());
	}

	public CharacterRace getCharacterRace() {
		return CharacterRaceStandard.valueOf(this.name());
	}

	public Seq<ClassSelection> getClassSelections() {
		return availableClasses;
	}

	public int getSTRModifier() {
		return strMod;
	}

	public int getDEXModifier() {
		return dexMod;
	}

	public int getCONModifier() {
		return conMod;
	}

	public int getINTModifier() {
		return intMod;
	}

	public int getWIDModifier() {
		return wisMod;
	}

	public int getCHRModifier() {
		return chrMod;
	}
}
