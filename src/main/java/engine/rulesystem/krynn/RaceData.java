package engine.rulesystem.krynn;

import static character.ClassSelection.of;
import static character.forgottenrealms.CharacterClassForgottenRealms.CLERIC;
import static character.forgottenrealms.CharacterClassForgottenRealms.FIGHTER;
import static character.forgottenrealms.CharacterClassForgottenRealms.KNIGHT;
import static character.forgottenrealms.CharacterClassForgottenRealms.MAGE;
import static character.forgottenrealms.CharacterClassForgottenRealms.RANGER;
import static character.forgottenrealms.CharacterClassForgottenRealms.THIEF;

import io.vavr.collection.List;
import io.vavr.collection.Seq;

import character.CharacterRace;
import character.ClassSelection;
import character.forgottenrealms.CharacterRaceKrynn;

enum RaceData {
	SILVANESTI_ELF(0, 0, 0, 0, 0, 0, of(CLERIC), of(FIGHTER), of(MAGE), of(RANGER), of(CLERIC, FIGHTER), of(CLERIC, FIGHTER, MAGE), of(CLERIC, MAGE),
		of(CLERIC, RANGER), of(FIGHTER, MAGE)), //
	QUALINESTI_ELF(0, 0, 0, 0, 0, 0, of(CLERIC), of(FIGHTER), of(MAGE), of(THIEF), of(RANGER), of(CLERIC, FIGHTER), of(CLERIC, FIGHTER, MAGE),
		of(CLERIC, MAGE), of(CLERIC, RANGER), of(FIGHTER, MAGE), of(FIGHTER, MAGE, THIEF), of(FIGHTER, THIEF), of(MAGE, THIEF)), //
	HALF_ELF(0, 0, 0, 0, 0, 0, of(CLERIC), of(FIGHTER), of(MAGE), of(THIEF), of(RANGER), of(KNIGHT), of(CLERIC, FIGHTER), of(CLERIC, FIGHTER, MAGE),
		of(CLERIC, MAGE), of(CLERIC, RANGER), of(FIGHTER, MAGE), of(FIGHTER, MAGE, THIEF), of(FIGHTER, THIEF), of(MAGE, THIEF)), //
	MOUNTAIN_DWARF(0, 0, 0, 0, 0, 0, of(CLERIC), of(FIGHTER), of(THIEF), of(CLERIC, FIGHTER), of(CLERIC, THIEF), of(FIGHTER, THIEF)), //
	HILL_DWARF(0, 0, 0, 0, 0, 0, of(CLERIC), of(FIGHTER), of(THIEF), of(RANGER), of(CLERIC, FIGHTER), of(CLERIC, THIEF), of(CLERIC, RANGER),
		of(FIGHTER, THIEF)), //
	KENDER(0, 0, 0, 0, 0, 0, of(CLERIC), of(FIGHTER), of(THIEF), of(RANGER), of(CLERIC, FIGHTER), of(CLERIC, THIEF), of(CLERIC, RANGER),
		of(FIGHTER, THIEF)), //
	HUMAN(0, 0, 0, 0, 0, 0, of(CLERIC), of(FIGHTER), of(MAGE), of(THIEF), of(RANGER), of(KNIGHT)), //
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

	public static RaceData by(CharacterRaceKrynn race) {
		return RaceData.valueOf(race.name());
	}

	public CharacterRace getCharacterRace() {
		return CharacterRaceKrynn.valueOf(this.name());
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
