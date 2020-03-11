package data.character;

import static character.ClassSelection.of;
import static character.forgottenrealms.CharacterClassForgottenRealms.CLERIC;
import static character.forgottenrealms.CharacterClassForgottenRealms.DRUID;
import static character.forgottenrealms.CharacterClassForgottenRealms.FIGHTER;
import static character.forgottenrealms.CharacterClassForgottenRealms.KNIGHT;
import static character.forgottenrealms.CharacterClassForgottenRealms.MAGE;
import static character.forgottenrealms.CharacterClassForgottenRealms.PALADIN;
import static character.forgottenrealms.CharacterClassForgottenRealms.RANGER;
import static character.forgottenrealms.CharacterClassForgottenRealms.THIEF;
import static io.vavr.collection.Map.entry;

import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;

import character.ClassSelection;
import character.buckrogers.CharacterClassBuckRogers;

public class ClassSelections {
	static final Map<ClassSelection, Integer> CLASS_SELECTION_BUCK_ROGERS = List.of(CharacterClassBuckRogers.values()).toMap(ClassSelection::of,
		CharacterClassBuckRogers::ordinal);

	static final Map<ClassSelection, Integer> CLASS_SELECTION_STANDARD = HashMap.ofEntries( //
		entry(of(CLERIC), 0), //
		entry(of(DRUID), 1), //
		entry(of(FIGHTER), 2), //
		entry(of(PALADIN), 3), //
		entry(of(RANGER), 4), //
		entry(of(MAGE), 5), //
		entry(of(THIEF), 6), //
		entry(of(KNIGHT), 7), //
		entry(of(CLERIC, FIGHTER), 8), //
		entry(of(CLERIC, FIGHTER, MAGE), 9), //
		entry(of(CLERIC, RANGER), 0xA), //
		entry(of(CLERIC, MAGE), 0xB), //
		entry(of(CLERIC, THIEF), 0xC), //
		entry(of(FIGHTER, MAGE), 0xD), //
		entry(of(FIGHTER, THIEF), 0xE), //
		entry(of(FIGHTER, MAGE, THIEF), 0xF), //
		entry(of(MAGE, THIEF), 0x10) //
	);

	static final Map<ClassSelection, Integer> CLASS_SELECTION_KRYNN = HashMap.ofEntries( //
		entry(of(CLERIC), 0), //
		entry(of(DRUID), 1), //
		entry(of(FIGHTER), 2), //
		entry(of(PALADIN), 3), //
		entry(of(RANGER), 4), //
		entry(of(MAGE), 5), //
		entry(of(THIEF), 6), //
		entry(of(KNIGHT), 7), //
		entry(of(CLERIC, FIGHTER), 8), //
		entry(of(CLERIC, FIGHTER, MAGE), 9), //
		entry(of(CLERIC, RANGER), 0xA), //
		entry(of(CLERIC, MAGE), 0xB), //
		entry(of(CLERIC, THIEF), 0xC), //
		entry(of(FIGHTER, MAGE), 0xD), //
		entry(of(FIGHTER, THIEF), 0xE), //
		entry(of(FIGHTER, MAGE, THIEF), 0xF), //
		entry(of(MAGE, THIEF), 0x10) //
	);

	private ClassSelections() {
	}
}
