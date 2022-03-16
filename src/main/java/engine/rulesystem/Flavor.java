package engine.rulesystem;

import javax.annotation.Nonnull;

import io.vavr.collection.Seq;

import character.CharacterAlignment;
import character.CharacterClass;
import character.CharacterDeity;
import character.CharacterRace;
import character.ClassSelection;
import data.character.AbstractCharacter;

public interface Flavor {
	Seq<CharacterRace> getRaces();

	Seq<ClassSelection> getClassSelections(@Nonnull CharacterRace race);

	boolean isAlignmentRequired();

	Seq<CharacterAlignment> getAlignments(ClassSelection classSelection);

	boolean isDeityRequired(@Nonnull ClassSelection classSelection);

	Seq<CharacterDeity> getDeities(@Nonnull ClassSelection classSelection);

	void initCharacter(@Nonnull AbstractCharacter character, int initialExperience);

	void reroll(@Nonnull AbstractCharacter character);

	int getRequiredExperienceFor(CharacterClass clazz, int level);
}
