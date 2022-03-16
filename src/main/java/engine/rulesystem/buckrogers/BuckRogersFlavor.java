package engine.rulesystem.buckrogers;

import static character.buckrogers.CharacterStatusBuckRogers.OKAY;
import static character.buckrogers.MoneyBuckRogers.CREDITS;
import static engine.rulesystem.Dice.D100;
import static engine.rulesystem.Dice.D6;

import javax.annotation.Nonnull;

import io.vavr.collection.List;
import io.vavr.collection.Seq;

import character.CharacterAlignment;
import character.CharacterClass;
import character.CharacterDeity;
import character.CharacterRace;
import character.ClassSelection;
import character.buckrogers.AbilityScoreBuckRogers;
import character.buckrogers.CharacterClassBuckRogers;
import character.buckrogers.CharacterRaceBuckRogers;
import data.character.AbstractCharacter;
import engine.rulesystem.Flavor;

public class BuckRogersFlavor implements Flavor {

	@Override
	public Seq<CharacterRace> getRaces() {
		return List.of(RaceData.values()) //
			.filter(race -> !race.getClassSelections().isEmpty()) //
			.map(RaceData::getCharacterRace); //
	}

	@Override
	public Seq<ClassSelection> getClassSelections(@Nonnull CharacterRace race) {
		return RaceData.by((CharacterRaceBuckRogers) race).getClassSelections();
	}

	@Override
	public boolean isAlignmentRequired() {
		return false;
	}

	@Override
	public Seq<CharacterAlignment> getAlignments(ClassSelection classSelection) {
		return List.empty();
	}

	@Override
	public boolean isDeityRequired(ClassSelection classSelection) {
		return false;
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
		character.getClassSelection().forEach(CharacterClassBuckRogers.class, c -> character.setLevel(c, 1));
		character.setMoneyValue(CREDITS, 1000);
		character.setStatus(OKAY);
		reroll(character);
	}

	@Override
	public void reroll(AbstractCharacter character) {
		List.of(AbilityScoreBuckRogers.values()).forEach(score -> {
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

	@Override
	public int getRequiredExperienceFor(CharacterClass clazz, int level) {
		return ClassData.forClass(clazz).getLevelInfo().forLevel(level).getExpNeeded();
	}
}
