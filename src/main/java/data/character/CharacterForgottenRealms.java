package data.character;

import static io.vavr.collection.Map.entry;

import java.util.Optional;

import javax.annotation.Nonnull;

import io.vavr.collection.Array;
import io.vavr.collection.HashMap;
import io.vavr.collection.LinkedHashMap;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;

import character.AbilityScore;
import character.CharacterAlignment;
import character.CharacterClass;
import character.CharacterGender;
import character.CharacterRace;
import character.CharacterSkill;
import character.CharacterStatus;
import character.ClassSelection;
import character.Item;
import character.Money;
import character.forgottenrealms.AbilityScoreForgottenRealms;
import character.forgottenrealms.CharacterClassForgottenRealms;
import character.forgottenrealms.CharacterStatusForgottenRealms;
import character.forgottenrealms.MoneyForgottenRealms;
import common.ByteBufferWrapper;
import data.ContentType;

public class CharacterForgottenRealms extends AbstractCharacter {
	@SuppressWarnings("unchecked")
	private static final Map<MoneyForgottenRealms, CharacterValueType> MONEY_MAPPING = LinkedHashMap.ofEntries( //
		entry(MoneyForgottenRealms.COPPER, CharacterValueType.MONEY_COPPER), //
		entry(MoneyForgottenRealms.BRONZE, CharacterValueType.MONEY_BRONZE), //
		entry(MoneyForgottenRealms.SILVER, CharacterValueType.MONEY_SILVER), //
		entry(MoneyForgottenRealms.ELEKTRUM, CharacterValueType.MONEY_ELEKTRUM), //
		entry(MoneyForgottenRealms.GOLD, CharacterValueType.MONEY_GOLD), //
		entry(MoneyForgottenRealms.PLATINUM, CharacterValueType.MONEY_PLATINUM), //
		entry(MoneyForgottenRealms.STEEL, CharacterValueType.MONEY_STEEL), //
		entry(MoneyForgottenRealms.GEM, CharacterValueType.MONEY_GEM), //
		entry(MoneyForgottenRealms.JEWELRY, CharacterValueType.MONEY_JEWELRY) //
	);
	private static final Map<AbilityScoreForgottenRealms, CharacterValueType> STATS_CURRENT_MAPPING = HashMap.ofEntries( //
		entry(AbilityScoreForgottenRealms.STRENGTH, CharacterValueType.STR_CURRENT), //
		entry(AbilityScoreForgottenRealms.INTELLIGENCE, CharacterValueType.INT_CURRENT), //
		entry(AbilityScoreForgottenRealms.WISDOM, CharacterValueType.WIS_CURRENT), //
		entry(AbilityScoreForgottenRealms.DEXTERITY, CharacterValueType.DEX_CURRENT), //
		entry(AbilityScoreForgottenRealms.CONSTITUTION, CharacterValueType.CON_CURRENT), //
		entry(AbilityScoreForgottenRealms.CHARISMA, CharacterValueType.CHA_CURRENT) //
	);
	private static final Map<AbilityScoreForgottenRealms, CharacterValueType> STATS_NATURAL_MAPPING = HashMap.ofEntries( //
		entry(AbilityScoreForgottenRealms.STRENGTH, CharacterValueType.STR_NATURAL), //
		entry(AbilityScoreForgottenRealms.INTELLIGENCE, CharacterValueType.INT_NATURAL), //
		entry(AbilityScoreForgottenRealms.WISDOM, CharacterValueType.WIS_NATURAL), //
		entry(AbilityScoreForgottenRealms.DEXTERITY, CharacterValueType.DEX_NATURAL), //
		entry(AbilityScoreForgottenRealms.CONSTITUTION, CharacterValueType.CON_NATURAL), //
		entry(AbilityScoreForgottenRealms.CHARISMA, CharacterValueType.CHA_NATURAL) //
	);
	private static final Map<CharacterClassForgottenRealms, CharacterValueType> CLASS_MAPPING = HashMap.ofEntries( //
		entry(CharacterClassForgottenRealms.CLERIC, CharacterValueType.LEVEL_CLERIC), //
		entry(CharacterClassForgottenRealms.DRUID, CharacterValueType.LEVEL_DRUID), //
		entry(CharacterClassForgottenRealms.FIGHTER, CharacterValueType.LEVEL_FIGHTER), //
		entry(CharacterClassForgottenRealms.KNIGHT, CharacterValueType.LEVEL_KNIGHT), //
		entry(CharacterClassForgottenRealms.MAGE, CharacterValueType.LEVEL_MAGE), //
		entry(CharacterClassForgottenRealms.MONK, CharacterValueType.LEVEL_MONK), //
		entry(CharacterClassForgottenRealms.PALADIN, CharacterValueType.LEVEL_PALADIN), //
		entry(CharacterClassForgottenRealms.RANGER, CharacterValueType.LEVEL_RANGER), //
		entry(CharacterClassForgottenRealms.THIEF, CharacterValueType.LEVEL_THIEF) //
	);

	public CharacterForgottenRealms(@Nonnull ByteBufferWrapper data, @Nonnull ContentType type) {
		super(data);
	}

	public CharacterForgottenRealms(@Nonnull CharacterRace race, @Nonnull CharacterGender gender, @Nonnull ClassSelection classSelection,
		@Nonnull CharacterAlignment alignment) {

		write(CharacterValueType.GENDER, gender.getValue());
		write(CharacterValueType.RACE, race.getValue());
		write(CharacterValueType.CLASS, CHAR_VALUES.getClassSelections().getOrElse(classSelection, null).byteValue());
		write(CharacterValueType.ALIGNMENT, alignment.getValue());
	}

	@Override
	public CharacterGender getGender() {
		return CharacterGender.from(read(CharacterValueType.GENDER));
	}

	@Override
	public ClassSelection getClassSelection() {
		final int classesId = read(CharacterValueType.CLASS);
		return CHAR_VALUES.getClassSelections() //
			.filter((cs, i) -> i == classesId) //
			.get()._1;
	}

	@Override
	public Optional<CharacterAlignment> getAlignment() {
		return Optional.of(CharacterAlignment.from(read(CharacterValueType.ALIGNMENT)));
	}

	@Override
	public CharacterStatus getStatus() {
		return CharacterStatusForgottenRealms.from(getStatusValue());
	}

	@Override
	public void setStatus(CharacterStatus status) {
		CharacterStatusForgottenRealms s = (CharacterStatusForgottenRealms) status;
		setStatusValue(s.ordinal(), s.getMemStatus());
	}

	@Override
	public int getLevel(CharacterClass charClass) {
		checkCharacterClass(charClass);
		return CLASS_MAPPING.get((CharacterClassForgottenRealms) charClass) //
			.map(this::read) //
			.getOrElseThrow(IllegalStateException::new);
	}

	@Override
	public void setLevel(CharacterClass charClass, int level) {
		checkCharacterClass(charClass);
		CLASS_MAPPING.get((CharacterClassForgottenRealms) charClass) //
			.peek(t -> write(t, level));
	}

	@Override
	public Seq<AbilityScore> getAbilityScoreTypes() {
		return Array.of(AbilityScoreForgottenRealms.values());
	}

	@Override
	public int getCurrentStatValue(AbilityScore stat) {
		return STATS_CURRENT_MAPPING.get((AbilityScoreForgottenRealms) stat) //
			.map(this::read) //
			.getOrElseThrow(IllegalStateException::new);
	}

	@Override
	public void setCurrentStatValue(AbilityScore stat, int value) {
		STATS_CURRENT_MAPPING.get((AbilityScoreForgottenRealms) stat) //
			.peek(t -> write(t, value));
	}

	@Override
	public int getCurrentStatExcValue(AbilityScore stat) {
		if (AbilityScoreForgottenRealms.STRENGTH.equals(stat)) {
			return read(CharacterValueType.STR_EXC_CURRENT);
		}
		return 0;
	}

	@Override
	public void setCurrentExcStatValue(AbilityScore stat, int value) {
		if (AbilityScoreForgottenRealms.STRENGTH.equals(stat)) {
			write(CharacterValueType.STR_EXC_CURRENT, value);
		}
	}

	@Override
	public int getNaturalStatValue(AbilityScore stat) {
		return STATS_NATURAL_MAPPING.get((AbilityScoreForgottenRealms) stat) //
			.map(this::read) //
			.getOrElseThrow(IllegalStateException::new);
	}

	@Override
	public void setNaturalStatValue(AbilityScore stat, int value) {
		STATS_NATURAL_MAPPING.get((AbilityScoreForgottenRealms) stat) //
			.peek(t -> write(t, value));
	}

	@Override
	public int getNaturalStatExcValue(AbilityScore stat) {
		if (AbilityScoreForgottenRealms.STRENGTH.equals(stat)) {
			return read(CharacterValueType.STR_EXC_NATURAL);
		}
		return 0;
	}

	@Override
	public void setNaturalExcStatValue(AbilityScore stat, int value) {
		if (AbilityScoreForgottenRealms.STRENGTH.equals(stat)) {
			write(CharacterValueType.STR_EXC_NATURAL, value);
		}
	}

	@Override
	public Seq<Money> getMoneyTypes() {
		return MONEY_MAPPING //
			.toList() //
			.filter(t2 -> CHAR_VALUES.hasValueType(t2._2)) //
			.map(t2 -> t2._1);
	}

	@Override
	public int getMoneyValue(Money money) {
		return MONEY_MAPPING.get((MoneyForgottenRealms) money) //
			.map(this::read) //
			.getOrElseThrow(IllegalStateException::new);
	}

	@Override
	public void setMoneyValue(Money money, int amount) {
		MONEY_MAPPING.get((MoneyForgottenRealms) money) //
			.peek(t -> write(t, amount));
	}

	@Override
	public int getMovementRate() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getTHACO() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Seq<Item> getItems() {
		return Array.empty();
	}

	@Override
	public boolean isEquipped(Item item) {
		// TODO Auto-generated method stub
		return false;
	}

	public Seq<CharacterSkill> getSkills() {
		return Array.empty();
	}

	public int getSkillValue(CharacterSkill skill) {
		// TODO Auto-generated method stub
		return 0;
	}

}
