package shared.party;

import java.util.Optional;

import io.vavr.collection.Seq;

import shared.GoldboxString;

public interface CharacterSheet extends PartyMember {
	GoldboxString getRaceDescription();

	GoldboxString getGenderDescription();

	GoldboxString getClassDescription();

	GoldboxString getLevelDescription();

	GoldboxString getHPDescription();

	GoldboxString getStatusDescription();

	GoldboxString getExperienceDescription();

	GoldboxString getAgeDescription();

	Seq<CharacterValue> getStatDescriptions();

	Seq<CharacterValue> getMoneyDescriptions();

	GoldboxString getTHACODescription();

	GoldboxString getMovementRateDescription();

	GoldboxString getEncumbranceDescription();

	GoldboxString getEquippedWeaponDescription();

	GoldboxString getDamageDescription();

	GoldboxString getEquippedArmorDescription();

	Optional<WithAlignment> withAlignment();

	Optional<WithSkills> withClassSkills();

	Optional<WithSkills> withGeneralSkills();
}
