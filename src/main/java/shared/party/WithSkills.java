package shared.party;

import io.vavr.collection.Seq;

public interface WithSkills {
	Seq<CharacterValue> getSkillDescriptions();
}
