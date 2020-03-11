package engine.rulesystem.krynn;

import static character.CharacterAlignment.AlignmentMorals.EVIL;
import static character.forgottenrealms.CharacterStatusForgottenRealms.OKAY;
import static engine.rulesystem.Dice.D100;
import static engine.rulesystem.Dice.D6;

import javax.annotation.Nonnull;

import io.vavr.collection.Array;
import io.vavr.collection.List;
import io.vavr.collection.Seq;

import character.CharacterAlignment;
import character.CharacterClass;
import character.CharacterDeity;
import character.CharacterRace;
import character.ClassSelection;
import character.forgottenrealms.AbilityScoreForgottenRealms;
import character.forgottenrealms.CharacterClassForgottenRealms;
import character.forgottenrealms.CharacterRaceKrynn;
import data.character.AbstractCharacter;
import engine.rulesystem.Flavor;

public class KrynnFlavor implements Flavor {

	@Override
	public Seq<CharacterRace> getRaces() {
		return List.of(RaceData.values()) //
			.filter(race -> !race.getClassSelections().isEmpty()) //
			.map(RaceData::getCharacterRace); //
	}

	@Override
	public Seq<ClassSelection> getClassSelections(@Nonnull CharacterRace race) {
		return RaceData.by((CharacterRaceKrynn) race).getClassSelections();
	}

	@Override
	public boolean isAlignmentRequired() {
		return true;
	}

	@Override
	public Seq<CharacterAlignment> getAlignments(ClassSelection classSelection) {
		return classSelection //
			.map(CharacterClass::toString) //
			.map(ClassData::valueOf) //
			.map(ClassData::getAllowedAlignments) //
			.map(Array::of) //
			.reduce((l1, l2) -> l1.retainAll(l2)) //
			.filter(alignment -> !EVIL.equals(alignment.getMorals()));
	}

	@Override
	public boolean isDeityRequired(ClassSelection classSelection) {
		return classSelection.contains(CharacterClassForgottenRealms.CLERIC);
	}

	@Override
	public Seq<CharacterDeity> getDeities(ClassSelection classSelection) {
		return List.empty();
	}

	@Override
	public void initCharacter(AbstractCharacter character, int initialExperience) {
		character.setName("");
		character.setAge(10);
		character.addExperience(initialExperience);
		character.getClassSelection().forEach(CharacterClassForgottenRealms.class, c -> character.setLevel(c, 1));
		character.getMoneyTypes().forEach(m -> character.setMoneyValue(m, 10));
		character.setStatus(OKAY);
		reroll(character);
	}

	@Override
	public void reroll(AbstractCharacter character) {
		List.of(AbilityScoreForgottenRealms.values()).forEach(score -> {
			int scoreValue = D6.rollIgnoreLowest(4);
			character.setCurrentStatValue(score, scoreValue);
			character.setNaturalStatValue(score, scoreValue);

			int excValue = 0;
			if (scoreValue == 18) {
				excValue = D100.roll();
			}
			character.setCurrentExcStatValue(score, excValue);
			character.setNaturalExcStatValue(score, excValue);
		});
		character.setCurrentHP(10);
		character.setNaturalHP(10);
	}
}
