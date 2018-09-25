package engine.opcodes;

public enum EclOpCode {
	EXIT(0x00, 0), //
	GOTO(0x01, 1), //
	GOSUB(0x02, 1), //
	COMPARE(0x03, 2), //
	ADD(0x04, 3), //
	SUBTRACT(0x05, 3), //
	DIVIDE(0x06, 3), //
	MULTIPLY(0x07, 3), //
	RANDOM(0x08, 2), //
	WRITE_MEM(0x09, 2), //
	LOAD_CHAR(0x0A, 1), //
	LOAD_MON(0x0B, 3), //
	SETUP_MON(0x0C, 4), // TODO: Game specific
	APPROACH(0x0D, 0), //
	PICTURE(0x0E, 1), //
	INPUT_NUMBER(0x0F, 2), //
	INPUT_STRING(0x10, 2), //
	PRINT(0x11, 1), //
	PRINT_CLEAR(0x12, 1), //
	RETURN(0x13, 0), //
	COMPARE_AND(0x14, 4), //
	MENU_VERTICAL(0x15, 0), //
	IF_EQUALS(0x16, 0), //
	IF_NOT_EQUALS(0x17, 0), //
	IF_LESS(0x18, 0), //
	IF_GREATER(0x19, 0), //
	IF_LESS_EQUALS(0x1A, 0), //
	IF_GREATER_EQUALS(0x1B, 0), //
	CLEAR_MON(0x1C, 0), //
	PARTY_STRENGTH(0x1D, 1), //
	PARTY_CHECK(0x1E, 6), //
	UNKNOWN_1F(0x1F, 4), //
	NEW_ECL(0x20, 1), //
	LOAD_FILES(0x21, 3), //
	PARTY_SURPRISE(0x22, 3), // TODO: Game specific
	SURPRISE(0x23, 3), // TODO: Game specific
	COMBAT(0x24, 0), //
	ON_GOTO(0x25, 2), //
	ON_GOSUB(0x26, 2), //
	TREASURE(0x27, 2), // TODO: Game specific
	ROB(0x28, 3), //
	INPUT_RETURN(0x29, 0), // TODO: Game specific
	COPY_MEM(0x2A, 3), //
	MENU_HORIZONTAL(0x2B, 2), //
	PARLAY(0x2C, 0), // TODO: Game specific
	CALL(0x2D, 1), //
	DAMAGE(0x2E, 5), //
	AND(0x2F, 3), //
	OR(0x30, 3), //
	SELECT_ACTION(0x31, 2), // TODO: Game specific
	FIND_ITEM(0x32, 1), //
	PRINT_RETURN(0x33, 0), //
	CLOCK(0x34, 2), //
	WRITE_MEM_BASE_OFF(0x35, 3), //
	ADD_NPC(0x36, 2), //
	LOAD_PIECES(0x37, 3), //
	PROGRAM(0x38, 1), //
	WHO(0x39, 1), //
	DELAY(0x3A, 0), //
	SPELL(0x3B, 3), //
	PROTECTION(0x3C, 1), //
	CLEAR_BOX(0x3D, 0), //
	DUMP(0x3E, 0), //
	LOGBOOK_ENTRY(0x3F, 2), //
	DESTROY_ITEM(0x40, 1), //
	UNKNOWN_41(0x41, 2), //
	STOP_MOVE(0x42, 0), //
	UNKNOWN_43(0x43, 1), //
	UNKNOWN_44(0x44, 0), //
	UNKNOWN_45(0x45, 2), //
	UNKNOWN_46(0x46, 2), //
	UNKNOWN_47(0x47, 0), //
	UNKNOWN_48(0x48, 1), //
	UNKNOWN_49(0x49, 6), //
	UNKNOWN_4A(0x4A, 0), //
	UNKNOWN_4B(0x4B, 1), //
	PICTURE2(0x4C, 2);

	private int id;
	private int argCount;

	private EclOpCode(int id, int argCount) {
		this.id = id;
		this.argCount = argCount;
	}

	public int getId() {
		return id;
	}

	public int getArgCount() {
		return argCount;
	}
}
