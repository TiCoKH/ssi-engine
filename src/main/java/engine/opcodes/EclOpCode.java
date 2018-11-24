package engine.opcodes;

public enum EclOpCode {
	EXIT(0x00, 0, -1), //
	GOTO(0x01, 1, -1), //
	GOSUB(0x02, 1, -1), //
	COMPARE(0x03, 2, -1), //
	ADD(0x04, 3, -1), //
	SUBTRACT(0x05, 3, -1), //
	DIVIDE(0x06, 3, -1), //
	MULTIPLY(0x07, 3, -1), //
	RANDOM(0x08, 2, -1), //
	WRITE_MEM(0x09, 2, -1), //
	LOAD_CHAR(0x0A, 1, -1), //
	LOAD_MON(0x0B, 3, -1), //
	SPRITE_START3(0x0C, 3, -1), //
	SPRITE_START4(0x0C, 4, -1), //
	SPRITE_ADVANCE(0x0D, 0, -1), //
	PICTURE(0x0E, 1, -1), //
	INPUT_NUMBER(0x0F, 2, -1), //
	INPUT_STRING(0x10, 2, -1), //
	PRINT(0x11, 1, -1), //
	PRINT_CLEAR(0x12, 1, -1), //
	RETURN(0x13, 0, -1), //
	COMPARE_AND(0x14, 4, -1), //
	MENU_VERTICAL(0x15, 3, 2), //
	IF_EQUALS(0x16, 0, -1), //
	IF_NOT_EQUALS(0x17, 0, -1), //
	IF_LESS(0x18, 0, -1), //
	IF_GREATER(0x19, 0, -1), //
	IF_LESS_EQUALS(0x1A, 0, -1), //
	IF_GREATER_EQUALS(0x1B, 0, -1), //
	CLEAR_MON(0x1C, 0, -1), //
	INPUT_RETURN_1D(0x1D, 0, -1), //
	PARTY_STRENGTH(0x1D, 1, -1), //
	PARTY_CHECK(0x1E, 6, -1), //
	UNUSED_1F(0x1F, 0, -1), //
	JOURNAL_ENTRY(0x1F, 1, -1), //
	SPACE_COMBAT(0x1F, 4, -1), //
	NEW_ECL(0x20, 1, -1), //
	LOAD_AREA_MAP_DECO(0x21, 2, -1), //
	LOAD_AREA_MAP(0x21, 3, -1), //
	INPUT_YES_NO_22(0x22, 0, -1), //
	PARTY_SKILL_CHECK2(0x22, 2, -1), //
	PARTY_SKILL_CHECK3(0x22, 3, -1), //
	STOP_MOVE_23(0x23, 0, -1), //
	SURPRISE(0x23, 4, -1), //
	SKILL_CHECK(0x23, 3, -1), //
	COMBAT(0x24, 0, -1), //
	ON_GOTO(0x25, 2, 1), //
	ON_GOSUB(0x26, 2, 1), //
	TREASURE(0x27, 2, 1), //
	TREASURE_MULTICOIN(0x27, 8, -1), //
	TREASURE_MULTICOIN4(0x27, 4, 3), //
	ROB(0x28, 3, -1), //
	ENCOUNTER_MENU(0x29, 14, -1), //
	INPUT_RETURN_29(0x29, 0, -1), //
	POD_29(0x29, 0, -1), //
	COPY_MEM(0x2A, 3, -1), //
	MENU_HORIZONTAL(0x2B, 2, 1), //
	PARLAY(0x2C, 6, -1), //
	SOUND_EVENT_2C(0x2C, 1, -1), //
	INPUT_YES_NO_2C(0x2C, 0, -1), //
	CALL(0x2D, 1, -1), //
	DAMAGE(0x2E, 5, -1), //
	AND(0x2F, 3, -1), //
	OR(0x30, 3, -1), //
	SPRITE_OFF(0x31, 0, -1), //
	SELECT_ACTION(0x31, 2, 1), //
	FIND_ITEM(0x32, 1, -1), //
	GTTSF_32(0x32, 4, -1), //
	PRINT_RETURN(0x33, 0, -1), //
	CLOCK1(0x34, 1, -1), //
	CLOCK2(0x34, 2, -1), //
	WRITE_MEM_BASE_OFF(0x35, 3, -1), //
	NPC_ADD(0x36, 2, -1), //
	NPC_FIND(0x37, 2, -1), //
	LOAD_AREA_DECO(0x37, 3, -1), //
	PROGRAM(0x38, 1, -1), //
	WHO(0x39, 1, -1), //
	DELAY(0x3A, 0, -1), //
	SPELL(0x3B, 3, -1), //
	PRINT_RUNES(0x3C, 1, -1), //
	COPY_PROTECTION(0x3C, 1, -1), //
	STORE(0x3C, 1, -1), //
	CLEAR_BOX(0x3D, 0, -1), //
	NPC_REMOVE(0x3E, 0, -1), //
	HAS_EFFECT(0x3F, 1, -1), //
	LOGBOOK_ENTRY(0x3F, 2, -1), //
	DESTROY_ITEM(0x40, 1, -1), //
	GTTSF_40(0x40, 4, -1), //
	GIVE_EXP(0x41, 2, -1), //
	UNUSED_42(0x42, 0, -1), //
	STOP_MOVE_42(0x42, 0, -1), //
	LOAD_AREA_MAP_DECOS(0x42, 4, -1), //
	SOUND_EVENT_43(0x43, 1, -1), //
	UNKNOWN_44(0x44, 0, -1), //
	RANDOM0(0x45, 2, -1), //
	FOR_START(0x46, 2, -1), //
	FOR_REPEAT(0x47, 0, -1), //
	UNKNOWN_48(0x48, 1, -1), //
	UNKNOWN_49(0x49, 6, -1), //
	UNKNOWN_4A(0x4A, 0, -1), //
	UNKNOWN_4B(0x4B, 1, -1), //
	PICTURE2(0x4C, 2, -1);

	private int id;
	private int argCount;
	private int argIndexDynArgs;

	private EclOpCode(int id, int argCount, int argIndexDynArgs) {
		this.id = id;
		this.argCount = argCount;
		this.argIndexDynArgs = argIndexDynArgs;
	}

	public int getId() {
		return id;
	}

	public int getArgCount() {
		return argCount;
	}

	public boolean hasDynArgs() {
		return argIndexDynArgs >= 0;
	}

	public int getArgIndexDynArgs() {
		return argIndexDynArgs;
	}
}
