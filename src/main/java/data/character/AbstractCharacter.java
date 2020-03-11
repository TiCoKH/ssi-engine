package data.character;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Optional;

import javax.annotation.Nonnull;

import io.vavr.collection.Seq;

import character.AbilityScore;
import character.CharacterAlignment;
import character.CharacterClass;
import character.CharacterGender;
import character.CharacterRace;
import character.CharacterStatus;
import character.ClassSelection;
import character.Item;
import character.Money;
import common.ByteBufferWrapper;
import data.Content;

public abstract class AbstractCharacter extends Content {

	protected static CharacterValues CHAR_VALUES;

	private ByteBufferWrapper file;
	private ByteBufferWrapper memoryMapping;

	protected AbstractCharacter() {
		this(ByteBufferWrapper.allocateLE(CHAR_VALUES.getFileSize()));
	}

	protected AbstractCharacter(@Nonnull ByteBufferWrapper data) {
		this.file = data;
		this.memoryMapping = ByteBufferWrapper.allocateLE(0x1FF);
		CHAR_VALUES.copyToMemory(file, memoryMapping);
		setStatus(getStatus());
	}

	public static void configValues(@Nonnull CharacterValues characterValues) {
		CHAR_VALUES = characterValues;
	}

	public int readValue(int address, boolean isShort) {
		return isShort ? memoryMapping.getUnsignedShort(address) : memoryMapping.getUnsigned(address);
	}

	public void writeValue(int address, int value, boolean isShort) {
		if (isShort) {
			memoryMapping.putShort(address, (short) value);
		} else {
			memoryMapping.put(address, (byte) value);
		}
	}

	public void writeTo(FileChannel fc) throws IOException {
		CharacterStatus status = getStatus();
		CHAR_VALUES.copyToFile(file, memoryMapping);
		setStatus(status);

		file.position(0).writeTo(fc);
	}

	public CharacterRace getRace() {
		return CHAR_VALUES.interpret(read(CharacterValueType.RACE));
	}

	public abstract CharacterGender getGender();

	public abstract ClassSelection getClassSelection();

	public Optional<CharacterAlignment> getAlignment() {
		return Optional.empty();
	}

	public String getName() {
		return CHAR_VALUES.readName(file);
	}

	public void setName(String name) {
		CHAR_VALUES.writeName(file, name);
	}

	public int getCurrentHP() {
		return read(CharacterValueType.HP_CURRENT);
	}

	public void setCurrentHP(int currentHP) {
		write(CharacterValueType.HP_CURRENT, currentHP);
	}

	public int getNaturalHP() {
		return read(CharacterValueType.HP_NATURAL);
	}

	public void setNaturalHP(int naturalHP) {
		write(CharacterValueType.HP_NATURAL, naturalHP);
	}

	public abstract CharacterStatus getStatus();

	protected int getStatusValue() {
		return CHAR_VALUES.readStatus(file);
	}

	public abstract void setStatus(CharacterStatus status);

	protected void setStatusValue(int statusFile, int statusMemory) {
		CHAR_VALUES.writeStatus(file, memoryMapping, statusFile, statusMemory);
	}

	public abstract int getLevel(CharacterClass charClass);

	public abstract void setLevel(CharacterClass charClass, int level);

	public int getExperience() {
		return read(CharacterValueType.EXP);
	}

	public void addExperience(int experience) {
		write(CharacterValueType.EXP, getExperience() + experience);
	}

	public int getAge() {
		return read(CharacterValueType.AGE);
	}

	public void setAge(int age) {
		write(CharacterValueType.AGE, age);
	}

	public abstract Seq<AbilityScore> getAbilityScoreTypes();

	public abstract int getCurrentStatValue(AbilityScore stat);

	public abstract void setCurrentStatValue(AbilityScore stat, int value);

	public abstract int getCurrentStatExcValue(AbilityScore stat);

	public abstract void setCurrentExcStatValue(AbilityScore stat, int value);

	public abstract int getNaturalStatValue(AbilityScore stat);

	public abstract void setNaturalStatValue(AbilityScore stat, int value);

	public abstract int getNaturalStatExcValue(AbilityScore stat);

	public abstract void setNaturalExcStatValue(AbilityScore stat, int value);

	public abstract Seq<Money> getMoneyTypes();

	public abstract int getMoneyValue(Money money);

	public abstract void setMoneyValue(Money money, int value);

	public abstract int getMovementRate();

	public abstract int getTHACO();

	public abstract Seq<Item> getItems();

	public abstract boolean isEquipped(Item item);

	protected void checkCharacterClass(CharacterClass charClass) {
		if (!getClassSelection().contains(charClass)) {
			throw new IllegalArgumentException("Wrong character class " + charClass.toString());
		}
	}

	protected int read(CharacterValueType type) {
		return CHAR_VALUES.read(type, file, memoryMapping);
	}

	protected void write(CharacterValueType type, int value) {
		CHAR_VALUES.write(type, file, memoryMapping, value);
	}
}
