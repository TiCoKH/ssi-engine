package engine.character;

import static io.vavr.API.Seq;

import java.util.Objects;
import java.util.Optional;

import io.vavr.collection.Seq;

import data.character.AbstractCharacter;
import engine.rulesystem.Flavor;
import engine.rulesystem.Flavors;
import shared.CustomGoldboxString;
import shared.GoldboxString;
import shared.party.CharacterSheet;
import shared.party.CharacterValue;
import shared.party.WithAlignment;
import shared.party.WithSkills;

public class CharacterSheetImpl implements CharacterSheet {

	private final Flavor flavor;
	private final AbstractCharacter character;

	public CharacterSheetImpl(Flavor flavor, AbstractCharacter character) {
		this.flavor = flavor;
		this.character = character;
	}

	public AbstractCharacter getCharacter() {
		return character;
	}

	@Override
	public GoldboxString getName() {
		return new CustomGoldboxString(character.getName());
	}

	@Override
	public GoldboxString getRaceDescription() {
		return character.getRace().getName();
	}

	@Override
	public GoldboxString getGenderDescription() {
		return character.getGender().getName();
	}

	@Override
	public GoldboxString getClassDescription() {
		return character.getClassSelection().getDescription();
	}

	@Override
	public GoldboxString getCurrentHPDescription() {
		return new CustomGoldboxString(Integer.toString(character.getCurrentHP()));
	}

	@Override
	public GoldboxString getHPDescription() {
		return new CustomGoldboxString(String.format("%d/%d", character.getCurrentHP(), character.getNaturalHP()));
	}

	@Override
	public GoldboxString getStatusDescription() {
		return character.getStatus().getName();
	}

	@Override
	public GoldboxString getLevelDescription() {
		return new CustomGoldboxString(character.getClassSelection() //
			.map(character::getLevel) //
			.map(i -> Integer.toString(i)) //
			.reduceLeft((a, b) -> a + "/" + b));
	}

	@Override
	public GoldboxString getExperienceDescription() {
		return new CustomGoldboxString(Integer.toString(character.getExperience()));
	}

	@Override
	public GoldboxString getAgeDescription() {
		if (Flavors.BUCK_ROGERS.getFlavor().equals(flavor))
			return new CustomGoldboxString(String.format("%d YEARS", character.getAge()));
		return new CustomGoldboxString(String.format("%d", character.getAge()));
	}

	@Override
	public Seq<CharacterValue> getStatDescriptions() {
		return character.getAbilityScoreTypes() //
			.map(stat -> new CharacterValue() {
				@Override
				public GoldboxString getValue() {
					return new CustomGoldboxString(Seq(character.getCurrentStatValue(stat),
						character.getCurrentStatExcValue(stat) != 0 ? character.getCurrentStatExcValue(stat) : null)
						.filter(Objects::nonNull)
						.mkString("/"));
				}

				@Override
				public GoldboxString getName() {
					return stat.getName();
				}
			});
	}

	@Override
	public Seq<CharacterValue> getMoneyDescriptions() {
		return character.getMoneyTypes() //
			.map(money -> new CharacterValue() {
				@Override
				public GoldboxString getValue() {
					return new CustomGoldboxString(Integer.toString(character.getMoneyValue(money)));
				}

				@Override
				public GoldboxString getName() {
					return money.getName();
				}
			});
	}

	@Override
	public GoldboxString getMovementRateDescription() {
		// TODO Auto-generated method stub
		return new CustomGoldboxString("10");
	}

	@Override
	public GoldboxString getTHACODescription() {
		// TODO Auto-generated method stub
		return new CustomGoldboxString("10");
	}

	@Override
	public GoldboxString getEncumbranceDescription() {
		// TODO Auto-generated method stub
		return new CustomGoldboxString("10");
	}

	@Override
	public GoldboxString getEquippedWeaponDescription() {
		// TODO Auto-generated method stub
		return new CustomGoldboxString("10");
	}

	@Override
	public GoldboxString getDamageDescription() {
		// TODO Auto-generated method stub
		return new CustomGoldboxString("10");
	}

	@Override
	public GoldboxString getEquippedArmorDescription() {
		// TODO Auto-generated method stub
		return new CustomGoldboxString("10");
	}

	@Override
	public GoldboxString getArmorClassDescription() {
		// TODO Auto-generated method stub
		return new CustomGoldboxString("10");
	}

	@Override
	public Optional<WithAlignment> withAlignment() {
		return character.getAlignment().map(alignment -> alignment::getDescription);
	}

	@Override
	public Optional<WithSkills> withGeneralSkills() {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<WithSkills> withClassSkills() {
		// TODO Auto-generated method stub
		return Optional.empty();
	}
}
