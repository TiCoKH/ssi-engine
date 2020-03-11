package engine.rulesystem.buckrogers;

import static character.ClassSelection.of;
import static character.buckrogers.CharacterClassBuckRogers.ENGINEER;
import static character.buckrogers.CharacterClassBuckRogers.MEDIC;
import static character.buckrogers.CharacterClassBuckRogers.ROCKETJOCK;
import static character.buckrogers.CharacterClassBuckRogers.ROGUE;
import static character.buckrogers.CharacterClassBuckRogers.WARRIOR;

import io.vavr.collection.List;
import io.vavr.collection.Seq;

import character.CharacterRace;
import character.ClassSelection;
import character.buckrogers.CharacterRaceBuckRogers;

enum RaceData {
	TERRAN(0, 0, 1, 0, 1, 0, 0, of(ROCKETJOCK), of(MEDIC), of(WARRIOR), of(ENGINEER), of(ROGUE)), //
	MARTIAN(-1, 1, -1, 0, -1, 1, 0, of(ROCKETJOCK), of(MEDIC), of(WARRIOR), of(ENGINEER), of(ROGUE)), //
	VENUSIAN(0, -1, 1, 0, 1, -1, 0, of(ROCKETJOCK), of(MEDIC), of(WARRIOR), of(ENGINEER), of(ROGUE)), //
	MERCURIAN(-1, 1, 1, 0, 0, 0, 0, of(ROCKETJOCK), of(MEDIC), of(WARRIOR), of(ENGINEER), of(ROGUE)), //
	TINKER(-2, 3, -2, 0, 0, 0, 3, of(MEDIC), of(ENGINEER)), //
	DESERT_RUNNER(2, 2, 1, 0, 0, -1, 0, of(ROCKETJOCK), of(WARRIOR), of(ENGINEER)), //
	LUNARIAN(0, 0, 0, 0, 0, 0, 0), //
	LOWLANDER(0, 0, 0, 0, 0, 0, 0), //
	;

	private int strMod;
	private int dexMod;
	private int conMod;
	private int intMod;
	private int wisMod;
	private int chrMod;
	private int techMod;
	private Seq<ClassSelection> availableClasses;

	private RaceData(int strMod, int dexMod, int conMod, int intMod, int wisMod, int chrMod, int techMod, ClassSelection... availableClasses) {

		this.strMod = strMod;
		this.dexMod = dexMod;
		this.conMod = conMod;
		this.intMod = intMod;
		this.wisMod = wisMod;
		this.chrMod = chrMod;
		this.techMod = techMod;
		this.availableClasses = List.of(availableClasses);
	}

	public static RaceData by(CharacterRaceBuckRogers race) {
		return RaceData.valueOf(race.name());
	}

	public CharacterRace getCharacterRace() {
		return CharacterRaceBuckRogers.valueOf(this.name());
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

	public int getTECHModifier() {
		return techMod;
	}

	public boolean hasTECHModifier() {
		return true;
	}
}
