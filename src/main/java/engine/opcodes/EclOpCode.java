package engine.opcodes;

public enum EclOpCode {
	EXIT(0x00, 0, "EXIT"), //
	GOTO(0x01, 1, "GOTO"), //
	GOSUB(0x02, 1, "GOSUB"), //
	COMPARE(0x03, 2, "COMPARE"), //
	ADD(0x04, 3, "ADD"), //
	SUBTRACT(0x05, 3, "SUBTRACT"), //
	DIVIDE(0x06, 3, "DIVIDE"), //
	MULTIPLY(0x07, 3, "MULTIPLY"), //
	RANDOM(0x08, 2, "RANDOM"), //
	SAVE(0x09, 2, "SAVE"), //
	LOAD_CHAR(0x0A, 1, "LOAD CHARACTER"), //
	LOAD_MON(0x0B, 3, "LOAD MONSTER"), //
	SETUP_MON(0x0C, 3, "SETUP MONSTER"), //
	APPROACH(0x0D, 0, "APPROACH"), //
	PICTURE(0x0E, 1, "PICTURE"), //
	INPUT_NUMBER(0x0F, 2, "INPUT NUMBER"), //
	INPUT_STRING(0x10, 2, "INPUT STRING"), //
	PRINT(0x11, 1, "PRINT"), //
	PRINT_CLEAR(0x12, 1, "PRINTCLEAR"), //
	RETURN(0x13, 0, "RETURN"), //
	COMPARE_AND(0x14, 4, "COMPARE AND"), //
	MENU_VERTICAL(0x15, 0, "VERTICAL MENU"), //
	IF_EQUALS(0x16, 0, "IF ="), //
	IF_NOT_EQUALS(0x17, 0, "IF <>"), //
	IF_LESS(0x18, 0, "IF <"), //
	IF_GREATER(0x19, 0, "IF >"), //
	IF_LESS_EQUALS(0x1A, 0, "IF <="), //
	IF_GREATER_EQUALS(0x1B, 0, "IF >="), //
	CLEAR_MON(0x1C, 0, "CLEARMONSTERS"), //
	PARTY_STRENGTH(0x1D, 1, "PARTYSTRENGTH"), //
	PARTY_CHECK(0x1E, 6, "CHECKPARTY"), //
	UNKNOWN_1F(0x1F, 2, "notsure 0x1f"), //
	NEW_ECL(0x20, 1, "NEWECL"), //
	LOAD_FILES(0x21, 3, "LOAD FILES"), //
	PARTY_SURPRISE(0x22, 2, "PARTY SURPRISE"), //
	SURPRISE(0x23, 4, "SURPRISE"), //
	COMBAT(0x24, 0, "COMBAT"), //
	ON_GOTO(0x25, 0, "ON GOTO"), //
	ON_GOSUB(0x26, 0, "ON GOSUB"), //
	TREASURE(0x27, 8, "TREASURE"), //
	ROB(0x28, 3, "ROB"), //
	MENU_ENCOUNTER(0x29, 14, "ENCOUNTER MENU"), //
	TABLE_GET(0x2A, 3, "GETTABLE"), //
	MENU_HORIZONTAL(0x2B, 0, "HORIZONTAL MENU"), //
	PARLAY(0x2C, 6, "PARLAY"), //
	CALL(0x2D, 1, "CALL"), //
	DAMAGE(0x2E, 5, "DAMAGE"), //
	AND(0x2F, 3, "AND"), //
	OR(0x30, 3, "OR"), //
	SPRITE_OFF(0x31, 0, "SPRITE OFF"), //
	FIND_ITEM(0x32, 1, "FIND ITEM"), //
	PRINT_RETURN(0x33, 0, "PRINT RETURN"), //
	CLOCK(0x34, 2, "ECL CLOCK"), //
	TABLE_SET(0x35, 3, "SAVE TABLE"), //
	ADD_NPC(0x36, 2, "ADD NPC"), //
	LOAD_PIECES(0x37, 3, "LOAD PIECES"), //
	PROGRAM(0x38, 1, "PROGRAM"), //
	WHO(0x39, 1, "WHO"), //
	DELAY(0x3A, 0, "DELAY"), //
	SPELL(0x3B, 3, "SPELL"), //
	PROTECTION(0x3C, 1, "PROTECTION"), //
	CLEAR_BOX(0x3D, 0, "CLEAR BOX"), //
	DUMP(0x3E, 0, "DUMP"), //
	FIND_SPECIAL(0x3F, 1, "FIND SPECIAL"), //
	DESTROY_ITEM(0x40, 1, "DESTROY ITEMS");

	private int id;
	private int argCount;
	private String description;

	private EclOpCode(int id, int argCount, String description) {
		this.id = id;
		this.argCount = argCount;
		this.description = description;
	}

	public int getId() {
		return id;
	}

	public int getArgCount() {
		return argCount;
	}

	public String getDescription() {
		return description;
	}
}
