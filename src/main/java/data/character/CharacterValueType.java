package data.character;

import static data.character.ScriptModificationType.READ_WRITE;

public enum CharacterValueType {
	STR_NATURAL(DataType.BYTE, ScriptModificationType.READ_ONLY), //
	INT_NATURAL(DataType.BYTE, ScriptModificationType.READ_ONLY), //
	WIS_NATURAL(DataType.BYTE, ScriptModificationType.READ_ONLY), //
	DEX_NATURAL(DataType.BYTE, ScriptModificationType.READ_ONLY), //
	CON_NATURAL(DataType.BYTE, ScriptModificationType.READ_ONLY), //
	CHA_NATURAL(DataType.BYTE, ScriptModificationType.READ_ONLY), //
	TCH_NATURAL(DataType.BYTE, ScriptModificationType.READ_ONLY), //
	STR_CURRENT(DataType.BYTE, ScriptModificationType.READ_WRITE), //
	INT_CURRENT(DataType.BYTE, ScriptModificationType.READ_WRITE), //
	WIS_CURRENT(DataType.BYTE, ScriptModificationType.READ_WRITE), //
	DEX_CURRENT(DataType.BYTE, ScriptModificationType.READ_WRITE), //
	CON_CURRENT(DataType.BYTE, ScriptModificationType.READ_WRITE), //
	CHA_CURRENT(DataType.BYTE, ScriptModificationType.READ_WRITE), //
	TCH_CURRENT(DataType.BYTE, ScriptModificationType.READ_WRITE), //
	STR_EXC_NATURAL(DataType.BYTE, ScriptModificationType.READ_ONLY), //
	STR_EXC_CURRENT(DataType.BYTE, ScriptModificationType.READ_WRITE), //
	HP_NATURAL(DataType.BYTE, ScriptModificationType.READ_ONLY), //
	HP_CURRENT(DataType.BYTE, ScriptModificationType.READ_WRITE), //

	RACE(DataType.BYTE, ScriptModificationType.READ_ONLY), //
	CLASS(DataType.BYTE, ScriptModificationType.READ_ONLY), //
	AGE(DataType.SHORT, ScriptModificationType.READ_ONLY), //
	GENDER(DataType.BYTE, ScriptModificationType.READ_ONLY), //
	ALIGNMENT(DataType.BYTE, ScriptModificationType.READ_ONLY), //
	NPC_FLAG(DataType.BYTE, ScriptModificationType.READ_ONLY), //

	MONEY_COPPER(DataType.SHORT, ScriptModificationType.READ_WRITE), //
	MONEY_BRONZE(DataType.SHORT, ScriptModificationType.READ_WRITE), //
	MONEY_SILVER(DataType.SHORT, ScriptModificationType.READ_WRITE), //
	MONEY_ELEKTRUM(DataType.SHORT, ScriptModificationType.READ_WRITE), //
	MONEY_STEEL(DataType.SHORT, ScriptModificationType.READ_WRITE), //
	MONEY_GOLD(DataType.SHORT, ScriptModificationType.READ_WRITE), //
	MONEY_PLATINUM(DataType.SHORT, ScriptModificationType.READ_WRITE), //
	MONEY_GEM(DataType.SHORT, ScriptModificationType.READ_WRITE), //
	MONEY_JEWELRY(DataType.SHORT, ScriptModificationType.READ_WRITE), //
	MONEY_CREDITS(DataType.SHORT, ScriptModificationType.READ_WRITE), //

	LEVEL_CLERIC(DataType.BYTE, ScriptModificationType.READ_ONLY), //
	LEVEL_DRUID(DataType.BYTE, ScriptModificationType.READ_ONLY), //
	LEVEL_FIGHTER(DataType.BYTE, ScriptModificationType.READ_ONLY), //
	LEVEL_PALADIN(DataType.BYTE, ScriptModificationType.READ_ONLY), //
	LEVEL_RANGER(DataType.BYTE, ScriptModificationType.READ_ONLY), //
	LEVEL_MAGE(DataType.BYTE, ScriptModificationType.READ_ONLY), //
	LEVEL_THIEF(DataType.BYTE, ScriptModificationType.READ_ONLY), //
	LEVEL_KNIGHT(DataType.BYTE, ScriptModificationType.READ_ONLY), //
	LEVEL_MONK(DataType.BYTE, ScriptModificationType.READ_ONLY), //
	LEVEL_BUCK_ROGERS(DataType.BYTE, ScriptModificationType.READ_ONLY), //

	EXP(DataType.INT, ScriptModificationType.READ_WRITE), //
	STATUS(DataType.BYTE, ScriptModificationType.READ_WRITE), //

	SKILL_PILOT_ROCKET(DataType.BYTE, ScriptModificationType.READ_ONLY), //

	// Game specific values
	BUCK_ROGERS_INFECTION_STATUS(DataType.BYTE, ScriptModificationType.READ_WRITE), //
	BUCK_ROGERS_GRENADE_MUFFLED(DataType.BYTE, ScriptModificationType.READ_WRITE), //
	BUCK_ROGERS_TALOS_DEFEATED(DataType.BYTE, ScriptModificationType.READ_WRITE), //
	;

	private DataType dataType;
	private ScriptModificationType modificationType;

	private CharacterValueType(DataType dataType, ScriptModificationType modificationType) {
		this.dataType = dataType;
		this.modificationType = modificationType;
	}

	public DataType getDataType() {
		return dataType;
	}

	public boolean isReadWrite() {
		return READ_WRITE.equals(modificationType);
	}
}
