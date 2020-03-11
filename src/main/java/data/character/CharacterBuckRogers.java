package data.character;

import static io.vavr.collection.Map.entry;

import javax.annotation.Nonnull;

import io.vavr.collection.Array;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;

import character.AbilityScore;
import character.CharacterClass;
import character.CharacterGender;
import character.CharacterRace;
import character.CharacterSkill;
import character.CharacterStatus;
import character.ClassSelection;
import character.Item;
import character.Money;
import character.buckrogers.AbilityScoreBuckRogers;
import character.buckrogers.CharacterClassBuckRogers;
import character.buckrogers.CharacterStatusBuckRogers;
import character.buckrogers.MoneyBuckRogers;
import common.ByteBufferWrapper;
import data.ContentType;

public class CharacterBuckRogers extends AbstractCharacter {
	private static final Map<AbilityScoreBuckRogers, CharacterValueType> STATS_CURRENT_MAPPING = HashMap.ofEntries( //
		entry(AbilityScoreBuckRogers.STRENGTH, CharacterValueType.STR_CURRENT), //
		entry(AbilityScoreBuckRogers.INTELLIGENCE, CharacterValueType.INT_CURRENT), //
		entry(AbilityScoreBuckRogers.WISDOM, CharacterValueType.WIS_CURRENT), //
		entry(AbilityScoreBuckRogers.DEXTERITY, CharacterValueType.DEX_CURRENT), //
		entry(AbilityScoreBuckRogers.CONSTITUTION, CharacterValueType.CON_CURRENT), //
		entry(AbilityScoreBuckRogers.CHARISMA, CharacterValueType.CHA_CURRENT), //
		entry(AbilityScoreBuckRogers.TECH, CharacterValueType.TCH_CURRENT) //
	);
	private static final Map<AbilityScoreBuckRogers, CharacterValueType> STATS_NATURAL_MAPPING = HashMap.ofEntries( //
		entry(AbilityScoreBuckRogers.STRENGTH, CharacterValueType.STR_NATURAL), //
		entry(AbilityScoreBuckRogers.INTELLIGENCE, CharacterValueType.INT_NATURAL), //
		entry(AbilityScoreBuckRogers.WISDOM, CharacterValueType.WIS_NATURAL), //
		entry(AbilityScoreBuckRogers.DEXTERITY, CharacterValueType.DEX_NATURAL), //
		entry(AbilityScoreBuckRogers.CONSTITUTION, CharacterValueType.CON_NATURAL), //
		entry(AbilityScoreBuckRogers.CHARISMA, CharacterValueType.CHA_NATURAL), //
		entry(AbilityScoreBuckRogers.TECH, CharacterValueType.TCH_NATURAL) //
	);

	public CharacterBuckRogers(@Nonnull ByteBufferWrapper data, @Nonnull ContentType type) {
		super(data);
	}

	public CharacterBuckRogers(@Nonnull CharacterRace race, @Nonnull CharacterGender gender, @Nonnull ClassSelection classSelection) {
		write(CharacterValueType.GENDER, gender.ordinal());
		write(CharacterValueType.RACE, race.getValue());
		write(CharacterValueType.CLASS, CHAR_VALUES.getClassSelections().getOrElse(classSelection, null).byteValue());
	}

	@Override
	public CharacterGender getGender() {
		return CharacterGender.from(read(CharacterValueType.GENDER));
	}

	@Override
	public ClassSelection getClassSelection() {
		return ClassSelection.of(CharacterClassBuckRogers.from(read(CharacterValueType.CLASS)));
	}

	@Override
	public CharacterStatus getStatus() {
		return CharacterStatusBuckRogers.from(getStatusValue());
	}

	@Override
	public void setStatus(CharacterStatus status) {
		CharacterStatusBuckRogers s = (CharacterStatusBuckRogers) status;
		setStatusValue(s.ordinal(), s.ordinal());
	}

	@Override
	public int getLevel(CharacterClass charClass) {
		checkCharacterClass(charClass);
		return read(CharacterValueType.LEVEL_BUCK_ROGERS);
	}

	@Override
	public void setLevel(CharacterClass charClass, int level) {
		checkCharacterClass(charClass);
		write(CharacterValueType.LEVEL_BUCK_ROGERS, level);
	}

	@Override
	public Seq<AbilityScore> getAbilityScoreTypes() {
		return Array.of(AbilityScoreBuckRogers.values());
	}

	@Override
	public int getCurrentStatValue(AbilityScore stat) {
		return STATS_CURRENT_MAPPING.get((AbilityScoreBuckRogers) stat) //
			.map(this::read) //
			.getOrElseThrow(IllegalStateException::new);
	}

	@Override
	public void setCurrentStatValue(AbilityScore stat, int value) {
		STATS_CURRENT_MAPPING.get((AbilityScoreBuckRogers) stat) //
			.peek(t -> write(t, value));
	}

	@Override
	public int getCurrentStatExcValue(AbilityScore stat) {
		return 0; // not used
	}

	@Override
	public void setCurrentExcStatValue(AbilityScore stat, int value) {
		// not used
	}

	@Override
	public int getNaturalStatValue(AbilityScore stat) {
		return STATS_NATURAL_MAPPING.get((AbilityScoreBuckRogers) stat) //
			.map(this::read) //
			.getOrElseThrow(IllegalStateException::new);
	}

	@Override
	public void setNaturalStatValue(AbilityScore stat, int value) {
		STATS_NATURAL_MAPPING.get((AbilityScoreBuckRogers) stat) //
			.peek(t -> write(t, value));
	}

	@Override
	public int getNaturalStatExcValue(AbilityScore stat) {
		return 0; // not used
	}

	@Override
	public void setNaturalExcStatValue(AbilityScore stat, int value) {
		// not used
	}

	@Override
	public Seq<Money> getMoneyTypes() {
		return Array.of(MoneyBuckRogers.CREDITS);
	}

	@Override
	public int getMoneyValue(Money money) {
		return read(CharacterValueType.MONEY_CREDITS);
	}

	@Override
	public void setMoneyValue(Money money, int credits) {
		write(CharacterValueType.MONEY_CREDITS, credits);
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
